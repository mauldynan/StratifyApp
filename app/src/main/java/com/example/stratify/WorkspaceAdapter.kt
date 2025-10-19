package com.example.stratify

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.stratify.databinding.ItemWorkspaceBinding
import com.google.firebase.auth.FirebaseAuth

class WorkspaceAdapter(
    private var items: MutableList<Workspace>,
    private val onItemClick: (Workspace) -> Unit,
    private val onDeleteClick: (Workspace) -> Unit
) : RecyclerView.Adapter<WorkspaceAdapter.WorkspaceViewHolder>() {

    private val auth = FirebaseAuth.getInstance()

    inner class WorkspaceViewHolder(val binding: ItemWorkspaceBinding) : RecyclerView.ViewHolder(binding.root) {

        // FUNGSI INI JUGA HARUS DIPERBAIKI
        @SuppressLint("SetTextI18n")
        fun updateProfilePhoto() {
            val position = bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val workspace = items[position]
                val currentUser = auth.currentUser

                // Reload user untuk dapet foto terbaru
                currentUser?.reload()?.addOnCompleteListener {
                    val updatedUser = FirebaseAuth.getInstance().currentUser

                    // Cek by creatorId (lebih akurat)
                    if (workspace.creatorId == updatedUser?.uid) {
                        // ✨ PERBAIKAN 1: Update nama juga
                        binding.tvCreatorName.text = "by ${updatedUser?.displayName ?: "Unknown"}"

                        Glide.with(itemView.context)
                            .load(updatedUser?.photoUrl)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .circleCrop()
                            .into(binding.ivProfile)
                    } else {
                        // ✨ PERBAIKAN 2: Tampilkan data basi dari creator
                        val creatorPhotoUrl = workspace.memberPhotos[workspace.creatorId]
                        binding.tvCreatorName.text = "by ${workspace.creatorName}"
                        Glide.with(itemView.context)
                            .load(creatorPhotoUrl)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .circleCrop()
                            .into(binding.ivProfile)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkspaceViewHolder {
        val binding = ItemWorkspaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WorkspaceViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: WorkspaceViewHolder, position: Int) {
        val workspace = items[position]
        val currentUser = auth.currentUser // Ambil current user

        // Set the workspace name
        holder.binding.tvTaskName.text = workspace.name

        // --- AWAL PERBAIKAN BUG NAMA & FOTO ---

        val creatorNameToShow: String?
        val photoUrlToLoad: String?

        if (workspace.creatorId == currentUser?.uid) {
            // Ini workspace saya. Pakai data 'live' dari Auth
            creatorNameToShow = currentUser.displayName
            photoUrlToLoad = currentUser.photoUrl?.toString()
        } else {
            // Ini workspace orang lain. Pakai data 'basi' dari dokumen
            creatorNameToShow = workspace.creatorName
            photoUrlToLoad = workspace.memberPhotos[workspace.creatorId]
        }

        // Set nama dan foto berdasarkan variabel di atas
        holder.binding.tvCreatorName.text = "by ${creatorNameToShow ?: "Unknown"}" // <-- NAMA SUDAH DIPERBAIKI

        Glide.with(holder.itemView.context)
            .load(photoUrlToLoad)
            .placeholder(R.drawable.ic_profile_placeholder)
            .circleCrop()
            .into(holder.binding.ivProfile) // <-- FOTO SUDAH DIPERBAIKI

        // --- AKHIR PERBAIKAN BUG NAMA & FOTO ---

        // Set the status text
        holder.binding.tvStatus.text = workspace.status

        // Determine background color for status
        val colorRes = when (workspace.status) {
            "In Progress" -> R.color.status_inprogress
            "To Verify" -> R.color.status_toverify
            "Done" -> R.color.status_done
            else -> R.color.status_todo
        }
        holder.binding.tvStatus.backgroundTintList = ContextCompat.getColorStateList(
            holder.itemView.context,
            colorRes
        )

        // Set click listeners
        holder.itemView.setOnClickListener {
            onItemClick(workspace)
        }

        holder.binding.btnDeleteWorkspace.setOnClickListener {
            onDeleteClick(workspace)
        }
    }

    override fun onBindViewHolder(holder: WorkspaceViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            // Update foto (dan nama) saja, gak perlu rebind semua
            holder.updateProfilePhoto()
        }
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateWorkspaces(newWorkspaces: List<Workspace>) {
        items.clear()
        items.addAll(newWorkspaces)
        notifyDataSetChanged()
    }

    // Method untuk update foto profil semua item
    fun refreshProfilePhotos() {
        for (i in items.indices) {
            notifyItemChanged(i, "UPDATE_PHOTO")
        }
    }
}