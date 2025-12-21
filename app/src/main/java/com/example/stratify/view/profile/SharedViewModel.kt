package com.example.stratify.view.profile

import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.stratify.R
import com.example.stratify.Workspace

data class EcommerceAppData(
    val id: Int,
    val name: String,
    val logo: Int,
    val rating: String,
    val reviews: String,
    val pos: Int,
    val neg: Int,
    val latestComments: List<Pair<String, String>>
)

class SharedViewModel : ViewModel() {
    val displayName = MutableLiveData<String>()
    val photoUri = MutableLiveData<Uri>()

    private val _workspaces = mutableStateListOf<Workspace>()
    val workspaces: List<Workspace> = _workspaces

    private var lastDeletedWorkspace: Workspace? = null
    private var lastDeletedWorkspaceIndex: Int = -1

    val ecommerceApps = listOf(
        EcommerceAppData(1, "Shopee", R.drawable.shopee_logo, "4.8", "1.2M", 850, 150, listOf(
            "Budi S." to "Love the free shipping vouchers!",
            "Siti A." to "Items arrived fast and as described.",
            "Andi W." to "App feels a bit laggy sometimes."
        )),
        EcommerceAppData(2, "Tokopedia", R.drawable.tokopedia_logo, "4.7", "900K", 780, 220, listOf(
            "Rina K." to "Everything is original in Official Stores.",
            "Dedi H." to "Customer service is very responsive.",
            "Lina M." to "Update frequency is a bit too high."
        )),
        EcommerceAppData(3, "Tiktok", R.drawable.tiktok_logo, "4.5", "600K", 600, 400, listOf(
            "Eko P." to "Great monthly discounts available.",
            "Ani R." to "Very secure packaging.",
            "Zaki F." to "Shipping takes too long."
        ))
    )

    fun createWorkspace(name: String, code: String, password: String) {
        if (!workspaceExists(code)) {
            val initialMembers = arrayListOf(displayName.value ?: "Creator")
            _workspaces.add(Workspace(id = code, name = name, password = password, isJoined = false, members = initialMembers))
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
    }

    fun joinWorkspace(code: String, password: String): Boolean {
        if (workspaceExists(code)) {
            val index = _workspaces.indexOfFirst { it.id == code }
            if (index != -1) {
                val existing = _workspaces[index]
                
                // Only add member if transitioning to joined status
                if (!existing.isJoined) {
                    val updatedMembers = ArrayList(existing.members)
                    updatedMembers.add(displayName.value ?: "Member")

                    _workspaces[index] = existing.copy(
                        isJoined = true,
                        members = updatedMembers
                    )
                }
            }
            return true
        }
        
        // Cannot join a workspace that doesn't exist
        return false
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
