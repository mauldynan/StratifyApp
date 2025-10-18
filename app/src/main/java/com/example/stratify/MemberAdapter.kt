package com.example.stratify

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.stratify.databinding.ItemMemberBinding

class MemberAdapter(private val members: List<Member>) : RecyclerView.Adapter<MemberAdapter.MemberViewHolder>() {

    inner class MemberViewHolder(val binding: ItemMemberBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val binding = ItemMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemberViewHolder(binding)
    }

    /**
     * Binds the data from a [Member] object to the views in the ViewHolder.
     */
    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = members[position]
        val memberName = if (member.isYou) "${member.name} (You)" else member.name
        holder.binding.tvMemberName.text = memberName
        holder.binding.ivMemberProfile.setImageResource(R.drawable.ic_profile_placeholder)
    }

    override fun getItemCount(): Int = members.size
}
