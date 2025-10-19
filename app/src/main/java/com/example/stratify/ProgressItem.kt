package com.example.stratify

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class ProgressItem(
    @DocumentId
    val id: String = "",
    val text: String = "",
    val createdBy: String = "",  // User name
    val createdById: String = "",  // User ID
    @ServerTimestamp
    val createdAt: Date? = null,
    val dateText: String = ""  // Formatted date string (for display)
) : Parcelable {

    // No-arg constructor untuk Firestore
    constructor() : this(
        id = "",
        text = "",
        createdBy = "",
        createdById = "",
        createdAt = null,
        dateText = ""
    )
}