package com.example.stratify

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

class WorkspaceRepository {

    val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val workspacesCollection = db.collection("workspaces")

    // Create workspace baru
    suspend fun createWorkspace(
        id: String,
        name: String,
        password: String
    ): Result<Workspace> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("User not authenticated"))

            val userName = currentUser.displayName ?: "Unknown User"
            val userId = currentUser.uid
            val photoUrl = currentUser.photoUrl?.toString() ?: ""

            val workspace = Workspace(
                id = id,
                name = name,
                creatorName = userName,
                creatorId = userId,
                password = password,
                status = "To Do",
                department = "",
                members = arrayListOf(userName),
                memberIds = arrayListOf(userId),
                memberPhotos = hashMapOf(userId to photoUrl),  // ✨ Include photo
                details = ""
            )

            Log.d("REPO_CREATE", "Creating workspace: $id")
            workspacesCollection.document(id).set(workspace).await()

            Result.success(workspace)
        } catch (e: Exception) {
            Log.e("REPO_ERROR", "Create workspace error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Join workspace dengan ID dan password
    suspend fun joinWorkspace(
        workspaceId: String,
        password: String
    ): Result<Workspace> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("User not authenticated"))

            val userName = currentUser.displayName ?: "Unknown User"
            val userId = currentUser.uid
            val photoUrl = currentUser.photoUrl?.toString() ?: ""

            Log.d("REPO_JOIN", "User: $userName ($userId)")
            Log.d("REPO_JOIN", "Workspace ID: $workspaceId")

            // Get workspace document
            val docSnapshot = workspacesCollection.document(workspaceId).get().await()

            if (!docSnapshot.exists()) {
                Log.e("REPO_JOIN", "Workspace not found")
                return Result.failure(Exception("Workspace tidak ditemukan"))
            }

            val workspace = docSnapshot.toObject(Workspace::class.java)
                ?: return Result.failure(Exception("Invalid workspace data"))

            Log.d("REPO_JOIN", "Workspace found: ${workspace.name}")
            Log.d("REPO_JOIN", "Current members: ${workspace.memberIds}")

            // Verify password
            if (workspace.password != password) {
                Log.e("REPO_JOIN", "Password mismatch")
                return Result.failure(Exception("Password salah"))
            }

            // Check if already a member
            if (workspace.memberIds.contains(userId)) {
                Log.w("REPO_JOIN", "User already a member")
                return Result.failure(Exception("You are already a member"))
            }

            // Add member dengan photo
            Log.d("REPO_JOIN", "Adding user to workspace...")
            workspacesCollection.document(workspaceId).update(
                mapOf(
                    "members" to FieldValue.arrayUnion(userName),
                    "memberIds" to FieldValue.arrayUnion(userId),
                    "memberPhotos.$userId" to photoUrl,  // ✨ Save photo URL
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()

            Log.d("REPO_JOIN", "Successfully joined workspace!")

            // Return updated workspace
            val updatedWorkspace = workspace.copy(
                members = ArrayList(workspace.members).apply { add(userName) },
                memberIds = ArrayList(workspace.memberIds).apply { add(userId) },
                memberPhotos = HashMap(workspace.memberPhotos).apply { put(userId, photoUrl) }
            )

            Result.success(updatedWorkspace)
        } catch (e: Exception) {
            Log.e("REPO_ERROR", "Join workspace error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Get all workspaces where user is a member (real-time listener)
    fun listenToUserWorkspaces(
        onWorkspacesChanged: (List<Workspace>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration? {
        val currentUser = auth.currentUser ?: return null

        return workspacesCollection
            .whereArrayContains("memberIds", currentUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("REPO_LISTEN", "Listen error: ${error.message}", error)
                    onError(error)
                    return@addSnapshotListener
                }

                val workspaces = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Workspace::class.java)
                } ?: emptyList()

                Log.d("REPO_LISTEN", "Workspaces updated: ${workspaces.size} items")
                onWorkspacesChanged(workspaces)
            }
    }

    // Get user workspaces (one-time fetch)
    suspend fun getUserWorkspaces(): Result<List<Workspace>> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("User not authenticated"))

            val querySnapshot = workspacesCollection
                .whereArrayContains("memberIds", currentUser.uid)
                .get()
                .await()

            val workspaces = querySnapshot.documents.mapNotNull { doc ->
                doc.toObject(Workspace::class.java)
            }

            Result.success(workspaces)
        } catch (e: Exception) {
            Log.e("REPO_ERROR", "Get workspaces error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Delete workspace (creator only)
    suspend fun deleteWorkspace(workspaceId: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("User not authenticated"))

            val docSnapshot = workspacesCollection.document(workspaceId).get().await()
            val workspace = docSnapshot.toObject(Workspace::class.java)
                ?: return Result.failure(Exception("Workspace not found"))

            // Only creator can delete
            if (workspace.creatorId != currentUser.uid) {
                return Result.failure(Exception("Only creator can delete workspace"))
            }

            workspacesCollection.document(workspaceId).delete().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("REPO_ERROR", "Delete workspace error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Update workspace (untuk edit status, department, details)
    suspend fun updateWorkspace(workspace: Workspace): Result<Unit> {
        return try {
            workspacesCollection.document(workspace.id)
                .update(
                    mapOf(
                        "status" to workspace.status,
                        "department" to workspace.department,
                        "details" to workspace.details,
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("REPO_ERROR", "Update workspace error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ============ PROGRESS METHODS ============

    // Add progress to workspace
    suspend fun addProgress(workspaceId: String, progressText: String): Result<ProgressItem> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("User not authenticated"))

            val userName = currentUser.displayName ?: "Unknown User"
            val userId = currentUser.uid

            // Format date
            val dateFormat = java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.getDefault())
            val dateText = dateFormat.format(java.util.Date())

            val progress = ProgressItem(
                text = progressText,
                createdBy = userName,
                createdById = userId,
                dateText = dateText
            )

            val docRef = workspacesCollection
                .document(workspaceId)
                .collection("progress")
                .add(progress)
                .await()

            val createdProgress = progress.copy(id = docRef.id)

            Log.d("REPO_PROGRESS", "Progress added: ${createdProgress.id}")
            Result.success(createdProgress)
        } catch (e: Exception) {
            Log.e("REPO_ERROR", "Add progress error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Delete progress from workspace
    suspend fun deleteProgress(workspaceId: String, progressId: String): Result<Unit> {
        return try {
            workspacesCollection
                .document(workspaceId)
                .collection("progress")
                .document(progressId)
                .delete()
                .await()

            Log.d("REPO_PROGRESS", "Progress deleted: $progressId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("REPO_ERROR", "Delete progress error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Real-time listener untuk progress
    fun listenToProgress(
        workspaceId: String,
        onProgressChanged: (List<ProgressItem>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return workspacesCollection
            .document(workspaceId)
            .collection("progress")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("REPO_PROGRESS", "Listen error: ${error.message}", error)
                    onError(error)
                    return@addSnapshotListener
                }

                val progressList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ProgressItem::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                Log.d("REPO_PROGRESS", "Progress updated: ${progressList.size} items")
                onProgressChanged(progressList)
            }
    }

    // --- FUNGSI BARU UNTUK SPLASHSCREEN ---
    /**
     * Mengecek dengan cepat apakah user punya minimal 1 workspace.
     */
    suspend fun hasWorkspaces(): Result<Boolean> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.success(false) // Jika tidak login, pasti tidak punya

            val querySnapshot = workspacesCollection
                .whereArrayContains("memberIds", currentUser.uid)
                .limit(1) // <-- Ini kuncinya, kita cuma butuh 1 data
                .get()
                .await()

            // Jika query.isEmpty (kosong) = false -> return false
            // Jika query.isEmpty (tidak kosong) = true -> return true
            Result.success(!querySnapshot.isEmpty)

        } catch (e: Exception) {
            // Jika error, anggap tidak punya
            Result.failure(e)
        }
    }
}