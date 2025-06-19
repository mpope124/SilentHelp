package com.silenthelp.repository
//Added by Michael

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.silenthelp.api.ApiService

object IncidentRepository {
    private const val BASE_URL = "http://10.0.2.2:5000/api/" // for Android emulator

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

}