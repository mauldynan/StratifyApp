package com.example.stratify

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.stratify.ui.theme.MaroonPrimary
import com.example.stratify.ui.theme.StratifyTheme
import kotlinx.coroutines.delay

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StratifyTheme {
                SplashScreen {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(3000)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaroonPrimary),
        contentAlignment = Alignment.Center
    ) {
        // Top ellipse
        Image(
            painter = painterResource(id = R.drawable.elips),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-500).dp)
                .alpha(0.5f)
        )

        // Bottom ellipse
        Image(
            painter = painterResource(id = R.drawable.elips),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp)
                .align(Alignment.BottomCenter)
                .offset(y = 500.dp)
                .alpha(0.5f)
        )

        // Center Logo
        Image(
            painter = painterResource(id = R.drawable.logo_stratify),
            contentDescription = "App Logo",
            modifier = Modifier.size(200.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    StratifyTheme {
        SplashScreen(onTimeout = {})
    }
}
