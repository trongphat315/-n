package com.example.doan.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Đổi thành false và cập nhật BASE_URL khi kết nối máy chủ thật
    const val USE_MOCK_DATA = true
    private const val BASE_URL = "https://ten-mien-api-cua-ban.com/api/"

    var authToken: String? = null
    var currentUserId: String = "CPN_01"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val builder = chain.request().newBuilder()
                    .addHeader("Accept", "application/json")

                authToken?.takeIf { it.isNotBlank() }?.let { token ->
                    builder.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(builder.build())
            }
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val topicApi: TopicApi by lazy { retrofit.create(TopicApi::class.java) }
    val battleApi: BattleApi by lazy { retrofit.create(BattleApi::class.java) }
}