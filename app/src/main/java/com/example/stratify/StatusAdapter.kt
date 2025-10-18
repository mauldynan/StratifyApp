// Corrected StatusAdapter.kt
import android.view.LayoutInflater    import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StatusAdapter(
    private val statusList: List<String>,
    private val onItemClick: (String) -> Unit // Add this parameter
) : RecyclerView.Adapter<StatusAdapter.StatusViewHolder>() {

    inner class StatusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val statusName: TextView = itemView.findViewById(android.R.id.text1) // Example ID, adjust if needed

        fun bind(status: String) {
            statusName.text = status
            itemView.setOnClickListener {
                onItemClick(status) // Call the lambda when an item is clicked
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        // Use a simple built-in layout for demonstration. You might have a custom one.
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return StatusViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {
        holder.bind(statusList[position])
    }

    override fun getItemCount(): Int = statusList.size
}
    