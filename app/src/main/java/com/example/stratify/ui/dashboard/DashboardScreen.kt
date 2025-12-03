package com.example.stratify.ui.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.stratify.DonutChart

@Composable
fun DashboardScreen(
    onNavigateToFullAnalysis: () -> Unit,
    onNavigateToProfileEdit: () -> Unit // <--- 1. Parameter Baru
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Dashboard", modifier = Modifier.padding(bottom = 16.dp))

        // Chart kamu yang sudah ada
        DonutChart(
            positivePercent = 70f,
            negativePercent = 30f,
            totalReviews = 1200
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Tombol ke Full Analysis
        Button(
            onClick = onNavigateToFullAnalysis,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Full Analysis")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Tombol Baru ke Edit Profile
        OutlinedButton(
            onClick = onNavigateToProfileEdit,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Edit Profile")
        }
    }
}