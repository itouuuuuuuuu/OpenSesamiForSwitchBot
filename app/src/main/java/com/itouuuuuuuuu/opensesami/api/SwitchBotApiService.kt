package com.itouuuuuuuuu.opensesami.api

import android.content.Context
import android.content.SharedPreferences
import com.itouuuuuuuuu.opensesami.SharedPreferencesManager
import com.itouuuuuuuuu.opensesami.model.SwitchBotPressRequest
import com.itouuuuuuuuu.opensesami.model.SwitchBotPressResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface ISwitchBotApiService {
    @POST("/v1.0/devices/{deviceId}/commands")
    fun press(@Path("deviceId") deviceId : String?, @Body switchBotPressRequest: SwitchBotPressRequest = SwitchBotPressRequest()): Call<SwitchBotPressResponse>
}

class SwitchBotApiService {
    private lateinit var prefs: SharedPreferencesManager

    companion object {
        private const val API_BASE_URL = "https://api.switch-bot.com/"
    }

    fun createService(context: Context): ISwitchBotApiService {
        prefs = SharedPreferencesManager(context)

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
            .baseUrl(API_BASE_URL)
            .client(httpBuilder.build())
            .build()

        return retrofit.create(ISwitchBotApiService::class.java)
    }

    private val httpBuilder: OkHttpClient.Builder
        get() {
            val httpClient = OkHttpClient.Builder()
                .addInterceptor(Interceptor { chain ->
                    val original = chain.request()
                    val request = original.newBuilder()
                        .header("Authorization", prefs.apiToken ?: "")
                        .addHeader("Content-Type", "application/json; charset=utf8")
                        .method(original.method, original.body)
                        .build()
                    return@Interceptor chain.proceed(request)
                })
                .readTimeout(30, TimeUnit.SECONDS)

            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            httpClient.addInterceptor(loggingInterceptor)

            return httpClient
        }
}