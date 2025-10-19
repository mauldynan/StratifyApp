package com.example.stratify

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay // (Opsional, jika kamu mau ada delay)
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        lifecycleScope.launch {
            // (Opsional: tambahkan delay jika ingin splash screen terlihat)
            // delay(1000)

            if (auth.currentUser == null) {
                // 1. User BELUM login -> Kirim ke Login
                goToActivity(LoginActivity::class.java)
            } else {
                // 2. User SUDAH login -> Kirim ke MainActivity
                goToActivity(MainActivity::class.java)
            }
        }
    }

    private fun goToActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Tutup SplashActivity
    }
}