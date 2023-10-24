package com.relatablecode.quickhttpclient.interceptors

import com.relatablecode.quickhttpclient.request_helpers.RequestHeader


/**
 * A class to represent a modifier that can be used to modify network requests before sending them.
 */
class RequestModifier {

    /**
     * A mutable list of request headers that can be added to a network request.
     */
    var headers = mutableListOf<RequestHeader>()
        private set

    /**
     * The maximum time to wait while connecting to the server (in milliseconds).
     */
    var connectTimeout: Int? = null
        private set

    /**
     * The maximum time to wait for reading data from the server after the connection is established (in milliseconds).
     */
    var readTimeout: Int? = null
        private set

    /**
     * The number of times the request should be retried in case of failure.
     */
    var retryCount: Int? = null
        private set

    /**
     * The endpoint to which the request should be sent.
     */
    var endpoint: String? = null

    /**
     * Adds a [requestHeader] to the list of headers.
     *
     * @param requestHeader The header to add.
     */
    fun addHeader(requestHeader: RequestHeader) {
        headers.add(requestHeader)
    }

    /**
     * Adds multiple [requestHeaders] to the list of headers.
     *
     * @param requestHeaders The list of headers to add.
     */
    fun addHeaders(requestHeaders: List<RequestHeader>) {
        requestHeaders.forEach {
            headers.add(it)
        }
    }

    /**
     * Sets the connection timeout.
     *
     * @param timeout The timeout value in milliseconds.
     */
    fun setConnectTimeout(timeout: Int) {
        connectTimeout = timeout
    }

    /**
     * Sets the read timeout.
     *
     * @param timeout The timeout value in milliseconds.
     */
    fun setReadTimeout(timeout: Int) {
        readTimeout = timeout
    }

    /**
     * Sets the retry count.
     *
     * @param count The number of retries.
     */
    fun setRetryCount(count: Int) {
        retryCount = count
    }

}
