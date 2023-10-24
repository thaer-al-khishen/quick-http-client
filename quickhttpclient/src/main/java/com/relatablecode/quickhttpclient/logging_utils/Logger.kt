package com.relatablecode.quickhttpclient.logging_utils

import android.util.Log

/**
 * A utility logger to aid in logging messages of different levels.
 */
object Logger {

    var condition: Boolean = true

    fun d(tag: String, message: String) {
        if (condition) {
            Log.d(tag, message)
        }
    }

    fun e(tag: String, message: String) {
        if (condition) {
            Log.e(tag, message)
        }
    }
    // ... add methods for other log levels
}
