package com.example.scrum_section

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.scrum_section.model.Task
import com.example.stratify.R

class TaskDetailDialog(private val task: Task) : DialogFragment() {
    /**
     * Creates and configures the dialog for displaying task details.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())

        // Remove the title bar from the dialog.
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_task_detail)

        val tvName = dialog.findViewById<TextView>(R.id.tvName)
        val tvDeadline = dialog.findViewById<TextView>(R.id.tvDeadline)
        val tvDepartment = dialog.findViewById<TextView>(R.id.tvDepartment)
        val tvDescription = dialog.findViewById<TextView>(R.id.tvDescription)
        val btnClose = dialog.findViewById<TextView>(R.id.btnClose)
        val underlineView = dialog.findViewById<View>(R.id.underlineView)

        tvName.text = task.name
        tvDeadline.text = task.deadline
        tvDepartment.text = task.department
        tvDescription.text = task.description

        // Adjust the width of the underline view to match the width of the task name.
        tvName.post {
            val params = underlineView.layoutParams
            params.width = tvName.width
            underlineView.layoutParams = params
        }

        // Set an OnClickListener for the close button to dismiss the dialog.
        btnClose.setOnClickListener { dismiss() }

        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.8).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        return dialog
    }
}
