package com.example.stratify

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Member(
    val userId: String = "",
    val name: String = "",
    val photoUrl: String? = null,
    val isYou: Boolean = false
) : Parcelable
