package com.example.stratify.ui.workspace

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class ProgressDetail(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable
