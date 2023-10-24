package com.relatablecode.quickhttpclient.request_helpers

/**
 * Enum representing the possible HTTP methods for a request.
 *
 * @param key The string representation of the HTTP method.
 */
enum class RequestMethod(val key: String) {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    PATCH("PATCH"),
    DELETE("DELETE")
}
