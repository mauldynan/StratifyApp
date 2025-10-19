package com.example.stratify

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Workspace(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val creatorName: String = "",
    val creatorId: String = "",
    val password: String = "",
    var status: String = "To Do",
    var department: String = "",
    val members: ArrayList<String> = arrayListOf(),
    val memberIds: ArrayList<String> = arrayListOf(),
    val memberPhotos: HashMap<String, String> = hashMapOf(),
    var details: String = "",
    @ServerTimestamp
    val createdAt: Date? = null,
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable {

    constructor() : this(
        id = "",
        name = "",
        creatorName = "",
        creatorId = "",
        password = "",
        status = "To Do",
        department = "",
        members = arrayListOf(),
        memberIds = arrayListOf(),
        memberPhotos = hashMapOf(),
        details = "",
        createdAt = null,
        updatedAt = System.currentTimeMillis()
    )
}