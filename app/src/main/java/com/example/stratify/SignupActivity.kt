package com.example.stratify

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import java.util.regex.Pattern

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            SignupScreen(
                onSignupClicked = { email, password ->
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Sign up failed: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onSigninClicked = {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            )
        }
    }
}

@Composable
fun SignupScreen(
    onSignupClicked: (String, String) -> Unit,
    onSigninClicked: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordValidationResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    val passwordPattern = remember {
        Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$")
    }

    // Real-time password validation
    LaunchedEffect(password) {
        if (password.isEmpty()) {
            passwordValidationResult = null
            return@LaunchedEffect
        }
        val isValid = passwordPattern.matcher(password).matches()
        passwordValidationResult = if (isValid) {
            Pair(true, "Password is valid!")
        } else {
            Pair(false, "Password must be at least 8 characters long, contain a lowercase, uppercase, number, and symbol (@, $, !, %, *, ?, & only).")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Sign Up", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        passwordValidationResult?.let { (isValid, message) ->
            Text(
                text = message,
                color = if (isValid) Color.Green else Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        Spacer(Modifier.height(24.dp))

        var formError by remember { mutableStateOf<String?>(null) }
        Button(
            onClick = {
                formError = when {
                    email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> "Please fill in all fields."
                    password != confirmPassword -> "Passwords do not match."
                    passwordValidationResult?.first == false -> "Password is not strong enough."
                    else -> null
                }
                if (formError == null) {
                    onSignupClicked(email, password)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Up")
        }

        formError?.let {
            Text(
                text = it,
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(Modifier.height(16.dp))
        val annotatedString = buildAnnotatedString {
            append("Already have an account? ")
            pushStringAnnotation(tag = "SIGNIN", annotation = "signin")
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                append("Sign In")
            }
            pop()
        }
        ClickableText(
            text = annotatedString,
            onClick = { offset ->
                annotatedString.getStringAnnotations(tag = "SIGNIN", start = offset, end = offset)
                    .firstOrNull()?.let {
                        onSigninClicked()
                    }
            }
        )
    }
}
