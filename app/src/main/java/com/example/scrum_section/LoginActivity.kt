package com.example.scrum_section

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stratify.ForgotPasswordActivity
import com.example.stratify.MainActivity
import com.example.stratify.R
import com.example.stratify.SignupActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            var errorMessage by remember { mutableStateOf<String?>(null) }

            // The main entry point for the Compose UI
            LoginScreen(
                errorMessage = errorMessage,
                onLoginClicked = { email, password ->
                    loginUser(email, password) { error ->
                        errorMessage = error
                    }
                },
                onGoogleSignInClicked = {
                    // TODO: Implement Google Sign-In logic
                    Toast.makeText(this, "Google Sign-In not implemented", Toast.LENGTH_SHORT).show()
                },
                onForgotPasswordClicked = {
                    startActivity(Intent(this, ForgotPasswordActivity::class.java))
                },
                onSignUpClicked = {
                    startActivity(Intent(this, SignupActivity::class.java))
                },
                onClearError = {
                    errorMessage = null
                }
            )
        }
    }

    private fun loginUser(email: String, password: String, onLoginFailed: (String) -> Unit) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password.", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                // Failure is handled by the OnFailureListener
            }
            .addOnFailureListener { exception ->
                val error = when (exception) {
                    is FirebaseAuthInvalidUserException -> "Email is not registered."
                    is FirebaseAuthInvalidCredentialsException -> "Incorrect password. Please try again."
                    else -> "Login failed: ${exception.localizedMessage}"
                }
                onLoginFailed(error)
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    errorMessage: String?,
    onLoginClicked: (String, String) -> Unit,
    onGoogleSignInClicked: () -> Unit,
    onForgotPasswordClicked: () -> Unit,
    onSignUpClicked: () -> Unit,
    onClearError: () -> Unit
) {
    // State variables to hold the input field values
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Decorative ellipses from the original XML layout
            Image(
                painter = painterResource(id = R.drawable.ellipse),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .size(1000.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 600.dp, y = (-600).dp)
            )
            Image(
                painter = painterResource(id = R.drawable.ellipse),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .size(1000.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-600).dp, y = 600.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App Logo
                Image(
                    painter = painterResource(id = R.drawable.logo_stratify2),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .padding(top = 64.dp)
                        .height(100.dp)
                )

                Text(
                    text = "Login",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(top = 24.dp)
                )

                Text(
                    text = "Access your account",
                    color = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Email Input Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; onClearError() },
                    label = { Text("Email") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = errorMessage != null
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password Input Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; onClearError() },
                    label = { Text("Password") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        val description = if (passwordVisible) "Hide password" else "Show password"
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = errorMessage != null
                )

                // Forgot Password Text
                Text(
                    text = "Forgot Password?",
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp)
                        .clickable { onForgotPasswordClicked() }
                )

                // Error Message Display
                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }

                // Login Button
                Button(
                    onClick = { onLoginClicked(email, password) },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                        .height(50.dp)
                ) {
                    Text("Login", fontSize = 16.sp)
                }

                Text(
                    text = "OR",
                    modifier = Modifier.padding(vertical = 24.dp),
                    color = Color.Gray
                )

                // Google Sign-In Button
                OutlinedButton(
                    onClick = { onGoogleSignInClicked() },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google_scaled),
                            contentDescription = "Google Logo"
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Continue with Google", color = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Sign-up prompt
                Row(
                    modifier = Modifier.clickable { onSignUpClicked() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Don't have an account?")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Sign Up",
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen(
            errorMessage = null,
            onLoginClicked = { _, _ -> },
            onGoogleSignInClicked = { },
            onForgotPasswordClicked = { },
            onSignUpClicked = { },
            onClearError = { }
        )
    }
}
