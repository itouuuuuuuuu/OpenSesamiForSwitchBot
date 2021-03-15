package com.itouuuuuuuuu.opensesami

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory


class SharedPreferencesManager(context: Context) {

    companion object {
        private const val TAKE_PHOTO_INTERVAL_HASH = "d687fa5a597b6b0d4a885ab933813eee86d1b9d52b02a9c945ee0f92cc7b29d7"
        private const val MAX_RETRY_COUNT_HASH = "be6a2c41b98c387c07c12963f42ab886ad14e8de85775cd236359dfc3c44ae78"
        private const val CONFIDENCE_THRESHOLD_HASH = "089dd3f5d909a2102830a8f2f0c40fec9047d493fc760a04401586afbbae038a"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
    private val editor = prefs.edit()

    var takePhotoInterval: Long
        get() = prefs.getLong(TAKE_PHOTO_INTERVAL_HASH, 2500)
        set(value) {
            editor.apply {
                putLong(TAKE_PHOTO_INTERVAL_HASH, value)
                apply()
            }
        }

    var maxRetryCount: Int
        get() = prefs.getInt(MAX_RETRY_COUNT_HASH, 5)
        set(value) {
            editor.apply {
                putInt(MAX_RETRY_COUNT_HASH, value)
                apply()
            }
        }

    var confidenceThreshold: Int
        get() = prefs.getInt(CONFIDENCE_THRESHOLD_HASH, 80)
        set(value) {
            editor.apply {
                putInt(CONFIDENCE_THRESHOLD_HASH, value)
                apply()
            }
        }
}