package com.example.merk

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.merk.data.api.RetrofitClient
import com.example.merk.data.models.LoginRequest
import com.example.merk.data.models.LoginResponse
import kotlinx.coroutines.launch
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("merk_session", MODE_PRIVATE)
        if (prefs.getBoolean("isLoggedIn", false)) {
            navigateByRole(prefs.getString("role", "") ?: "")
            return
        }

        val etLogin = findViewById<EditText>(R.id.etLogin)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnReg = findViewById<Button>(R.id.btnRegistration)
        val tvError = findViewById<TextView>(R.id.tvError)
        val progress = findViewById<ProgressBar>(R.id.progressBar)

        btnLogin.setOnClickListener {
            val login = etLogin.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            if (login.isEmpty() || pass.isEmpty()) {
                tvError.text = "Заполните все поля"
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            tvError.visibility = View.GONE
            progress.visibility = View.VISIBLE
            btnLogin.isEnabled = false; btnReg.isEnabled = false

            lifecycleScope.launch {
                try {
                    val resp = RetrofitClient.api.login(LoginRequest(login, pass))
                    if (resp.isSuccessful) {
                        val body = resp.body()!!
                        prefs.edit()
                            .putInt("userId", body.userId)
                            .putString("login", body.login)
                            .putString("role", body.role)
                            .putBoolean("isLoggedIn", true)
                            .apply()
                        navigateByRole(body.role)
                    } else {
                        tvError.text = "Неверный логин или пароль"
                        tvError.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    tvError.text = "Ошибка соединения: ${e.message}"
                    tvError.visibility = View.VISIBLE
                } finally {
                    progress.visibility = View.GONE
                    btnLogin.isEnabled = true; btnReg.isEnabled = true
                }
            }
        }

        btnReg.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }
    }

    private fun navigateByRole(role: String) {
        val cls = if (role == "Teacher") TeacherActivity::class.java else HomeActivity::class.java
        startActivity(Intent(this, cls).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }
}
