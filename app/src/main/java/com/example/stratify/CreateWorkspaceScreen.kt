package com.example.stratify

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWorkspaceScreen(navController: NavController) {
    var workspaceId by remember { mutableStateOf("") }
    var workspaceName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var isCreating by remember { mutableStateOf(false) }

    val repository = remember { WorkspaceRepository() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val generateAndSetWorkspaceId: () -> Unit = {
        val allowedChars = ('A'..'Z') + ('0'..'9')
        workspaceId = (1..6)
            .map { allowedChars.random() }
            .joinToString("")
    }

    LaunchedEffect(Unit) {
        generateAndSetWorkspaceId()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Workspace") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = workspaceId,
                onValueChange = {},
                label = { Text("Workspace ID") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = generateAndSetWorkspaceId) {
                        Icon(Icons.Default.Refresh, contentDescription = "Generate New ID")
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = workspaceName,
                onValueChange = {
                    workspaceName = it
                    nameError = null
                },
                label = { Text("Nama Workspace") },
                modifier = Modifier.fillMaxWidth(),
                isError = nameError != null,
                singleLine = true,
                supportingText = { if (nameError != null) Text(nameError!!) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                isError = passwordError != null,
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                supportingText = { if (passwordError != null) Text(passwordError!!) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    var isValid = true
                    if (workspaceName.isBlank()) {
                        nameError = "Nama Workspace tidak boleh kosong"
                        isValid = false
                    }
                    if (password.isBlank()) {
                        passwordError = "Password tidak boleh kosong"
                        isValid = false
                    } else if (password.length < 4) {
                        passwordError = "Password minimal 4 karakter"
                        isValid = false
                    }

                    if (isValid) {
                        coroutineScope.launch {
                            isCreating = true
                            val result = repository.createWorkspace(workspaceId, workspaceName, password)
                            result.onSuccess {
                                Toast.makeText(context, "Successfully created workspace!", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }.onFailure { error ->
                                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                            }
                            isCreating = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isCreating
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create Workspace")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateWorkspaceScreenPreview() {
    CreateWorkspaceScreen(navController = NavController(LocalContext.current))
}
