package com.example.stratify.ui.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.UUID

private val maroonPrimary = Color(0xFF8B0000)
private val goldAccent = Color(0xFFF6C761)
private val lightBg = Color(0xFFF8F9FB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWorkspaceScreen(
    onWorkspaceCreated: (String, String, String) -> Unit,
    onBackPressed: () -> Unit
) {
    var workspaceName by remember { mutableStateOf("") }

    // Generate Code dan Password otomatis saat layar dibuka
    val generatedCode = remember { (100000..999999).random().toString() }
    val generatedPassword = remember { UUID.randomUUID().toString().take(8).uppercase() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("CREATE WORKSPACE", fontSize = 16.sp, fontWeight = FontWeight.Black, color = goldAccent) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, null, tint = goldAccent)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = maroonPrimary)
            )
        },
        containerColor = lightBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Setup Your Team",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = maroonPrimary
            )
            Text(
                "Create a private space for your colleagues.",
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Workspace Name Input
            OutlinedTextField(
                value = workspaceName,
                onValueChange = { workspaceName = it },
                label = { Text("Workspace Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = maroonPrimary,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Generated Info Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "AUTO-GENERATED ACCESS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    AccessInfoItem(label = "Workspace ID/Code", value = generatedCode, icon = Icons.Default.Key)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = lightBg)
                    AccessInfoItem(label = "Access Password", value = generatedPassword, icon = Icons.Default.Lock)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { onWorkspaceCreated(workspaceName, generatedCode, generatedPassword) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = maroonPrimary),
                shape = RoundedCornerShape(16.dp),
                enabled = workspaceName.isNotBlank()
            ) {
                Text("CREATE WORKSPACE", fontWeight = FontWeight.Black, color = goldAccent)
            }
        }
    }
}

@Composable
fun AccessInfoItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = maroonPrimary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 11.sp, color = Color.Gray)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = maroonPrimary)
        }
    }
}

// --- PREVIEW ---

@Preview(showBackground = true)
@Composable
fun CreateWorkspaceScreenPreview() {
    MaterialTheme {
        CreateWorkspaceScreen(
            onWorkspaceCreated = { _, _, _ -> },
            onBackPressed = {}
        )
    }
}