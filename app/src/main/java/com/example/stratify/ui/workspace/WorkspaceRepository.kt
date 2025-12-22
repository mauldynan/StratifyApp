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

        val snapshot = db.collection("workspaces")
            .whereArrayContains("memberIds", uid)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            Workspace(
                id = doc.id,
                name = doc.getString("name") ?: "",
                password = doc.getString("password") ?: "",
                status = doc.getString("status") ?: "To Do"
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
}
