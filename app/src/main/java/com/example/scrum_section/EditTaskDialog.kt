package com.example.scrum_section

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.scrum_section.data.TaskRepository
import com.example.scrum_section.model.Task
import com.example.stratify.R

class EditTaskDialog(private val task: Task, private val onUpdated: () -> Unit) : DialogFragment() {
    /**
     * Creates and configures the dialog for editing a task.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_add_task) // Reusing the add task dialog layout

        // Get references to the UI elements in the dialog layout
        val etName = dialog.findViewById<EditText>(R.id.etName)
        val etDeadline = dialog.findViewById<EditText>(R.id.etDeadline)
        val etDepartment = dialog.findViewById<EditText>(R.id.etDepartment)
        val etDescription = dialog.findViewById<EditText>(R.id.etDescription)
        val btnAdd = dialog.findViewById<Button>(R.id.btnAdd)

        // Pre-fill the input fields with the existing task's data
        etName.setText(task.name)
        etDeadline.setText(task.deadline)
        etDepartment.setText(task.department)
        etDescription.setText(task.description)
        btnAdd.text = "Update" // Change the button text to "Update"

        // Set an OnClickListener for the "Update" button
        btnAdd.setOnClickListener {
            // Get the new values from the input fields
            val newName = etName.text.toString().trim()
            val newDeadline = etDeadline.text.toString().trim()
            val newDept = etDepartment.text.toString().trim()
            val newDescription = etDescription.text.toString().trim()

            // Check if the task name is not empty
            if (newName.isNotEmpty()) {
                // Update the task object with the new values
                task.name = newName
                task.deadline = newDeadline
                task.department = newDept
                task.description = newDescription

                // Update the task in the repository
                TaskRepository.updateTask(task)
                // Show a success message
                Toast.makeText(requireContext(), "Task updated succesfully!", Toast.LENGTH_SHORT).show()

                // Invoke the callback function and dismiss the dialog
                onUpdated()
                dismiss()
            } else {
                // Show an error message if the task name is empty
                Toast.makeText(requireContext(), "Task's name not allowed empty!", Toast.LENGTH_SHORT).show()
            }
        }

        // Set the dialog's width to 80% of the screen width
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.8).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        // Set the dialog's background to be transparent
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        return dialog
    }
}
