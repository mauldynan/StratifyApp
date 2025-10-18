package com.example.stratify

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Workspace(
    val id: String,
    val name: String,
    val creatorName: String,
    val password: String,
    var status: String = "To Do",
    var department: String = "",
    val members: ArrayList<String>,
    var details: String = ""
) : Parcelable