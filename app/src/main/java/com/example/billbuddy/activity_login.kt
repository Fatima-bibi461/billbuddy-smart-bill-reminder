package com.example.billbuddy

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val sharedPref = getSharedPreferences("BillBuddyLoginPrefs", Context.MODE_PRIVATE)

        val tilEmail = findViewById<TextInputLayout>(R.id.tilEmail)
        val tilPassword = findViewById<TextInputLayout>(R.id.tilPassword)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val tvSavedInfo = findViewById<TextView>(R.id.tvSavedInfo)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnShow = findViewById<Button>(R.id.btnShow)
        val btnClear = findViewById<Button>(R.id.btnClear)

        btnLogin.setOnClickListener {
            tilEmail.error = null
            tilPassword.error = null

            val email = etEmail.text?.toString()?.trim().orEmpty()
            val password = etPassword.text?.toString().orEmpty()

            if (email.isEmpty()) {
                tilEmail.error = "Email is required"
                Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                tilPassword.error = "Password is required"
                Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 1. Save data in SharedPreferences
            sharedPref.edit().apply {
                putString("email", email)
                putString("password", password)
                apply()
            }

            Toast.makeText(this, "Login successful, opening Dashboard", Toast.LENGTH_SHORT).show()

            // 2. Open DashboardActivity
            val intent = Intent(this, DashboardActivity::class.java).apply {
                putExtra("user_email", email)
            }
            startActivity(intent)
            finish()
        }

        btnShow.setOnClickListener {
            val savedEmail = sharedPref.getString("email", "Not saved")
            val savedPassword = sharedPref.getString("password", "Not saved")

            tvSavedInfo.visibility = View.VISIBLE
            tvSavedInfo.text = "Saved Email: $savedEmail\nSaved Password: $savedPassword"
            Toast.makeText(this, "Email: $savedEmail", Toast.LENGTH_SHORT).show()
        }

        btnClear.setOnClickListener {
            sharedPref.edit().clear().apply()

            etEmail.setText("")
            etPassword.setText("")
            tilEmail.error = null
            tilPassword.error = null
            tvSavedInfo.text = ""
            tvSavedInfo.visibility = View.GONE

            Toast.makeText(this, "Data cleared", Toast.LENGTH_SHORT).show()
        }
    }
}