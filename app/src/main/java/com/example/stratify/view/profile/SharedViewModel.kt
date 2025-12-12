package com.example.stratify.view.profile

import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.stratify.Workspace

class SharedViewModel : ViewModel() {
    val displayName = MutableLiveData<String>()
    val photoUri = MutableLiveData<Uri>()

    private val _workspaces = mutableStateListOf<Workspace>()
    val workspaces: List<Workspace> = _workspaces

    private var lastDeletedWorkspace: Workspace? = null
    private var lastDeletedWorkspaceIndex: Int = -1

    fun createWorkspace(name: String, code: String, password: String) {
        if (!workspaceExists(code)) {
            _workspaces.add(Workspace(id = code, name = name, password = password))
        }
    }

    fun getWorkspaceById(workspaceId: String): LiveData<Workspace?> {
        val liveData = MutableLiveData<Workspace?>()
        liveData.value = workspaces.find { it.id == workspaceId }
        return liveData
    }

    fun joinWorkspace(code: String, password: String): Boolean {
        if (workspaceExists(code)) {
            return true
        }

        _workspaces.add(Workspace(id = code, name = "Joined: $code", password = password))
        return true
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
        }
    }

    fun undoDeleteWorkspace() {
        lastDeletedWorkspace?.let {
            if (lastDeletedWorkspaceIndex != -1) {
                _workspaces.add(lastDeletedWorkspaceIndex, it)
                lastDeletedWorkspace = null
                lastDeletedWorkspaceIndex = -1
            }
        }
    }
}
