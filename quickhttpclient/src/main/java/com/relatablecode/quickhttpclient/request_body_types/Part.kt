package com.relatablecode.quickhttpclient.request_body_types

/**
 * Represents a part of a multipart request.
 *
 * @property body The request body of the part.
 */
data class Part(
    val body: RequestBody
)
