package com.relatablecode.quickhttpclient.request_body_types

interface UploadProgressListener {
    fun onProgressUpdate(percentage: Int)
}
