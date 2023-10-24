package com.relatablecode.quickhttpclient.network_manager

import com.relatablecode.quickhttpclient.logging_utils.Logger
import com.relatablecode.quickhttpclient.network_interactors.RawHttpClient
import com.relatablecode.quickhttpclient.quickhttpclient.QuickHttpClient
import com.relatablecode.quickhttpclient.request_helpers.NetworkRequest
import com.relatablecode.quickhttpclient.request_helpers.RequestBuilder
import com.relatablecode.quickhttpclient.response_helpers.ApiResponseHandler


/**
 * A utility class for managing network operations and interactions with the server.
 */
object NetworkManager {

    /**
     * Makes a network call using the given [networkRequest] with the baseUrl.
     *
     * @param networkRequest The request to send.
     * @return A [Result] containing the response data or an error.
     */
    suspend inline fun <reified T> makeNetworkCall(
        networkRequest: NetworkRequest
    ): Result<T> {
        QuickHttpClient.quickHttpInstance?.let {
            val fullUrl =
                RequestBuilder.buildWithQueryParameters("${QuickHttpClient.baseUrl ?: ""}${networkRequest.endpoint}", networkRequest.params)
            return makeNetworkCallWithFullUrl(
                networkRequest.copy(
                    endpoint = fullUrl
                )
            )
        } ?: run {
            Logger.e("QuickHttpClient", "QuickHttpClient is not initialized")
            return Result.failure(Throwable("QuickHttpClient is not initialized"))
        }

    }

    /**
     * Makes a network call using the given [networkRequest] with a fully formed URL without the baseUrl.
     *
     * @param networkRequest The request to send.
     * @return A [Result] containing the response data or an error.
     */
    suspend inline fun <reified T> makeNetworkCallWithFullUrl(
        networkRequest: NetworkRequest
    ): Result<T> {
        QuickHttpClient.quickHttpInstance?.let {
            val urlWithParams = RequestBuilder.buildWithQueryParameters(networkRequest.endpoint, networkRequest.params)
            val jsonResult = RawHttpClient.makeNetworkRequest(
                networkRequest.copy(
                    endpoint = urlWithParams
                )
            )
            return with(jsonResult) {
                ApiResponseHandler.parseJson<T>(this.getOrElse {
                    it.message.toString()
                }).onFailure {
                    Logger.e("Network Error", "Network Error: $it")
                }.onSuccess {
                    Logger.d("Network Success", "$it")
                }
            }
        } ?: run {
            return Result.failure(Throwable("QuickHttpClient is not initialized"))
        }

    }

}
