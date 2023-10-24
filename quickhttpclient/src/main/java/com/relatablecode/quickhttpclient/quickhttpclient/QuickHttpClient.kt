package com.relatablecode.quickhttpclient.quickhttpclient

import com.relatablecode.quickhttpclient.interceptors.RequestInterceptor
import com.relatablecode.quickhttpclient.interceptors.RequestModifier
import com.relatablecode.quickhttpclient.logging_utils.Logger
import com.relatablecode.quickhttpclient.request_helpers.NetworkRequest
import com.relatablecode.quickhttpclient.ssl_helpers.SSLPinningMethod


/**
 * A helper object for easily setting up and interacting with the QuickHttpClient.
 */
object QuickHttpClient {

    private val initializationLock = Any()

    /**
     * The base URL to be used for the HTTP requests.
     */
    var baseUrl: String? = null
        private set

    /**
     * The main instance of the QuickHttpClient.
     */
    var quickHttpInstance: QuickHttpInstance? = null
        private set

    internal var requestModifier: RequestModifier? = null

    /**
     * A list of interceptors that can modify the requests.
     */
    internal var requestInterceptors: MutableList<RequestInterceptor> = mutableListOf()
        private set

    private var networkRequest: NetworkRequest? = null

    @Volatile
    internal var publicKeys = listOf<String>()
        private set

    @Volatile
    internal var certificates = listOf<String>()
        private set

    @Volatile
    internal var hostnameVerification = Pair(false, "")
        private set

    /**
     * Configures the base URL for the HTTP client.
     * @param _baseUrl The base URL to be set.
     * @return Returns the QuickHttpClient instance for method chaining.
     */
    fun withBaseUrl(_baseUrl: String): QuickHttpClient {
        baseUrl = _baseUrl
        return this
    }

    /**
     * Adds a request interceptor to the HTTP client.
     * @param _requestInterceptor The interceptor to be added.
     * @return Returns the QuickHttpClient instance for method chaining.
     */
    fun addRequestInterceptor(_requestInterceptor: RequestInterceptor): QuickHttpClient {
        requestInterceptors.add(_requestInterceptor)
        return this
    }

    /**
     * Initializes the QuickHttpClient. This method should be called once during the app's lifecycle.
     * @return Returns the QuickHttpClient instance.
     * @throws IllegalStateException If the client is already initialized.
     */
    fun initialize(): QuickHttpClient {
        synchronized(initializationLock) {
            if (quickHttpInstance == null) {
                quickHttpInstance = QuickHttpInstance()
            } else {
                throw IllegalStateException("SmartHttpClient is already initialized.")
            }
        }
        return this
    }

    internal fun setNetworkRequest(_networkRequest: NetworkRequest?) {
        networkRequest = _networkRequest
    }

    /**
     * Configures SSL pinning for the HTTP client.
     * @param sslPinningMethod The method to use for SSL pinning.
     * @return Returns the QuickHttpClient instance for method chaining.
     */
    fun configureSSLPinning(sslPinningMethod: SSLPinningMethod): QuickHttpClient {
        when (sslPinningMethod) {
            is SSLPinningMethod.PublicKeyPinning -> {
                publicKeys = sslPinningMethod.publicKeys
                certificates = listOf()
            }
            is SSLPinningMethod.CertificatesPinning -> {
                publicKeys = listOf()
                certificates = sslPinningMethod.certificates
            }
        }
        return this
    }

    /**
     * Configures hostname verification for the HTTP client.
     * @param hostname The hostname to verify against.
     * @return Returns the QuickHttpClient instance for method chaining.
     */
    fun configureHostNameVerification(hostname: String): QuickHttpClient {
        hostnameVerification = Pair(true, hostname)
        return this
    }

    /**
     * Configures logging for http requests.
     * @param loggingLevel The level of logging to use for http requests, NONE by default.
     * @return Returns the QuickHttpClient instance for method chaining.
     */
    fun setLoggingCondition(loggingCondition: Boolean): QuickHttpClient {
        Logger.condition = loggingCondition
        return this
    }

}
