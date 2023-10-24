package com.relatablecode.quickhttpclient.request_body_types

import java.io.OutputStream
import java.net.URLEncoder

/**
 * Represents a request body containing form data.
 *
 * @property formData The form data to be sent as the request body.
 */
class FormRequestBody(private val formData: Map<String, String>) : RequestBody {

    override fun content(): Any {
        return formData
    }

    override fun contentType() = "application/x-www-form-urlencoded"

    override fun writeTo(outputStream: OutputStream) {
        val encodedData = formData.entries.joinToString("&") { (key, value) ->
            "${URLEncoder.encode(key, "UTF-8")}=${URLEncoder.encode(value, "UTF-8")}"
        }
        outputStream.write(encodedData.toByteArray(Charsets.UTF_8))
    }

}
