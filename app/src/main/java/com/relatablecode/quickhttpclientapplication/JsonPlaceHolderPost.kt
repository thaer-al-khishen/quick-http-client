package com.relatablecode.quickhttpclientapplication

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JsonPlaceHolderPost(
    val userId: Int?,
    val id: Int?,
    @SerialName("title")
    val title: String?,
    val body: String?
)
