package com.example.stratify

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()

        setContent {
            ForgotPasswordScreen(
                onSendClicked = { email ->
                    if (email.isNotBlank()) {
                        sendPasswordReset(email)
                    } else {
                        Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                    }
                },
                onBackClicked = {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            )
        }
    }

    private fun sendPasswordReset(email: String) {
        mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener {
                Toast.makeText(
                    this,
                    "If this email is registered, a reset link has been sent. Check inbox/spam.",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onSendClicked: (String) -> Unit,
    onBackClicked: () -> Unit
) {
    var email by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {

            // TOP glow
            Image(
                painter = painterResource(id = R.drawable.elips),
                contentDescription = null,
                modifier = Modifier
                    .size(800.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 200.dp, y = (-200).dp)
                    .alpha(0.3f)
            )

            // BOTTOM glow
            Image(
                painter = painterResource(id = R.drawable.elips),
                contentDescription = null,
                modifier = Modifier
                    .size(800.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-200).dp, y = 200.dp)
                    .alpha(0.3f)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Back Arrow
                IconButton(
                    onClick = onBackClicked,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(top = 40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }

                Image(
                    painter = painterResource(id = R.drawable.logo_stratify2),
                    contentDescription = "App Logo",
                    contentScale = ContentScale.FillWidth, // tetap proporsional
                    modifier = Modifier
                        .padding(top = 64.dp)
                        .fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Forgot Password",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.app_yellow),
                    modifier = Modifier
                        .padding(top = 8.dp)
                )

                Text(
                    text = "Provide your accounts email for which you want to reset your password!",
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(Modifier.height(24.dp))

                // Email field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(id = R.color.app_yellow),
                        unfocusedBorderColor = Color.Gray
                    )
                )

                Spacer(Modifier.height(32.dp))

                // SEND button
                Button(
                    onClick = { onSendClicked(email) },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.app_yellow)
                    )
                ) {
                    Text(
                        text = "RESET PASSWORD",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
