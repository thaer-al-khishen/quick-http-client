package com.relatablecode.quickhttpclient.response_helpers

import com.relatablecode.quickhttpclient.logging_utils.Logger
import com.relatablecode.quickhttpclient.request_helpers.JsonUtils.toObject

/**
 * A helper object for processing and handling API responses.
 */
object ApiResponseHandler {

    /**
     * Parses a JSON response into a specified data type.
     * @param json The JSON string to be parsed.
     * @return Returns a Result object containing the parsed data or any exception that occurred during parsing.
     */
    inline fun <reified T> parseJson(json: String): Result<T> {
        return runCatching<ApiResponseHandler, T> {
            json.toObject()
        }.onFailure {
            Logger.d("Network Error", "Network Error: $it")
        }.onSuccess {
            Logger.d("Network Success", "$it")
        }
    }

}
