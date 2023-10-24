package com.relatablecode.quickhttpclient.request_body_types

import java.io.File
import java.io.OutputStream

/**
 * Represents a request body containing a file.
 *
 * @property file The file to be sent as the request body.
 */
class FileRequestBody(private val file: File) : RequestBody {

    override fun contentType() = "application/octet-stream"

    override fun content(): Any {
        return file
    }

    override fun writeTo(outputStream: OutputStream) {
        file.inputStream().use { inputStream ->
            val buffer = ByteArray(8 * 1024)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
        }
    }

}
