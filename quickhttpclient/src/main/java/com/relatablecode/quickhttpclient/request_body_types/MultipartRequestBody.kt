package com.relatablecode.quickhttpclient.request_body_types

import com.relatablecode.quickhttpclient.logging_utils.Logger
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.URLConnection


/**
 * Represents a multipart request body containing both text and file data.
 *
 * @property textData The text data to be sent as part of the multipart request.
 * @property fileData The file data to be sent as part of the multipart request.
 */
class MultipartRequestBody(
    private val textData: Map<String, String>,
    private val fileData: Map<String, File>
) : RequestBody {

    private val boundary = "===" + System.currentTimeMillis() + "==="
    private val LINE_FEED = "\r\n"
    private var progressListener: UploadProgressListener? = null
    private var totalBytesRead: Long = 0
    private val totalSize: Long = fileData.values.sumOf { it.length() }

    override fun content(): Any {
        return Pair(textData, fileData)
    }

    override fun contentType() = "multipart/form-data; boundary=$boundary"

    override fun writeTo(outputStream: OutputStream) {

        if (!validateFiles()) {
            Logger.e("MultipartRequestBody", "Some files are either missing or not readable.")
            return
        }

        val writer = PrintWriter(OutputStreamWriter(outputStream, "UTF-8"), true)

        // Send normal parameters
        textData.forEach { (key, value) ->
            writer.append("--$boundary").append(LINE_FEED)
            writer.append("Content-Disposition: form-data; name=\"$key\"").append(LINE_FEED)
            writer.append("Content-Type: text/plain; charset=UTF-8").append(LINE_FEED)
            writer.append(LINE_FEED)
            writer.append(value).append(LINE_FEED)
        }

        // Send binary file data
        fileData.forEach { (key, file) ->
            writer.append("--$boundary").append(LINE_FEED)
            writer.append("Content-Disposition: form-data; name=\"$key\"; filename=\"${file.name}\"").append(LINE_FEED)
            writer.append("Content-Type: ${URLConnection.guessContentTypeFromName(file.name)}").append(LINE_FEED)
            writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED)
            writer.append(LINE_FEED)
            writer.flush()

            FileInputStream(file).use { inputStream ->
                val buffer = ByteArray(4096)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    val progress = (totalBytesRead.toDouble() / totalSize * 100).toInt()
                    progressListener?.onProgressUpdate(progress)
                }
                outputStream.flush()
            }

            writer.append(LINE_FEED)
        }

        writer.append("--$boundary--").append(LINE_FEED)
        writer.close()
    }

    private fun validateFiles(): Boolean {
        return fileData.values.all { it.exists() && it.canRead() }
    }

    fun setUploadProgressListener(listener: UploadProgressListener) {
        this.progressListener = listener
    }

}
