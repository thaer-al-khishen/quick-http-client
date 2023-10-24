package com.relatablecode.quickhttpclient.cache_helpers


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * An in-memory cache for storing key-value pairs with a defined time-to-live (TTL).
 */
object InMemoryCache {

    var maxSize: Int = 100 // default max size
    const val DEFAULT_TTL = 60_000L   // 60 seconds

    private val cache = object : LinkedHashMap<String, Pair<String, Long>>(maxSize, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Pair<String, Long>>): Boolean {
            val currentTime = System.currentTimeMillis()
            val isExpired = eldest.value.second < currentTime
            return this.size > maxSize || isExpired
        }
    }

    @Synchronized
    fun put(key: String, value: String, ttl: Long = DEFAULT_TTL) {
        val expiryTime = System.currentTimeMillis() + ttl
        cache[key] = Pair(value, expiryTime)
    }

    @Synchronized
    fun get(key: String): String? {
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

    suspend fun putAsync(key: String, value: String) = withContext(Dispatchers.IO) {
        put(key, value)
    }

    suspend fun getAsync(key: String): String? = withContext(Dispatchers.IO) {
        get(key)
    }

    suspend fun invalidateAsync(key: String) = withContext(Dispatchers.IO) {
        invalidate(key)
    }

    suspend fun invalidateAllAsync() = withContext(Dispatchers.IO) {
        invalidateAll()
    }

}
