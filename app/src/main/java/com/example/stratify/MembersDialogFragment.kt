package com.example.stratify

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.firebase.auth.FirebaseAuth

class MembersDialogFragment : DialogFragment() {

    private val auth = FirebaseAuth.getInstance()

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
            setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return ComposeView(requireContext()).apply {
            setContent {
                val memberNames = arguments?.getStringArray(ARG_MEMBER_NAMES) ?: emptyArray()
                val memberIds = arguments?.getStringArray(ARG_MEMBER_IDS) ?: emptyArray()

                val memberPhotos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    arguments?.getSerializable(ARG_MEMBER_PHOTOS, HashMap::class.java) as? HashMap<String, String>
                } else {
                    @Suppress("DEPRECATION")
                    arguments?.getSerializable(ARG_MEMBER_PHOTOS) as? HashMap<String, String>
                } ?: hashMapOf()

                Log.d("DBG_MEMBERS", "Member names: ${memberNames.toList()}")
                Log.d("DBG_MEMBERS", "Member IDs: ${memberIds.toList()}")
                Log.d("DBG_MEMBERS", "Member photos: $memberPhotos")

                val currentUserId = auth.currentUser?.uid

                val members = memberNames.mapIndexed { index, name ->
                    val id = memberIds.getOrNull(index) ?: ""
                    Member(
                        userId = id,
                        name = name,
                        isYou = (id == currentUserId)
                    )
                }
                
                MaterialTheme {
                    MembersDialogContent(
                        members = members,
                        memberPhotos = memberPhotos,
                        onDismiss = { dismiss() }
                    )
                }
            }
        }
    }


    companion object {
        private const val ARG_MEMBER_NAMES = "member_names"
        private const val ARG_MEMBER_IDS = "member_ids"
        private const val ARG_MEMBER_PHOTOS = "member_photos"

        fun newInstance(
            memberNames: Array<String>,
            memberIds: Array<String>,
            memberPhotos: HashMap<String, String> = hashMapOf()
        ): MembersDialogFragment {
            val fragment = MembersDialogFragment()
            val args = Bundle().apply {
                putStringArray(ARG_MEMBER_NAMES, memberNames)
                putStringArray(ARG_MEMBER_IDS, memberIds)
                putSerializable(ARG_MEMBER_PHOTOS, memberPhotos)
            }
            fragment.arguments = args
            return fragment
        }
    }
}

@Composable
fun MembersDialogContent(
    members: List<Member>,
    memberPhotos: Map<String, String>,
    onDismiss: () -> Unit
) {
    Card(shape = MaterialTheme.shapes.large) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Workspace Members", style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onDismiss) {
                    Icon(painter = painterResource(id = R.drawable.ic_close_circle), contentDescription = "Close")
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                items(members) { member ->
                    MemberListItem(member = member, photoUrl = memberPhotos[member.userId])
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MemberListItem(member: Member, photoUrl: String?) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val memberName = if (member.isYou) "${member.name} (You)" else member.name
    
    val photoUrlToLoad = if(member.isYou) {
        FirebaseAuth.getInstance().currentUser?.photoUrl?.toString()
    } else {
        photoUrl
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlideImage(
            model = photoUrlToLoad,
            contentDescription = "Member profile photo",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        ) {
            it.error(R.drawable.ic_profile_placeholder)
             .placeholder(R.drawable.ic_profile_placeholder)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = memberName,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (member.isYou) FontWeight.Bold else FontWeight.Normal
        )
    }
}
