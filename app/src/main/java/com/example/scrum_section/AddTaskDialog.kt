package com.example.scrum_section

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
// COMMENT OUT these imports to test
// import com.example.scrum_section.data.TaskRepository
// import com.example.scrum_section.model.Task
// import com.example.scrum_section.util.TaskStatus

class AddTaskDialog(private val onTaskAdded: () -> Unit) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    AddTaskDialogContent(
                        onDismiss = { dismiss() },
                        onAddTask = { name, deadline, department, description ->
                            // --- TEMPORARILY COMMENTED OUT FOR DEBUGGING ---
                            // We are checking if the compiler crash stops when we remove this logic.

                            /* val newTask = Task(
                                id = 123,
                                name = name,
                                createdBy = "You",
                                deadline = deadline,
                                department = department,
                                description = description,
                                status = TaskStatus.TODO
                            )
                            TaskRepository.addTask(newTask)
                            */

                            // Just close the dialog for now to test compilation
                            onTaskAdded()
                            dismiss()
                        }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}

// ... Keep AddTaskDialogContent exactly as it was ...
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialogContent(
    onDismiss: () -> Unit,
    onAddTask: (name: String, deadline: String, department: String, description: String) -> Unit
) {
    // ... (Paste your existing AddTaskDialogContent UI code here) ...
    // If you need me to paste the full UI code again, let me know.
    var name by remember { mutableStateOf("") }
    // ... (rest of UI code)
    Button(onClick = { onAddTask(name, "", "", "") }) { Text("Test Add") }
}