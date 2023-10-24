package com.relatablecode.quickhttpclient.interceptors

import com.relatablecode.quickhttpclient.request_helpers.NetworkRequest

/**
 * Represents an interceptor that allows modification of the network request before sending.
 */
interface RequestInterceptor {
    fun intercept(networkRequest: NetworkRequest?): RequestModifier
}
