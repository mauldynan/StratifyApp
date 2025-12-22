package com.example.stratify.network

data class ReviewResponse(
    val username: String,
    val content: String,
    val score: Int,
    val date: String
)