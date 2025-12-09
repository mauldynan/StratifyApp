package com.example.stratify

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Pastikan library Glide Compose sudah ditambahkan di build.gradle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete

// ==========================================
// 1. PENGGANTI WORKSPACE ADAPTER
// ==========================================

@Composable
fun WorkspaceList(
    workspaces: List<Workspace>,
    onItemClick: (Workspace) -> Unit,
    onDeleteClick: (Workspace) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(workspaces) { workspace ->
            WorkspaceItem(workspace, onItemClick, onDeleteClick)
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun WorkspaceItem(
    workspace: Workspace,
    onItemClick: (Workspace) -> Unit,
    onDeleteClick: (Workspace) -> Unit
) {
    // --- LOGIKA DARI ADAPTER LAMA KAMU DIPINDAH KE SINI ---
    val currentUser = FirebaseAuth.getInstance().currentUser
    val isMyWorkspace = workspace.creatorId == currentUser?.uid

    // Jika workspace saya, pakai data live user. Jika orang lain, pakai data dari workspace.
    val creatorName = if (isMyWorkspace) currentUser?.displayName else workspace.creatorName
    val photoUrl = if (isMyWorkspace) currentUser?.photoUrl else workspace.memberPhotos[workspace.creatorId]

    // Status color logic
    val statusColor = when (workspace.status) {
        "In Progress" -> MaterialTheme.colorScheme.primary
        "To Verify" -> MaterialTheme.colorScheme.tertiary
        "Done" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outline // Todo
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(workspace) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto Profil
            GlideImage(
                model = photoUrl,
                contentDescription = "Profile",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            ) {
                it.placeholder(android.R.drawable.ic_menu_gallery)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = workspace.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "by ${creatorName ?: "Unknown"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Status Badge kecil
            Surface(
                color = statusColor.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = workspace.status,
                    color = statusColor,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            IconButton(onClick = { onDeleteClick(workspace) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// ==========================================
// 2. PENGGANTI MEMBER ADAPTER
// ==========================================

@Composable
fun MemberList(
    members: List<Member>,
    memberPhotos: Map<String, String> // Map foto dari parent
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(members) { member ->
            MemberItem(member, memberPhotos)
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MemberItem(
    member: Member,
    memberPhotos: Map<String, String>
) {
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Logika MemberAdapter: Cek apakah "isYou"
    val displayName = if (member.isYou) "${member.name} (You)" else member.name

    // Logika Foto MemberAdapter
    val photoUrl = if (member.isYou && currentUser != null) {
        currentUser.photoUrl
    } else {
        memberPhotos[member.userId]
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlideImage(
            model = photoUrl,
            contentDescription = "Member Photo",
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        ) {
            it.placeholder(android.R.drawable.ic_menu_gallery)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(text = displayName, style = MaterialTheme.typography.bodyLarge)
    }
}