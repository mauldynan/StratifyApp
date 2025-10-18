package com.example.stratify

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.stratify.databinding.ItemWorkspaceBinding

class WorkspaceAdapter(
    private var items: MutableList<Workspace>,
    private val onItemClick: (Workspace) -> Unit,
    private val onDeleteClick: (Workspace) -> Unit
) : RecyclerView.Adapter<WorkspaceAdapter.WorkspaceViewHolder>() {

    /**
     * ViewHolder for the workspace items.
     */
    inner class WorkspaceViewHolder(val binding: ItemWorkspaceBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * Creates a new ViewHolder by inflating the item layout.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkspaceViewHolder {
        val binding = ItemWorkspaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WorkspaceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkspaceViewHolder, position: Int) {
        val workspace = items[position]
        // Set the workspace name.
        holder.binding.tvTaskName.text = workspace.name
        // Set the creator's name.
        holder.binding.tvCreatorName.text = "by ${workspace.creatorName}"
        // Set a placeholder for the profile image.
        holder.binding.ivProfile.setImageResource(R.drawable.ic_profile_placeholder)

        // Set the status text.
        holder.binding.tvStatus.text = workspace.status
        // Determine the background color for the status based on its value.
        val colorRes = when (workspace.status) {
            "In Progress" -> R.color.status_inprogress
            "To Verify" -> R.color.status_toverify
            "Done" -> R.color.status_done
            else -> R.color.status_todo
        }
        // Apply the background tint to the status TextView.
        holder.binding.tvStatus.backgroundTintList = ContextCompat.getColorStateList(holder.itemView.context, colorRes)


        // Set a click listener for the entire item view.
        holder.itemView.setOnClickListener {
            onItemClick(workspace)
        }

        // Set a click listener for the delete button.
        holder.binding.btnDeleteWorkspace.setOnClickListener {
            onDeleteClick(workspace)
        }
    }

    /**
     * Returns the total number of items in the list.
     */
    override fun getItemCount(): Int {
        return items.size
    }

    /**
     * Updates the list of workspaces with a new list and notifies the adapter of the change.
     */
    fun updateWorkspaces(newWorkspaces: List<Workspace>) {
        items.clear()
        items.addAll(newWorkspaces)
        notifyDataSetChanged()
    }
}
