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
import com.example.scrum_section.util.TaskStatus
import com.example.stratify.R

class AddTaskDialog(private val onTaskAdded: () -> Unit) : DialogFragment() {

    /**
     * Creates and configures the dialog for adding a new task.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_add_task)

        // Get references to the UI elements in the dialog layout
        val etName = dialog.findViewById<EditText>(R.id.etName)
        val etDeadline = dialog.findViewById<EditText>(R.id.etDeadline)
        val etDepartment = dialog.findViewById<EditText>(R.id.etDepartment)
        val etDescription = dialog.findViewById<EditText>(R.id.etDescription)
        val btnAdd = dialog.findViewById<Button>(R.id.btnAdd)

        // Set an OnClickListener for the "Add" button
        btnAdd.setOnClickListener {
            // Get the text from the input fields
            val name = etName.text.toString().trim()
            val deadline = etDeadline.text.toString().trim()
            val department = etDepartment.text.toString().trim()
            val description = etDescription.text.toString().trim()

            // Check if the task name is not empty
            if (name.isNotEmpty()) {
                // Create a new Task object
                val newTask = Task(
                    id = TaskRepository.getTasks().size + 1,
                    name = name,
                    createdBy = "You", // The creator is hardcoded as "You"
                    deadline = deadline,
                    department = department,
                    description = description,
                    status = TaskStatus.TODO // The initial status is set to "TODO"
                )

                // Add the new task to the repository
                TaskRepository.addTask(newTask)
                // Show a success message
                Toast.makeText(requireContext(), "Task created succesfully!", Toast.LENGTH_SHORT).show()

                // Invoke the callback function and dismiss the dialog
                onTaskAdded()
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
