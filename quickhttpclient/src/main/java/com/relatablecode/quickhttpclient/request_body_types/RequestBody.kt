package com.relatablecode.quickhttpclient.request_body_types

import java.io.OutputStream

/**
 * Represents the base contract for request body types.
 */
interface RequestBody {
    fun contentType(): String
    fun content(): Any
    fun writeTo(outputStream: OutputStream)
}
