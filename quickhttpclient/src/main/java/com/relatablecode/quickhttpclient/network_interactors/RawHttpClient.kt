package com.relatablecode.quickhttpclient.network_interactors


import android.util.Log
import com.relatablecode.quickhttpclient.cache_helpers.CacheStrategy
import com.relatablecode.quickhttpclient.cache_helpers.InMemoryCache
import com.relatablecode.quickhttpclient.cache_helpers.InMemoryImageCache
import com.relatablecode.quickhttpclient.interceptors.modifyWith
import com.relatablecode.quickhttpclient.logging_utils.Logger
import com.relatablecode.quickhttpclient.quickhttpclient.QuickHttpClient
import com.relatablecode.quickhttpclient.request_body_types.RequestBody
import com.relatablecode.quickhttpclient.request_helpers.NetworkRequest
import com.relatablecode.quickhttpclient.request_helpers.RequestMethod
import com.relatablecode.quickhttpclient.ssl_helpers.setupSSL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection

/**
 * The `RawHttpClient` object provides functionalities for making raw network requests. It provides
 * utilities for setting up connections, handling HTTPS requirements, sending request bodies,
 * reading responses, and managing caches. It uses a combination of standard Java and Kotlin
 * libraries and other utility classes from the project.
 */
object RawHttpClient {

    /** Logging tag for the `RawHttpClient`. */
    const val TAG = "RawHttpClient"

    /**
     * Initializes and configures an [HttpURLConnection] based on the given [NetworkRequest].
     *
     * @param networkRequest The parameters for the network request.
     * @return A [Result] object that wraps the configured [HttpURLConnection].
     */
    private fun setupConnection(networkRequest: NetworkRequest): Result<HttpURLConnection> = kotlin.runCatching {
        val urlConnection = URL(networkRequest.endpoint).openConnection() as HttpURLConnection
        configureHttpsIfRequired(urlConnection)
        setupHeadersAndTimeout(urlConnection, networkRequest)
        urlConnection
    }.onFailure {
        Logger.e(TAG, "Error setting up connection: ${it.message}")
    }

    /**
     * Checks and applies HTTPS configuration for the given [HttpURLConnection].
     *
     * If the connection is an instance of [HttpsURLConnection], it sets up SSL configurations
     * and hostname verification.
     *
     * @param urlConnection The connection to be potentially configured as HTTPS.
     */
    private fun configureHttpsIfRequired(urlConnection: HttpURLConnection) {
        if (urlConnection is HttpsURLConnection) {
            QuickHttpClient.hostnameVerification.first.takeIf { it }?.let {
                urlConnection.hostnameVerifier = HostnameVerifier { hostname, _ ->
                    hostname == QuickHttpClient.hostnameVerification.second
                }
            }
            urlConnection.setupSSL(QuickHttpClient.publicKeys, QuickHttpClient.certificates)
        }
    }

    /**
     * Sets up request headers and timeout values for the given [HttpURLConnection].
     *
     * @param urlConnection The connection whose headers and timeouts are to be configured.
     * @param networkRequest The network request parameters used for the configuration.
     */
    private fun setupHeadersAndTimeout(urlConnection: HttpURLConnection, networkRequest: NetworkRequest) {
        urlConnection.apply {
            requestMethod = networkRequest.requestMethod.key
            connectTimeout = networkRequest.connectTimeout ?: connectTimeout
            readTimeout = networkRequest.readTimeout ?: readTimeout
            networkRequest.headers.forEach { (key, value) -> setRequestProperty(key, value) }
        }
    }

    /**
     * Sends the provided [RequestBody] with the given [HttpURLConnection].
     *
     * @param urlConnection The connection used to send the body.
     * @param body The request body to be sent. If null, nothing is sent.
     */
    private fun sendRequestBody(urlConnection: HttpURLConnection, body: RequestBody?) {
        body?.apply {
            urlConnection.setRequestProperty("Content-Type", contentType())
            urlConnection.outputStream.use(::writeTo)
        }
    }

    /**
     * Reads and returns the response from the provided [HttpURLConnection].
     *
     * @param urlConnection The connection from which the response is read.
     * @return The response text as a [String].
     */
    private fun readResponse(urlConnection: HttpURLConnection): String =
        urlConnection.inputStream.bufferedReader().use(BufferedReader::readText)

    /**
     * Attempts a single network request using the given [HttpURLConnection] and [NetworkRequest].
     * Handles the request by sending any available body, invoking the response handler, and caching
     * if necessary.
     *
     * @param urlConnection The [HttpURLConnection] used for the network request.
     * @param networkRequest The parameters and configurations for the network request.
     * @param responseHandler A callback to handle the input stream response.
     * @return A [Result] object containing the response as a [String] on success or an exception on failure.
     */
    private suspend fun attemptRequest(
        urlConnection: HttpURLConnection,
        networkRequest: NetworkRequest,
        responseHandler: (InputStream) -> Unit
    ): Result<String> {
        try {
            logRequest(networkRequest, urlConnection)
            sendRequestBody(urlConnection, networkRequest.body)

            val inputStream: InputStream = urlConnection.inputStream
            responseHandler(inputStream)

            val response = readResponse(urlConnection)
            logResponse(urlConnection, response)

            cacheIfRequired(networkRequest, response, inputStream)

            return Result.success(response)

        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure(e)
        }
    }

    /**
     * Caches the response if the network request mandates caching.
     *
     * This function handles caching for both image responses and general text responses.
     *
     * @param networkRequest The network request parameters and configurations.
     * @param response The response as a [String].
     */
    private suspend fun cacheIfRequired(networkRequest: NetworkRequest, response: String, imageStream: InputStream? = null) {
        if (networkRequest.cacheStrategy == CacheStrategy.InMemory && networkRequest.requestMethod == RequestMethod.GET) {
            if (imageStream != null) {
                InMemoryImageCache.putAsync(networkRequest.generateCacheKey(), imageStream)
            } else {
                InMemoryCache.putAsync(networkRequest.generateCacheKey(), response)
            }
        }
    }

    /**
     * Makes a network request based on the provided [NetworkRequest]. It ensures to handle request modifications,
     * caching mechanisms, and retry mechanisms. This function is the primary entry point to the [RawHttpClient] for
     * making network requests.
     *
     * @param networkRequest The parameters and configurations for the network request.
     * @param responseHandler A callback to handle the input stream response.
     * @return A [Result] object containing the response as a [String] on success or an exception on failure.
     */
    suspend fun makeNetworkRequest(
        networkRequest: NetworkRequest,
        responseHandler: (InputStream) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {

        modifyRequestUsingInterceptors(networkRequest)

        checkAndRetrieveFromCache(networkRequest, responseHandler)?.let {
            return@withContext it
        }

        var remainingRetries = (networkRequest.retryCount ?: 0) + 1

        var lastException: Exception? = null


        do {
            val urlConnection = setupConnection(networkRequest).getOrElse { throw it }
            try {
                val result = attemptRequest(urlConnection, networkRequest, responseHandler)
                result.onSuccess {
                    urlConnection.disconnect()
                }.onFailure {
                    throw it
                }
                return@withContext result
            } catch (e: Exception) {
                Logger.e(TAG, "Error while making network request: ${e.message}")
                lastException = e
                remainingRetries--
                if (remainingRetries > 0) {
                    Log.d(TAG, "Delayed with remaining $remainingRetries")
                    delay(1000)
                }
            } finally {
                urlConnection.disconnect()
            }
        } while (remainingRetries > 0)

        val errorResult = Result.failure<String>(
            lastException ?: Throwable("Unknown cause, check makeRequest")
        )

        return@withContext errorResult

    }

    /**
     * Modifies the given [NetworkRequest] by invoking any available interceptors.
     *
     * @param networkRequest The [NetworkRequest] that may be modified.
     */
    private fun modifyRequestUsingInterceptors(networkRequest: NetworkRequest) {
        QuickHttpClient.requestInterceptors.forEach { interceptor ->
            interceptor.intercept(networkRequest).let {
                networkRequest.modifyWith(it)
            }
        }
    }

    // ... (all your current functions and members) ...

    /**
     * Checks the cache for a previously stored response based on the [NetworkRequest] parameters.
     * If a cached response is found and it's valid, the response is returned immediately.
     *
     * @param networkRequest The network request parameters and configurations.
     * @return A [Result] object containing the cached response as a [String] if available, or null otherwise.
     */
    private suspend fun checkAndRetrieveFromCache(networkRequest: NetworkRequest, responseHandler: (InputStream) -> Unit): Result<String>? {
        when (networkRequest.cacheStrategy) {
            CacheStrategy.None -> return null
            CacheStrategy.InMemory -> InMemoryCache.getAsync(networkRequest.generateCacheKey())?.let {
                if (networkRequest.isImageRequest == true) {
                    InMemoryImageCache.getAsync(networkRequest.generateCacheKey())?.let {
                        responseHandler.invoke(it)
                    }
                } else {
                    InMemoryCache.getAsync(networkRequest.generateCacheKey())?.let {
                        return Result.success(it)
                    }
                }
            }
            is CacheStrategy.NetworkCacheControl -> {
                // As you are already handling cache-control headers in the NetworkRequest,
                // you can leave this case empty, unless you have additional logic you'd like to implement here.
            }
        }
        return null
    }

}
