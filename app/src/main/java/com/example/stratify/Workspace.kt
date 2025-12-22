package com.example.stratify

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

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

    val members: @RawValue ArrayList<String> = arrayListOf(),
    val memberIds: @RawValue ArrayList<String> = arrayListOf(),
    val memberPhotos: @RawValue HashMap<String, String> = hashMapOf(),

    var details: String = "",

    val tasks: @RawValue ArrayList<WorkspaceTask> = arrayListOf(),

    val createdAt: @RawValue Any? = null,

    val updatedAt: Long = System.currentTimeMillis(),
    var isJoined: Boolean = false
) : Parcelable {
    constructor() : this(id = "")
}

@Parcelize
data class WorkspaceTask(
    val id: String = "",
    val title: String = "",
    val description: String = "",

    val attachmentUri: String? = null,

    val isCompleted: Boolean = false
) : Parcelable