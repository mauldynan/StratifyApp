package com.example.stratify

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ForgotPasswordActivity : AppCompatActivity() {

    // Declaration
    private lateinit var sendToEmailButton: Button
    private lateinit var backButton: Button
    private lateinit var emailEditText: EditText
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Initialization
        backButton = findViewById(R.id.back_button)
        sendToEmailButton = findViewById(R.id.send_to_email_button)
        emailEditText = findViewById(R.id.email_input_edit_text)

        mAuth = Firebase.auth

        // Reset Button Listener
        sendToEmailButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (!TextUtils.isEmpty(email)) {
                sendPasswordReset(email)
            } else {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            }
        }

        // Back Button Listener
        backButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun sendPasswordReset(email: String) {

        mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener {

                // Change the Toast Message
                Toast.makeText(
                    this,
                    "If this email is registered, a password reset link has been sent. Please check your inbox and spam folder.",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

}