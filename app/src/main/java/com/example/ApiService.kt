package com.example.stratify.network

import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("reviews/{app_name}")
    suspend fun getReviews(@Path("app_name") appName: String): List<ReviewResponse>
}