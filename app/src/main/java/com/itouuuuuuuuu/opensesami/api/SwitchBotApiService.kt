package com.itouuuuuuuuu.opensesami.api

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
    companion object {
        private const val DEVICE_ID = "FA98475977EC"
    }

    @POST("/v1.0/devices/$DEVICE_ID/commands")
    fun press(@Body switchBotPressRequest: SwitchBotPressRequest = SwitchBotPressRequest()): Call<SwitchBotPressResponse>
}

class SwitchBotApiService {

    companion object {
        private const val API_BASE_URL = "https://api.switch-bot.com/"
        private const val API_TOKEN = "88f4e93e2e6c0b060fd8b29052583fce7df5a4c0d0db9ac53bcc95c5780b249790b59a4250b4f8b591490ee58587fa02"
    }

    fun createService(): ISwitchBotApiService {
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
                        .header("Authorization", API_TOKEN)
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