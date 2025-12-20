package com.example.stratify.compare

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class DashboardViewModel : ViewModel() {

    private val repo = ReviewRepository()

    var shopeeReviews = mutableStateOf<List<Review>>(emptyList())
        private set

    var tokopediaReviews = mutableStateOf<List<Review>>(emptyList())
        private set

    fun loadReviews() {
        repo.getLatestReviews("shopee") {
            shopeeReviews.value = it
        }

        repo.getLatestReviews("tokopedia") {
            tokopediaReviews.value = it
        }
    }
}
