package com.example.merk

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.merk.data.api.RetrofitClient
import com.example.merk.data.models.CreateStudentRequest
import kotlinx.coroutines.launch

class RegistrationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)



        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etPass = findViewById<EditText>(R.id.etPassword)
        val etConfirm = findViewById<EditText>(R.id.etConfirmPassword)
        val etLogin = findViewById<EditText>(R.id.etLogin)
        val etName = findViewById<EditText>(R.id.etName)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnContinue = findViewById<Button>(R.id.btnContinue)
        val progress = findViewById<ProgressBar>(R.id.progressBar)

        btnBack.setOnClickListener { finish() }

        btnContinue.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val pass = etPass.text.toString().trim()
            val confirm = etConfirm.text.toString().trim()
            val login = etLogin.text.toString().trim()
            val name = etName.text.toString().trim()

            if (listOf(email, phone, pass, confirm, login, name).any { it.isEmpty() }) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!email.contains("@")) { etEmail.error = "Некорректный email"; return@setOnClickListener }
            if (pass.length < 6) { etPass.error = "Минимум 6 символов"; return@setOnClickListener }
            if (pass != confirm) { etConfirm.error = "Пароли не совпадают"; return@setOnClickListener }

            progress.visibility = View.VISIBLE
            btnContinue.isEnabled = false

            lifecycleScope.launch {
                try {
                    val resp = RetrofitClient.api.createStudent(
                        CreateStudentRequest(login, pass, email, phone)
                    )
                    if (resp.isSuccessful) {
                        Toast.makeText(this@RegistrationActivity,
                            "Регистрация успешна! Войдите в аккаунт.", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Toast.makeText(this@RegistrationActivity,
                            resp.errorBody()?.string() ?: "Ошибка", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@RegistrationActivity,
                        "Ошибка соединения: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    progress.visibility = View.GONE
                    btnContinue.isEnabled = true
                }
            }
        }
    }
}