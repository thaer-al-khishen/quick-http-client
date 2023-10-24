package com.relatablecode.quickhttpclient.interceptors

import com.relatablecode.quickhttpclient.request_helpers.NetworkRequest

/**
 * Modifies the current [NetworkRequest] with the given [requestModifier].
 *
 * @param requestModifier The request modifier containing changes to be applied to this request.
 */
fun NetworkRequest.modifyWith(requestModifier: RequestModifier) {

    if (requestModifier.headers.isEmpty()) {
        requestModifier.headers.forEach {
            this.headers.add(it)
        }
    }

    requestModifier.connectTimeout?.let {
        this.connectTimeout = it
    }

    requestModifier.readTimeout?.let {
        this.readTimeout = it
    }

    requestModifier.retryCount?.let {
        this.retryCount = it
    }

}
