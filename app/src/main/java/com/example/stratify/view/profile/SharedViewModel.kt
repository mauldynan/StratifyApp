package com.example.stratify.view.profile

import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class Workspace(val name: String, val code: String, val password: String)

class SharedViewModel : ViewModel() {
    val displayName = MutableLiveData<String>()
    val photoUri = MutableLiveData<Uri>()

    private val _workspaces = mutableStateListOf<Workspace>()
    val workspaces: List<Workspace> = _workspaces

    fun createWorkspace(name: String, code: String, password: String) {
        if (!workspaceExists(code)) {
            _workspaces.add(Workspace(name, code, password))
        }
    }

    // Simulates joining a workspace. In a real app, this would verify credentials.
    // For now, it just adds a new workspace to the list if the code isn't already there.
    // Returns true if the join was "successful"
    fun joinWorkspace(code: String, password: String): Boolean {
        // In a real app, you'd have a list of available workspaces to join from a server.
        // For now, we'll just check if we've already "created" or "joined" a workspace with this code.
        if (workspaceExists(code)) {
            // Already a member of this workspace
            return true
        }

        // Simulate a successful join by adding it to our list.
        // The name is a placeholder since we don't know the real name from just a code/password.
        _workspaces.add(Workspace(name = "Joined: $code", code = code, password = password))
        return true
    }

    private fun workspaceExists(code: String): Boolean {
        return _workspaces.any { it.code == code }
    }
}
