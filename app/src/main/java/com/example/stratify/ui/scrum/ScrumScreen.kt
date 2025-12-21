package com.example.stratify.ui.scrum

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// --- Colors ---
private val maroonPrimary = Color(0xFF8B0000)
private val textYellow = Color(0xFFF6C761)
private val lightGray = Color(0xFFF0F0F0)
private val pureWhite = Color(0xFFFFFFFF)

// --- Helper Logic for Auto Estimation ---
fun calculateEstimation(deadlineString: String): String {
    return try {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val deadlineDate = sdf.parse(deadlineString) ?: return ""
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val diffInMillies = deadlineDate.time - today.time
        val diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS)

        when {
            diffInDays < 0 -> "Overdue"
            diffInDays == 0L -> "Today"
            diffInDays < 7 -> "$diffInDays Days"
            else -> {
                val weeks = diffInDays / 7
                val remainingDays = diffInDays % 7
                if (remainingDays == 0L) "$weeks Weeks" else "$weeks Weeks $remainingDays Days"
            }
        }
    } catch (e: Exception) { "" }
}

// NOTE: Task and TaskStatus are now imported from Task.kt

class ScrumViewModel : ViewModel() {
    private val _tasks = mutableStateListOf<Task>()
    val tasks: List<Task> = _tasks
    private var lastDeletedTask: Pair<Int, Task>? = null

    val activeTaskCount: Int get() = _tasks.count { it.status != TaskStatus.DONE }
    val progressPercentage: Int get() = if (_tasks.isEmpty()) 0 else (_tasks.count { it.status == TaskStatus.DONE } * 100 / _tasks.size)

    fun addTask(name: String, description: String, estimation: String, deadline: String) {
        val newId = (_tasks.maxOfOrNull { it.id } ?: 0) + 1
        _tasks.add(Task(newId, name, description, estimation, deadline, TaskStatus.TODO))
    }

    fun updateTask(updatedTask: Task) {
        val index = _tasks.indexOfFirst { it.id == updatedTask.id }
        if (index != -1) _tasks[index] = updatedTask
    }

    fun deleteTask(task: Task) {
        val index = _tasks.indexOf(task)
        if (index != -1) {
            lastDeletedTask = Pair(index, task)
            _tasks.removeAt(index)
        }
    }

    fun undoDelete() {
        lastDeletedTask?.let { (index, task) -> _tasks.add(index, task) }
    }
}

// --- Main Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrumScreen(viewModel: ScrumViewModel = viewModel()) {
    val tasks = viewModel.tasks
    var selectedStatusFilter by remember { mutableStateOf<TaskStatus?>(null) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val filteredTasks = tasks.filter { selectedStatusFilter == null || it.status == selectedStatusFilter }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onTaskAdded = { name, desc, est, dead ->
                viewModel.addTask(name, desc, est, dead)
                showAddTaskDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Scrum Board", fontWeight = FontWeight.Bold, color = textYellow) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = maroonPrimary)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddTaskDialog = true }, containerColor = maroonPrimary) {
                Icon(Icons.Default.Add, contentDescription = null, tint = textYellow)
            }
        },
        containerColor = pureWhite
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("SPRINT #${viewModel.activeTaskCount} • ACTIVE", color = textYellow, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
            ProgressCard(progress = viewModel.progressPercentage)
            Spacer(modifier = Modifier.height(24.dp))
            TaskSectionHeader(taskCount = filteredTasks.size, onFilterSelected = { selectedStatusFilter = it })
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredTasks, key = { it.id }) { task ->
                    TaskItem(
                        task = task,
                        onUpdateTask = { viewModel.updateTask(it) },
                        onDeleteTask = {
                            viewModel.deleteTask(task)
                            coroutineScope.launch {
                                val res = snackbarHostState.showSnackbar(
                                    message = "Task deleted",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short
                                )
                                if (res == SnackbarResult.ActionPerformed) viewModel.undoDelete()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskItem(task: Task, onUpdateTask: (Task) -> Unit, onDeleteTask: () -> Unit) {
    var isEditing by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var taskName by remember(task.name) { mutableStateOf(task.name) }
    var taskDescription by remember(task.description) { mutableStateOf(task.description) }
    var taskEstimation by remember(task.estimation) { mutableStateOf(task.estimation) }
    var taskDeadline by remember(task.deadline) { mutableStateOf(task.deadline) }
    var taskStatus by remember(task.status) { mutableStateOf(task.status) }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(onConfirm = { onDeleteTask(); showDeleteDialog = false }, onDismiss = { showDeleteDialog = false })
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = pureWhite)
    ) {
        if (isEditing) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = taskName, onValueChange = { taskName = it }, label = { Text("Task Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = taskDescription, onValueChange = { taskDescription = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = taskEstimation, onValueChange = {}, label = { Text("Est.") }, modifier = Modifier.weight(1f), readOnly = true)
                    DatePickerField(value = taskDeadline, onValueChange = { taskDeadline = it; taskEstimation = calculateEstimation(it) }, label = "Deadline", modifier = Modifier.weight(1.5f))
                }
                StatusSelector(selectedStatus = taskStatus, onStatusSelected = { taskStatus = it })
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { isEditing = false }) { Text("Cancel") }
                    Button(onClick = {
                        onUpdateTask(task.copy(name = taskName, description = taskDescription, estimation = taskEstimation, deadline = taskDeadline, status = taskStatus))
                        isEditing = false
                    }, colors = ButtonDefaults.buttonColors(containerColor = maroonPrimary)) { Text("Save") }
                }
            }
        } else {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(status = task.status)
                    Box {
                        IconButton(onClick = { showOptionsMenu = true }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.MoreVert, null, tint = Color.Gray) }
                        DropdownMenu(expanded = showOptionsMenu, onDismissRequest = { showOptionsMenu = false }, modifier = Modifier.background(pureWhite)) {
                            DropdownMenuItem(text = { Text("Edit") }, onClick = { isEditing = true; showOptionsMenu = false })
                            DropdownMenuItem(text = { Text("Delete") }, onClick = { showDeleteDialog = true; showOptionsMenu = false })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(task.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = maroonPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(task.description, fontSize = 14.sp, color = Color.Gray, lineHeight = 20.sp)
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = lightGray.copy(alpha = 0.5f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    InfoRow(icon = Icons.Default.Timer, text = "Estimate: ${task.estimation}")
                    InfoRow(icon = Icons.Default.CalendarToday, text = task.deadline, highlight = true)
                }
            }
        }
    }
}

// --- Components ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var showSheet by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { if (it is PressInteraction.Release) showSheet = true }
    }

    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        modifier = modifier,
        readOnly = true,
        trailingIcon = { Icon(Icons.Default.DateRange, null, tint = maroonPrimary) },
        interactionSource = interactionSource,
        shape = RoundedCornerShape(12.dp)
    )

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = pureWhite,
            dragHandle = { BottomSheetDefaults.DragHandle(color = lightGray) }
        ) {
            val datePickerState = rememberDatePickerState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, start = 8.dp, end = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Select Deadline", fontWeight = FontWeight.Bold, color = maroonPrimary, modifier = Modifier.padding(vertical = 12.dp), fontSize = 18.sp)

                DatePicker(
                    state = datePickerState,
                    showModeToggle = false,
                    title = null,
                    headline = null,
                    colors = DatePickerDefaults.colors(
                        containerColor = pureWhite,
                        selectedDayContainerColor = maroonPrimary,
                        todayContentColor = maroonPrimary,
                        todayDateBorderColor = maroonPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().scale(1.0f)
                )

                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { showSheet = false }) { Text("Cancel", color = Color.Gray) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            datePickerState.selectedDateMillis?.let {
                                onValueChange(SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(it)))
                            }
                            showSheet = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = maroonPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("Confirm") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusSelector(selectedStatus: TaskStatus, onStatusSelected: (TaskStatus) -> Unit) {
    Column {
        Text("Status", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TaskStatus.entries.forEach { status ->
                FilterChip(
                    selected = status == selectedStatus,
                    onClick = { onStatusSelected(status) },
                    label = { Text(status.displayName, fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = maroonPrimary.copy(0.1f), selectedLabelColor = maroonPrimary)
                )
            }
        }
    }
}

@Composable
fun ProgressCard(progress: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = pureWhite)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.EmojiEvents, null, modifier = Modifier.size(40.dp).background(maroonPrimary, CircleShape).padding(8.dp), tint = textYellow)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("SPRINT PROGRESS", fontSize = 12.sp, color = Color.Gray)
                Text("$progress% Completed", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = maroonPrimary)
            }
        }
    }
}

@Composable
fun TaskSectionHeader(taskCount: Int, onFilterSelected: (TaskStatus?) -> Unit) {
    var showSortMenu by remember { mutableStateOf(false) }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("All Tasks", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = maroonPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Surface(color = textYellow, shape = CircleShape, modifier = Modifier.size(24.dp)) {
                Box(contentAlignment = Alignment.Center) { Text("$taskCount", color = maroonPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            }
        }
        Box {
            Button(
                onClick = { showSortMenu = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFA726).copy(alpha = 0.1f),
                    contentColor = maroonPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(Icons.Default.FilterList, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Filter", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }, modifier = Modifier.background(pureWhite)) {
                DropdownMenuItem(text = { Text("All") }, onClick = { onFilterSelected(null); showSortMenu = false })
                TaskStatus.entries.forEach { status ->
                    DropdownMenuItem(text = { Text(status.displayName) }, onClick = { onFilterSelected(status); showSortMenu = false })
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: TaskStatus) {
    val color = when (status) {
        TaskStatus.TODO -> Color(0xFFFFA726)
        TaskStatus.IN_PROGRESS -> Color(0xFF42A5F5)
        TaskStatus.TO_VERIFY -> Color(0xFFEF5350)
        TaskStatus.DONE -> Color(0xFF66BB6A)
    }
    Surface(color = color.copy(0.1f), shape = RoundedCornerShape(8.dp)) {
        Text(status.displayName.uppercase(), modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), color = color, fontWeight = FontWeight.Bold, fontSize = 10.sp)
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier.background(if (highlight) textYellow.copy(0.2f) else Color.Transparent, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = if (highlight) maroonPrimary else Color.Gray, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, color = if (highlight) maroonPrimary else Color.Gray, fontSize = 12.sp)
    }
}

@Composable
private fun DeleteConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = pureWhite,
        title = { Text("Delete Task", fontWeight = FontWeight.Bold, color = maroonPrimary) },
        text = { Text("Are you sure you want to delete this task?") },
        confirmButton = { Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(maroonPrimary)) { Text("Delete") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) } }
    )
}

@Composable
private fun AddTaskDialog(onDismiss: () -> Unit, onTaskAdded: (String, String, String, String) -> Unit) {
    var taskName by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }
    var taskEstimation by remember { mutableStateOf("") }
    var taskDeadline by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = pureWhite,
        title = { Text("Add Task", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = taskName, onValueChange = { taskName = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = taskDescription, onValueChange = { taskDescription = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = taskEstimation, onValueChange = {}, label = { Text("Est.") }, modifier = Modifier.weight(1f), readOnly = true)
                    DatePickerField(value = taskDeadline, onValueChange = { taskDeadline = it; taskEstimation = calculateEstimation(it) }, label = "Deadline", modifier = Modifier.weight(1.5f))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onTaskAdded(taskName, taskDescription, taskEstimation, taskDeadline) },
                enabled = taskName.isNotBlank() && taskDeadline.isNotBlank()
            ) { Text("Add") }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ScrumPreview() {
    val vm = remember { ScrumViewModel() }
    LaunchedEffect(Unit) {
        if (vm.tasks.isEmpty()) {
            vm.addTask("Final Polish", "White calendar background and large scale implemented.", "1 Day", "21 Dec 2025")
        }
    }
    ScrumScreen(vm)
}