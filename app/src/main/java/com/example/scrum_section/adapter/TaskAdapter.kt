package com.example.scrum_section.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.stratify.R
import com.example.scrum_section.EditTaskDialog
import com.example.scrum_section.TaskDetailDialog
import com.example.scrum_section.model.Task
import com.example.scrum_section.util.TaskStatus
import com.example.scrum_section.data.TaskRepository


class TaskAdapter(private var tasks: MutableList<Task>) :
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvTaskName)
        val tvDeadline: TextView = itemView.findViewById(R.id.tvDeadline)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val imgAvatar: ImageView = itemView.findViewById(R.id.imgAvatar)
        val btnChangeStatus: ImageView = itemView.findViewById(R.id.btnChangeStatus)
        val btnDeleteTask: ImageView = itemView.findViewById(R.id.btnDeleteTask)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun getItemCount() = tasks.size

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        holder.tvName.text = task.name
        holder.tvDeadline.text = task.deadline
        holder.tvStatus.text = task.status.name.replace("_", " ")

        fun applyStatusColor(status: TaskStatus) {
            val context = holder.tvStatus.context
            val colorRes = when (status) {
                TaskStatus.TODO -> R.color.blue
                TaskStatus.IN_PROGRESS -> R.color.orange
                TaskStatus.TO_VERIFY -> R.color.purple
                TaskStatus.DONE -> R.color.green
                else -> R.color.gray
            }
            val color = context.getColor(colorRes)
            holder.tvStatus.background.setTint(color)
        }

        applyStatusColor(task.status)

        // Klik status -> popup ubah status
        holder.tvStatus.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.status_menu, popup.menu)

            popup.setOnMenuItemClickListener { menuItem ->
                val newStatus = when (menuItem.itemId) {
                    R.id.status_todo -> TaskStatus.TODO
                    R.id.status_in_progress -> TaskStatus.IN_PROGRESS
                    R.id.status_to_verify -> TaskStatus.TO_VERIFY
                    R.id.status_done -> TaskStatus.DONE
                    else -> task.status
                }

                if (newStatus != task.status) {
                    task.status = newStatus
                    TaskRepository.updateTask(task)
                    holder.tvStatus.text = newStatus.name.replace("_", " ")
                    applyStatusColor(newStatus)
                }
                true
            }

            popup.show()
        }

        // Click edit icon -> the edit dialog will open
        holder.btnChangeStatus.setOnClickListener {
            val activity = holder.itemView.context as? FragmentActivity
            activity?.let {
                val editDialog = EditTaskDialog(task) {
                    notifyItemChanged(holder.adapterPosition)
                }
                editDialog.show(it.supportFragmentManager, "edit_task")
            }
        }

        // Click trash icon -> the delete confirmation dialog will open
        holder.btnDeleteTask.setOnClickListener {
            val context = holder.itemView.context
            val currentPosition = holder.adapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                AlertDialog.Builder(context)
                    .setTitle("Delete Task")
                    .setMessage("Are you sure you want to delete this task?")
                    .setPositiveButton("Yes") { _, _ ->
                        TaskRepository.deleteTask(tasks[currentPosition].id)
                        tasks.removeAt(currentPosition)
                        notifyItemRemoved(currentPosition)
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        }

        // Click item -> the detail task dialog will open
        holder.itemView.setOnClickListener {
            val activity = holder.itemView.context as? FragmentActivity
            activity?.let {
                val detailDialog = TaskDetailDialog(task)
                detailDialog.show(it.supportFragmentManager, "task_detail")
            }
        }
    }

    fun updateData(newList: List<Task>) {
        tasks = newList.toMutableList()
        notifyDataSetChanged()
    }
}
