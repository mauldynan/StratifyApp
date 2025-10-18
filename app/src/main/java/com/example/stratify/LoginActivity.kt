package com.example.stratify

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.stratify.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Sign In"

        // Initialize FirebaseAuth instance
        auth = FirebaseAuth.getInstance()

        // Handle Sign In button click
        binding.loginButton.setOnClickListener {
            val email = binding.emailInputEditText.text.toString().trim()
            val password = binding.passwordInputEditText.text.toString().trim()

            // Clear any previous error messages
            binding.loginErrorText.visibility = View.GONE

            // Validate fields
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Attempt to sign in with Firebase
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in successful, navigate to the correct MainActivity
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                        
                        // Use a specific Intent to avoid ambiguity between the two MainActivity files
                        val intent = Intent(this, com.example.stratify.MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish() // Finish this activity so the user can't go back
                    }
                }
                .addOnFailureListener { exception ->
                    // Sign in failed, show appropriate error message
                    binding.loginErrorText.visibility = View.VISIBLE

                    when (exception) {
                        is FirebaseAuthInvalidUserException -> {
                            // User not found
                            binding.loginErrorText.text = "Email is not registered."
                        }
                        is FirebaseAuthInvalidCredentialsException -> {
                            // Incorrect password
                            binding.loginErrorText.text = "Incorrect password. Please try again."
                        }
                        else -> {
                            // Other errors
                            binding.loginErrorText.text = "Login failed: ${exception.localizedMessage}"
                        }
                    }
                }
        }

        // Handle Sign Up link click
        binding.signUpTextLink.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        // Handle Forgot Password link click
        binding.forgotPasswordText.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }
}