package com.example.stratify.ui.scrum

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

/**
 * Simple repository to store user's scrum tasks under `users/{uid}/scrum_tasks`.
 */
class ScrumRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun userTasksCollectionPath() = auth.currentUser?.uid?.let { uid ->
        db.collection("users").document(uid).collection("scrum_tasks")
    }

    suspend fun addTaskRemote(task: Task): Result<Task> {
        return try {
            val col = userTasksCollectionPath() ?: return Result.failure(Exception("User not authenticated"))
            val data = mapOf(
                "name" to task.name,
                "description" to task.description,
                "estimation" to task.estimation,
                "deadline" to task.deadline,
                "status" to task.status.name,
                "createdAt" to System.currentTimeMillis()
            )
            val docRef = col.add(data).await()
            val created = task.copy(remoteId = docRef.id)
            Result.success(created)
        } catch (e: Exception) {
            Log.e("SCRUM_REPO", "addTaskRemote error", e)
            Result.failure(e)
        }
    }

    suspend fun updateTaskRemote(task: Task): Result<Unit> {
        return try {
            val col = userTasksCollectionPath() ?: return Result.failure(Exception("User not authenticated"))
            val rid = task.remoteId ?: return Result.failure(Exception("Missing remoteId"))
            val data = mapOf(
                "name" to task.name,
                "description" to task.description,
                "estimation" to task.estimation,
                "deadline" to task.deadline,
                "status" to task.status.name,
                "updatedAt" to System.currentTimeMillis()
            )
            col.document(rid).update(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("SCRUM_REPO", "updateTaskRemote error", e)
            Result.failure(e)
        }
    }

    suspend fun deleteTaskRemote(remoteId: String): Result<Unit> {
        return try {
            val col = userTasksCollectionPath() ?: return Result.failure(Exception("User not authenticated"))
            col.document(remoteId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("SCRUM_REPO", "deleteTaskRemote error", e)
            Result.failure(e)
        }
    }

    fun listenToUserTasks(
        onChanged: (List<Task>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration? {
        val col = userTasksCollectionPath() ?: return null
        return col.orderBy("createdAt").addSnapshotListener { snapshot, error ->
            if (error != null) {
                onError(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapIndexedNotNull { idx, doc ->
                val name = doc.getString("name") ?: return@mapIndexedNotNull null
                val description = doc.getString("description") ?: ""
                val estimation = doc.getString("estimation") ?: ""
                val deadline = doc.getString("deadline") ?: ""
                val statusName = doc.getString("status") ?: TaskStatus.TODO.name
                val status = try { TaskStatus.valueOf(statusName) } catch (e: Exception) { TaskStatus.TODO }
                // Use hash of remote id for stable Int id used in UI keys
                val id = doc.id.hashCode()
                Task(id, name, description, estimation, deadline, status, remoteId = doc.id)
            } ?: emptyList()
            onChanged(list)
        }
    }
}
