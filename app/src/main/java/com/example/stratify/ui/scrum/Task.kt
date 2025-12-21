package com.example.stratify.ui.scrum
import com.example.stratify.ui.dashboard.EcommerceAppData
/**
 * Data model representing a single task in the Scrum board.
 */
data class Task(
    val id: Int,
    val name: String,
    val description: String,
    val estimation: String,
    val deadline: String,
    val status: TaskStatus,
    val remoteId: String? = null
)

/**
 * Enum representing the various stages of a task.
 */
enum class TaskStatus(val displayName: String) {
    TODO("To Do"),
    IN_PROGRESS("In Progress"),
    TO_VERIFY("To Verify"),
    DONE("Done")
}