package com.example.stratify.ui.scrum

data class Task(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val estimation: String = "",
    val deadline: String = "",
    val status: TaskStatus = TaskStatus.TODO
)
