package com.example.stratify

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.stratify.databinding.ItemInformationBinding
import java.text.SimpleDateFormat
import java.util.Locale

class InformationAdapter(
    private val items: List<InformationItem>,
    private val onActionClick: (InformationItem, Int) -> Unit,
    private val onTextChange: (InformationItem, String) -> Unit
) : RecyclerView.Adapter<InformationAdapter.InformationViewHolder>() {

    private var isDeleteModeActive: Boolean = false

    fun setDeleteMode(isActive: Boolean) {
        isDeleteModeActive = isActive
        notifyDataSetChanged() // Redraw the entire list to reflect the mode change.
    }

    /**
     * ViewHolder for the information items.
     */
    inner class InformationViewHolder(val binding: ItemInformationBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * Creates a new ViewHolder by inflating the item layout.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InformationViewHolder {
        val binding = ItemInformationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InformationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InformationViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvInfoDetails.text = item.details
        holder.binding.etInfoDetails.setText(item.details)

        // Toggle visibility between TextView and EditText based on the editing state.
        holder.binding.tvInfoDetails.isVisible = !item.isEditing
        holder.binding.etInfoDetails.isVisible = item.isEditing

        if (isDeleteModeActive) {
            // --- UI logic for Delete Mode ---
            holder.binding.tvSavedLabel.visibility = View.GONE
            holder.binding.tvInfoDate.visibility = View.GONE

            // Change the icon to indicate whether the item is selected for deletion.
            if (item.isSelectedForDeletion) {
                holder.binding.ivActionIcon.setImageResource(R.drawable.ic_close_circle)
            } else {
                // Use a different icon for items that are not yet selected for deletion.
                if (item.isSaved) {
                    holder.binding.ivActionIcon.setImageResource(R.drawable.ic_check_circle)
                } else {
                    holder.binding.ivActionIcon.setImageResource(R.drawable.ic_radio_button_unchecked)
                }
            }
            // The click listener toggles the selection state for deletion.
            holder.binding.ivActionIcon.setOnClickListener {
                item.isSelectedForDeletion = !item.isSelectedForDeletion
                notifyItemChanged(position)
            }

        } else {
            // --- UI logic for Normal Mode ---
            if (item.isSaved) {
                holder.binding.ivActionIcon.setImageResource(R.drawable.ic_check_circle)
                holder.binding.tvSavedLabel.visibility = View.VISIBLE
                holder.binding.tvInfoDate.visibility = View.VISIBLE
                item.savedDate?.let {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    holder.binding.tvInfoDate.text = sdf.format(it)
                }
            } else {
                holder.binding.ivActionIcon.setImageResource(R.drawable.ic_radio_button_unchecked)
                holder.binding.tvSavedLabel.visibility = View.GONE
                holder.binding.tvInfoDate.visibility = View.GONE
            }

            // If the item is in editing mode, give focus to the EditText.
            if (item.isEditing) {
                holder.binding.etInfoDetails.requestFocus()
                holder.binding.ivActionIcon.setImageResource(R.drawable.ic_check_circle)
            }

            // In normal mode, the action icon click is handled by the onActionClick callback.
            holder.binding.ivActionIcon.setOnClickListener {
                onActionClick(item, position)
            }
        }

        // Listener for the "Done" action on the soft keyboard.
        holder.binding.etInfoDetails.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // When "Done" is pressed, trigger the text change callback.
                onTextChange(item, textView.text.toString())
                return@setOnEditorActionListener true
            }
            false
        }
    }

    /**
     * Returns the total number of items in the list.
     */
    override fun getItemCount(): Int {
        return items.size
    }
}
