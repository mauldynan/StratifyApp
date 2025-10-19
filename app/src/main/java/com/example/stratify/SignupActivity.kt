package com.example.stratify

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.stratify.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.regex.Pattern

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignupBinding

    // Regex pattern for strong password validation.
    private val passwordPattern = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    )

    /**
     * Initializes the activity, sets up the layout, and configures event listeners.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Sign Up"

        auth = FirebaseAuth.getInstance()

        // Set up real-time password validation as the user types.
        setupPasswordValidation()

        // Set a click listener for the sign-in link to navigate to the LoginActivity.
        binding.signInTextLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Set a click listener for the sign-up button.
        binding.signupButton.setOnClickListener {
            val email = binding.emailInputEdittext.text.toString().trim()
            val password = binding.passwordInputEdittext.text.toString().trim()
            val confirmPassword = binding.confirmPasswordInputEdittext.text.toString().trim()

            // Validate that all fields are filled.
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate that the passwords match.
            if (password != confirmPassword) {
                binding.passwordValidationText.visibility = View.VISIBLE
                binding.passwordValidationText.setTextColor(Color.RED)
                binding.passwordValidationText.text = "Passwords do not match."
                return@setOnClickListener
            }

            // Validate that the password meets the strength criteria.
            if (!passwordPattern.matcher(password).matches()) {
                binding.passwordValidationText.visibility = View.VISIBLE
                binding.passwordValidationText.setTextColor(Color.RED)
                binding.passwordValidationText.text = "Password is not strong enough."
                return@setOnClickListener
            }

            // Proceed with Firebase user creation.
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Sign up failed: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Sets up a TextWatcher to provide real-time feedback on password strength as the user types.
     */
    private fun setupPasswordValidation() {
        binding.passwordInputEdittext.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val password = s.toString().trim()
                if (password.isEmpty()) {
                    // Hide the validation text if the password field is empty.
                    binding.passwordValidationText.visibility = View.INVISIBLE
                    return
                }
                binding.passwordValidationText.visibility = View.VISIBLE
                // Check if the password matches the pattern.
                if (passwordPattern.matcher(password).matches()) {
                    // If valid, show a green message.
                    binding.passwordValidationText.setTextColor(Color.GREEN)
                    binding.passwordValidationText.text = "Password is valid!"
                } else {
                    // If invalid, show a red message with the requirements.
                    binding.passwordValidationText.setTextColor(Color.RED)
                    binding.passwordValidationText.text = "Password must be at least 8 characters long, contain a lowercase, uppercase, number, and symbol (@, $, !, %, *, ?, & only)."
                }
            }
        })
    }
}
