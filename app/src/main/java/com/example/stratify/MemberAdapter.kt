package com.example.stratify

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.stratify.databinding.ItemMemberBinding
import com.google.firebase.auth.FirebaseAuth

class MemberAdapter(
    private val members: List<Member>,
    private val memberPhotos: Map<String, String> = emptyMap()
) : RecyclerView.Adapter<MemberAdapter.MemberViewHolder>() {

    private val auth = FirebaseAuth.getInstance()

    inner class MemberViewHolder(val binding: ItemMemberBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val binding = ItemMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemberViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = members[position]
        val currentUser = auth.currentUser

        val memberName = if (member.isYou) "${member.name} (You)" else member.name
        holder.binding.tvMemberName.text = memberName

        // --- AWAL PERBAIKAN BUG 2 ---
        val photoUrlToLoad: String? = if (member.isYou && currentUser != null) {
            // Jika itu user yang login, pakai foto terbaru
            currentUser.photoUrl?.toString()
        } else {
            // Jika itu member lain, cari fotonya di map pakai ID member tsb
            // (Saya asumsikan data class Member punya 'userId')
            memberPhotos[member.userId]
        }

        Glide.with(holder.itemView.context)
            .load(photoUrlToLoad)
            .placeholder(R.drawable.ic_profile_placeholder)
            .circleCrop()
            .into(holder.binding.ivMemberProfile)

        // --- AKHIR PERBAIKAN BUG 2 ---
    }

    override fun getItemCount(): Int = members.size
}