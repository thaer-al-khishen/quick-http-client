package com.relatablecode.quickhttpclient.request_helpers

import com.relatablecode.quickhttpclient.request_body_types.NormalRequestBody
import com.relatablecode.quickhttpclient.request_body_types.RequestBody
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Utility object for JSON serialization and deserialization.
 */
object JsonUtils {

    val json by lazy {
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = false
            prettyPrint = true
        }
    }

    /**
     * Serializes the provided content to JSON.
     *
     * @param content The content to serialize.
     * @return The JSON representation of the content.
     */
    inline fun <reified T> T.toJson() = json.encodeToString(this)

    /**
     * Deserializes the provided JSON string to an object.
     *
     * @param json The JSON string to deserialize.
     * @return The deserialized object.
     */
    inline fun <reified T> String.toObject() = json.decodeFromString<T>(this)

    /**
     * Constructs a [NormalRequestBody] with the provided content serialized as JSON.
     *
     * @param content The content to serialize and wrap in a [NormalRequestBody].
     * @return A [NormalRequestBody] with the provided content serialized as JSON.
     */
    inline fun <reified T> makeRequestBody(content: T): RequestBody {
        return NormalRequestBody(
            content.toJson()
        )
    }

}
