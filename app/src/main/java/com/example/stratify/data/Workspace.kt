package com.example.stratify.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Workspace(
    val id: String = "",
    val name: String = ""
    // Add other relevant fields for a workspace here
) : Parcelable
