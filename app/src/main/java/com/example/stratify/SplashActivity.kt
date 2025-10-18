package com.example.stratify

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    /**
     * Initializes the activity, sets the layout, and starts the delayed navigation.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Use a Handler to delay the transition to the next screen.
        // The delay is 3000 milliseconds (3 seconds).
        Handler(Looper.getMainLooper()).postDelayed({
            // Create an intent to navigate to LoginActivity.
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

            finish()
        }, 3000)
    }
}
