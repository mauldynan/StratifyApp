package com.example.stratify.compare

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ReviewRepository {

    private val db = FirebaseFirestore.getInstance()

    fun getLatestReviews(
        collectionName: String,
        onResult: (List<Review>) -> Unit
    ) {
        db.collection(collectionName)
            .orderBy("at", Query.Direction.DESCENDING)
            .limit(3)
            .get()
            .addOnSuccessListener { snapshot ->
                Log.d("FIREBASE", "$collectionName SIZE = ${snapshot.size()}")

                val reviews = snapshot.documents.mapNotNull {
                    Review(
                        userName = it.getString("userName") ?: "",
                        content = it.getString("content") ?: ""
                    )
                }
                onResult(reviews)
            }
            .addOnFailureListener {
                Log.e("FIREBASE", "ERROR $collectionName", it)
            }
    }
}

