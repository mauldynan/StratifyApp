package com.example.scrum_section.model

import com.example.scrum_section.util.TaskStatus

data class Task(
    val id: String,
    var name: String,
    var createdBy: String,
    var deadline: String,
    var department: String,
    var description: String,
    var status: TaskStatus
)
