package com.example.stratify.view.profile

import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stratify.Workspace
import com.example.stratify.WorkspaceRepository
import kotlinx.coroutines.launch

class SharedViewModel : ViewModel() {
    val displayName = MutableLiveData<String>()
    val photoUri = MutableLiveData<Uri>()

    private val _workspaces = mutableStateListOf<Workspace>()
    val workspaces: List<Workspace> = _workspaces

    private var lastDeletedWorkspace: Workspace? = null
    private var lastDeletedWorkspaceIndex: Int = -1

    private val repository = WorkspaceRepository()
    private var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    fun loadWorkspaces() {
        listenerRegistration?.remove()
        listenerRegistration = repository.listenToUserWorkspaces(
            onWorkspacesChanged = { newWorkspaces ->
                _workspaces.clear()
                _workspaces.addAll(newWorkspaces)
            },
            onError = { e ->
                e.printStackTrace()
            }
        )
    }

    fun createWorkspace(name: String, code: String, password: String) {
        viewModelScope.launch {
            repository.createWorkspace(code, name, password)
        }
    }

    fun getWorkspaceById(workspaceId: String): LiveData<Workspace?> {
        val liveData = MutableLiveData<Workspace?>()
        liveData.value = workspaces.find { it.id == workspaceId }
        return liveData
    }

    fun updateWorkspace(updatedWorkspace: Workspace) {
        val index = _workspaces.indexOfFirst { it.id == updatedWorkspace.id }
        if (index != -1) {
            _workspaces[index] = updatedWorkspace
        }
        
        viewModelScope.launch {
            repository.updateWorkspace(updatedWorkspace)
        }
    }

    fun joinWorkspace(code: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.joinWorkspace(code, password)
            if (result.isSuccess) {
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    private fun workspaceExists(code: String): Boolean {
        return _workspaces.any { it.id == code }
    }

    fun deleteWorkspace(workspace: Workspace) {
        val index = _workspaces.indexOf(workspace)
        if (index != -1) {
            lastDeletedWorkspace = workspace
            lastDeletedWorkspaceIndex = index
            _workspaces.removeAt(index)
            
            viewModelScope.launch {
                repository.deleteWorkspace(workspace.id)
            }
        }
    }

    fun undoDeleteWorkspace() {
        lastDeletedWorkspace?.let { workspace ->
            if (lastDeletedWorkspaceIndex != -1) {
                _workspaces.add(lastDeletedWorkspaceIndex, workspace)
                
                viewModelScope.launch {
                    // Attempt to restore. Note: This creates a fresh workspace with the same basic info
                    repository.createWorkspace(workspace.id, workspace.name, workspace.password)
                }

                lastDeletedWorkspace = null
                lastDeletedWorkspaceIndex = -1
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}
