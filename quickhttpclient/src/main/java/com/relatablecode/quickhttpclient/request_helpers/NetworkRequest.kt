package com.relatablecode.quickhttpclient.request_helpers

import com.relatablecode.quickhttpclient.cache_helpers.CacheStrategy
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
 * @param cacheStrategy Choose the caching strategy for this request.
 * @param retryCount The number of times to retry the request in case of failures.
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
    var cacheStrategy: CacheStrategy = CacheStrategy.None,
    var retryCount: Int? = null,
    var isImageRequest: Boolean? = false,
    var imageCacheDuration: Long = InMemoryImageCache.DEFAULT_TTL
) {
    /**
     * Generates a cache key based on the current request properties.
     *
     * Note: This method can be extended or modified to generate a more sophisticated key if needed.
     */
    fun generateCacheKey(): String {
        return "${requestMethod.name}::$endpoint?${params.joinToString("&")}"
    }

    /**
     * Update headers based on cache strategy.
     */
    init {
        when (cacheStrategy) {
            is CacheStrategy.NetworkCacheControl -> {
                val cacheControlValue = buildString {
                    append(if ((cacheStrategy as CacheStrategy.NetworkCacheControl).isPublic) "public" else "private")
                    append(", max-age=${(cacheStrategy as CacheStrategy.NetworkCacheControl).maxAge}")
                }
                headers.add(RequestHeader("Cache-Control", cacheControlValue))
            }
            else -> { /* Other cache strategies can add their specific headers here if needed. */ }
        }
    }

}
