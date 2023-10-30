package com.relatablecode.quickhttpclient.cache_helpers

sealed class CacheStrategy {
    data object None : CacheStrategy()
    data object InMemory : CacheStrategy()
    data class NetworkCacheControl(val maxAge: Int, val isPublic: Boolean = true) : CacheStrategy()
}
