package com.example.stratify

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.stratify.databinding.ItemProgressBinding

class ProgressAdapter(
    private var progressList: List<ProgressItem>,
    private val onDeleteClick: (ProgressItem) -> Unit
) :
    RecyclerView.Adapter<ProgressAdapter.ProgressViewHolder>() {

    /**
     * Creates a new ViewHolder by inflating the item layout.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgressViewHolder {
        val binding = ItemProgressBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProgressViewHolder(binding)
    }

    /**
     * Binds the data from a [ProgressItem] to the views in the ViewHolder.
     */
    override fun onBindViewHolder(holder: ProgressViewHolder, position: Int) {
        val progressItem = progressList[position]
        holder.bind(progressItem)
    }

    /**
     * Returns the total number of items in the list.
     */
    override fun getItemCount() = progressList.size

    /**
     * Updates the list of progress items and notifies the adapter of the data change.
     */
    fun submitList(newList: List<ProgressItem>) {
        progressList = newList
        notifyDataSetChanged()
    }

    /**
     * ViewHolder for the progress items.
     */
    inner class ProgressViewHolder(private val binding: ItemProgressBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Binds a [ProgressItem] to the views in the ViewHolder.
         */
        fun bind(progressItem: ProgressItem) {
            binding.tvProgressText.text = progressItem.text
            binding.tvProgressDate.text = progressItem.date
            binding.btnDeleteProgress.setOnClickListener {
                onDeleteClick(progressItem)
            }
        }
    }
}
