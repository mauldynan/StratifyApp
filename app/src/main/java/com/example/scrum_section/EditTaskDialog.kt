package com.example.scrum_section

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import com.example.scrum_section.data.TaskRepository
import com.example.scrum_section.model.Task

class EditTaskDialog(private val task: Task, private val onUpdated: () -> Unit) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                EditTaskDialogContent(
                    task = task,
                    onUpdateTask = { newName, newDeadline, newDept, newDescription ->
                        if (newName.isNotEmpty()) {
                            val updatedTask = task.copy(
                                name = newName,
                                deadline = newDeadline,
                                department = newDept,
                                description = newDescription
                            )
                            TaskRepository.updateTask(updatedTask)
                            Toast.makeText(requireContext(), "Task updated successfully!", Toast.LENGTH_SHORT).show()
                            onUpdated()
                            dismiss()
                        } else {
                            Toast.makeText(requireContext(), "Task's name not allowed empty!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.8).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskDialogContent(
    task: Task,
    onUpdateTask: (name: String, deadline: String, department: String, description: String) -> Unit
) {
    var name by remember { mutableStateOf(task.name) }
    var deadline by remember { mutableStateOf(task.deadline) }
    var department by remember { mutableStateOf(task.department) }
    var description by remember { mutableStateOf(task.description) }
    var nameError by remember { mutableStateOf(name.isEmpty()) }

    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Edit Task", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = it.isEmpty()
                },
                label = { Text("Task Name") },
                isError = nameError,
                modifier = Modifier.fillMaxWidth()
            )
            if (nameError) {
                Text("Task name cannot be empty", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = deadline,
                onValueChange = { deadline = it },
                label = { Text("Deadline") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = department,
                onValueChange = { department = it },
                label = { Text("Department") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onUpdateTask(name.trim(), deadline.trim(), department.trim(), description.trim()) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Update")
            }
        }
    }
}
