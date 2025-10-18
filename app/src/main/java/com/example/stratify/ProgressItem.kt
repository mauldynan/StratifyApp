package com.example.stratify

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProgressItem(
    val text: String,
    val date: String
) : Parcelable