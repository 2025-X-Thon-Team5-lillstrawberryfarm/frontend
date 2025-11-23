package com.example.myapp.ui.data.api

import android.content.Context
import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // ★ 실제 백엔드 서버 주소 적용
    private const val BASE_URL = "https://backend-service-926962939300.asia-northeast3.run.app/"

    private var authToken: String? = null

    // 토큰 설정 함수 (로그인 성공 시 호출하거나 앱 시작 시 호출)
    fun setAuthToken(token: String) {
        authToken = token
    }

    // ★ 인증 인터셉터: 모든 요청 헤더에 토큰을 자동으로 넣어줍니다.
    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()

        // 토큰이 있으면 헤더에 추가
        if (!authToken.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $authToken")
        }

        val request = requestBuilder.build()
        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor) // ★ 인터셉터 추가
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    // 앱 시작 시 SharedPreferences에서 토큰을 불러와 설정하는 헬퍼 함수
    fun initToken(context: Context) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        authToken = prefs.getString("access_token", null)
    }
}