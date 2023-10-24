package com.relatablecode.quickhttpclient.request_helpers

/**
 * Helper object to build requests with query parameters.
 */
object RequestBuilder {

    /**
     * Builds a URL with query parameters.
     *
     * @param base The base URL.
     * @param params A list of query parameters to be appended to the base URL.
     * @return The URL constructed with the provided query parameters.
     */
    fun buildWithQueryParameters(base: String, params: List<QueryParameter> = listOf()): String {
        val stringBuilder = StringBuilder(base)
        if (params.isNotEmpty()) {
            stringBuilder.append("?")
            params.forEachIndexed { index, param ->
                stringBuilder.append("${param.key}=${param.value}")
                if (index != params.size - 1) {
                    stringBuilder.append("&")
                }
            }
        }
        return stringBuilder.toString()
    }

}
