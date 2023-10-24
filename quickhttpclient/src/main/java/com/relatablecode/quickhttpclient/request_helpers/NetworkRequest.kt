package com.relatablecode.quickhttpclient.request_helpers

import com.relatablecode.quickhttpclient.cache_helpers.InMemoryImageCache
import com.relatablecode.quickhttpclient.request_body_types.RequestBody

/**
 * Represents a network request with its configuration details.
 *
 * @param endpoint The endpoint for the request.
 * @param requestMethod The HTTP method for the request.
 * @param body The request body, if any.
 * @param headers A list of headers to be sent with the request.
 * @param params A list of query parameters for the request.
 * @param connectTimeout The connection timeout for the request.
 * @param readTimeout The read timeout for the request.
 * @param shouldCache Indicates if the request response should be cached.
 * @param retryCount The number of times to retry the request in case of failures.
 * @param cacheImage Indicates if the image response should be cached.
 * @param imageCacheDuration Duration in milliseconds for which the image response should be cached.
 */
data class NetworkRequest(
    val endpoint: String,
    val requestMethod: RequestMethod,
    var body: RequestBody? = null,
    var headers: MutableList<RequestHeader> = mutableListOf(),
    var params: List<QueryParameter> = listOf(),
    var connectTimeout: Int? = null,
    var readTimeout: Int? = null,
    var shouldCache: Boolean = false,
    var retryCount: Int? = null,  // Number of times to retry
    var cacheImage: Boolean? = null,
    var imageCacheDuration: Long = InMemoryImageCache.DEFAULT_TTL
)
