package com.relatablecode.quickhttpclient.request_body_types

import kotlinx.serialization.Serializable
import java.io.OutputStream

/**
 * Represents a multipart request body containing both text and file data.
 *
 * @property textData The text data to be sent as part of the multipart request.
 * @property fileData The file data to be sent as part of the multipart request.
 */
@Serializable
class NormalRequestBody(
    private val content: String
) : RequestBody {

    override fun content(): Any {
        return content
    }

    override fun contentType() = "application/json"

    override fun writeTo(outputStream: OutputStream) {
        val bytes = content.toByteArray(Charsets.UTF_8)
        outputStream.write(bytes, 0, bytes.size)
    }

}
