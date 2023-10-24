package com.relatablecode.quickhttpclient.network_interactors


import com.relatablecode.quickhttpclient.logging_utils.Logger
import com.relatablecode.quickhttpclient.request_helpers.NetworkRequest
import java.net.HttpURLConnection

/**
 * Generates a cache key for the given [NetworkRequest].
 *
 * @return A string representing the cache key for this request.
 */
fun NetworkRequest.generateCacheKey(): String {
    val method = requestMethod.name
    val headerKeys = headers.joinToString(",") { it.key }
    val paramKeys = params.joinToString(",") { it.key }
    return "$method:$endpoint:$headerKeys:$paramKeys"
}

/**
 * Logs the details of the given [NetworkRequest] and [HttpURLConnection] for debugging purposes.
 *
 * @param networkRequest The network request to be logged.
 * @param urlConnection The HttpURLConnection object associated with the request.
 */
fun logRequest(networkRequest: NetworkRequest, urlConnection: HttpURLConnection) {
    Logger.d(
        RawHttpClient.TAG,
        "=============================================================================================================================="
    )
    Logger.d(RawHttpClient.TAG, "Request URL: ${networkRequest.endpoint}")
    Logger.d(RawHttpClient.TAG, "Request Method: ${networkRequest.requestMethod.key}")
    Logger.d(RawHttpClient.TAG, "Request Headers:")
    urlConnection.requestProperties.forEach { header ->
        Logger.d(RawHttpClient.TAG, "Request Header: ${header.key} = ${header.value}")
    }
    networkRequest.body?.let {
        Logger.d(RawHttpClient.TAG, "Request Body: ${it.contentType()}")
    }
}

/**
 * Logs the details of the response received through the given [HttpURLConnection].
 *
 * @param urlConnection The HttpURLConnection object from which the response was received.
 * @param response The response string to be logged.
 */
fun logResponse(urlConnection: HttpURLConnection, response: String) {
    Logger.d(
        RawHttpClient.TAG,
        "*.*_*.*.*_*.*_*.*.*_*.*_*.*.*_*.*_*.*.*_*.*_*.*.*_*.*_*.*.*_*.*_*.*.*_*.*_*.*.*_*.*_*.*.*_*.*_*.*.*_*.*_*.*.*_*"
    )

    val responseHeaders = urlConnection.headerFields
    Logger.d(RawHttpClient.TAG, "Response Headers:")
    responseHeaders.forEach { (key, values) ->
        values.forEach { value ->
            Logger.d(RawHttpClient.TAG, "$key: $value")
        }
    }

    Logger.d(RawHttpClient.TAG, "Response Body:")
    Logger.d(RawHttpClient.TAG, "Response: $response")
    Logger.d(
        RawHttpClient.TAG,
        "======================================================================================================================================"
    )
}
