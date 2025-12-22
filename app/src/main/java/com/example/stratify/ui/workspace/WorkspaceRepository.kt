package com.example.stratify.ui.workspace

import com.example.stratify.Workspace
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class WorkspaceRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getUserWorkspaces(): List<Workspace> {
        val uid = auth.currentUser?.uid ?: return emptyList()

        // 1. Ambil semua workspace yang user join
        val snapshot = db.collection("workspaces")
            .whereArrayContains("memberIds", uid)
            .get()
            .await()

        // 2. Mapping Firestore → Workspace
        return snapshot.documents.map { doc ->

            // ===== AMBIL PROGRESS DETAILS =====
            val progress = doc.get("progressDetails") as? List<Map<String, Any>> ?: emptyList()

            val progressList = progress.map {
                ProgressDetail(
                    id = it["id"] as String,
                    text = it["text"] as String,
                    createdAt = (it["createdAt"] as Number).toLong()
                )
            }

            // ===== BALIKIN WORKSPACE =====
            Workspace(
                id = doc.id,
                name = doc.getString("name") ?: "",
                password = doc.getString("password") ?: "",
                status = doc.getString("status") ?: "To Do",
                members = doc.get("members") as? ArrayList<String> ?: arrayListOf(),
                progressDetails = ArrayList(progressList),
                isJoined = true
            )
        }
    }


    suspend fun createWorkspace(
        name: String,
        code: String,
        password: String
    ) {
        val uid = auth.currentUser!!.uid
        val creatorName = auth.currentUser!!.displayName ?: "User"

        db.collection("workspaces")
            .document(code)
            .set(
                mapOf(
                    "name" to name,
                    "password" to password,
                    "creatorId" to uid,
                    "creatorName" to creatorName,
                    "memberIds" to listOf(uid),
                    "members" to listOf(creatorName),
                    "createdAt" to System.currentTimeMillis(),
                    "status" to "To Do"
                )
            )
            .await()
    }

    suspend fun joinWorkspace(
        code: String,
        password: String
    ): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        val userName = auth.currentUser?.displayName ?: "Member"

        val docRef = db.collection("workspaces").document(code)
        val snapshot = docRef.get().await()

        if (!snapshot.exists()) return false

        val storedPassword = snapshot.getString("password")
        if (storedPassword != password) return false

        docRef.update(
            mapOf(
                "memberIds" to com.google.firebase.firestore.FieldValue.arrayUnion(uid),
                "members" to com.google.firebase.firestore.FieldValue.arrayUnion(userName)
            )
        ).await()

        return true
    }

    suspend fun updateWorkspaceProgress(
        workspaceId: String,
        progressList: List<ProgressDetail>
    ) {
        db.collection("workspaces")
            .document(workspaceId)
            .update(
                "progressDetails",
                progressList.map {
                    mapOf(
                        "id" to it.id,
                        "text" to it.text,
                        "createdAt" to it.createdAt
                    )
                }
            )
            .await()
    }


}
