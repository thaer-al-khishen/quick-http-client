package com.relatablecode.quickhttpclient.cache_helpers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

/**
 * An in-memory cache specifically designed for caching image streams.
 */
object InMemoryImageCache {

    var maxSize: Int = 100 // default max size
    const val DEFAULT_TTL = 60_000L   // 60 seconds

    private val cache = object : LinkedHashMap<String, Pair<InputStream, Long>>(maxSize, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Pair<InputStream, Long>>): Boolean {
            val currentTime = System.currentTimeMillis()
            val isExpired = eldest.value.second < currentTime
            return this.size > maxSize || isExpired
        }
    }

    @Synchronized
    fun put(key: String, value: InputStream, ttl: Long = DEFAULT_TTL) {
        val expiryTime = System.currentTimeMillis() + ttl
        cache[key] = Pair(value, expiryTime)
    }

    @Synchronized
    fun get(key: String): InputStream? {
        val currentTime = System.currentTimeMillis()
        return cache[key]?.let {
            if (it.second >= currentTime) it.first else null
        }
    }

    @Synchronized
    fun invalidate(key: String) {
        cache.remove(key)
    }

    @Synchronized
    fun invalidateAll() {
        cache.clear()
    }

    suspend fun putAsync(key: String, value: InputStream, ttl: Long = DEFAULT_TTL) = withContext(Dispatchers.IO) {
        put(key, value, ttl)
    }

    suspend fun getAsync(key: String): InputStream? = withContext(Dispatchers.IO) {
        get(key)
    }

    suspend fun invalidateAsync(key: String) = withContext(Dispatchers.IO) {
        invalidate(key)
    }

    suspend fun invalidateAllAsync() = withContext(Dispatchers.IO) {
        invalidateAll()
    }

}