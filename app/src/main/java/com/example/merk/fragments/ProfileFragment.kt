package com.example.merk.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.merk.LoginActivity
import com.example.merk.R
import com.example.merk.data.api.RetrofitClient
import com.example.merk.data.models.UpdateProfileRequest
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        i.inflate(R.layout.fragment_profile, c, false)

    override fun onViewCreated(view: View, s: Bundle?) {
        super.onViewCreated(view, s)

        val prefs = requireContext().getSharedPreferences("merk_session", Context.MODE_PRIVATE)
        val userId = prefs.getInt("userId", 0)
        val role = prefs.getString("role", "") ?: ""

        val tvLogin = view.findViewById<TextView>(R.id.tvLogin)
        val tvRole = view.findViewById<TextView>(R.id.tvRole)
        val tvUserId = view.findViewById<TextView>(R.id.tvUserId)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPhone = view.findViewById<EditText>(R.id.etPhone)
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        val tvResult = view.findViewById<TextView>(R.id.tvResult)
        val cardStats = view.findViewById<CardView>(R.id.cardStats)

        tvLogin.text = prefs.getString("login", "—")
        tvRole.text = if (prefs.getString("role", "") == "Teacher") "Преподаватель" else "Студент"
        tvUserId.text = "ID: $userId"

        // Загружаем профиль с сервера
        loadProfile(userId, etEmail, etPhone, tvResult)

        if (role == "Student") {
            cardStats.visibility = View.VISIBLE
            loadStudentStats(
                view.findViewById(R.id.tvTotalAttempts),
                view.findViewById(R.id.tvTotalGrade),
                view.findViewById(R.id.tvAvgPercent)
            )
        } else {
            cardStats.visibility = View.GONE
        }

        // Сохранение изменений
        btnSave.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            updateProfile(userId, email, phone, tvResult)
        }


        // Выход
        btnLogout.setOnClickListener {
            prefs.edit().clear().apply()
            startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
    }

    private fun loadProfile(userId: Int, etEmail: EditText, etPhone: EditText, tvResult: TextView) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getProfile(userId)
                if (response.isSuccessful) {
                    val profile = response.body()
                    etEmail.setText(profile?.email ?: "")
                    etPhone.setText(profile?.phone ?: "")
                } else {
                    tvResult.text = "Ошибка загрузки профиля"
                    tvResult.setTextColor(resources.getColor(R.color.error_red, null))
                    tvResult.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                tvResult.text = "Ошибка сети: ${e.message}"
                tvResult.setTextColor(resources.getColor(R.color.error_red, null))
                tvResult.visibility = View.VISIBLE
            }
        }
    }

    private fun updateProfile(userId: Int, email: String, phone: String, tvResult: TextView) {
        tvResult.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val request = UpdateProfileRequest(
                    email = email.ifEmpty { null },
                    phone = phone.ifEmpty { null },
                    password = null
                )

                val response = RetrofitClient.api.updateProfile(userId, request)
                if (response.isSuccessful) {
                    tvResult.text = "✓ Профиль обновлён!"
                    tvResult.setTextColor(resources.getColor(R.color.success_green, null))
                    tvResult.visibility = View.VISIBLE
                } else {
                    tvResult.text = "Ошибка: ${response.code()}"
                    tvResult.setTextColor(resources.getColor(R.color.error_red, null))
                    tvResult.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                tvResult.text = "Ошибка сети: ${e.message}"
                tvResult.setTextColor(resources.getColor(R.color.error_red, null))
                tvResult.visibility = View.VISIBLE
            }
        }
    }

    private fun loadStudentStats(tvAttempts: TextView, tvGrade: TextView, tvAvg: TextView) {
        val prefs = requireContext().getSharedPreferences("merk_session", Context.MODE_PRIVATE)
        val userId = prefs.getInt("userId", 0)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getStudentStats(userId)
                if (response.isSuccessful) {
                    val stats = response.body()
                    tvAttempts.text = "${stats?.totalAttempts ?: 0}"
                    tvGrade.text = "${stats?.totalGrade ?: 0}/${stats?.totalMax ?: 0}"
                    tvAvg.text = "${stats?.averagePercent ?: 0.0}%"
                }
            } catch (e: Exception) {
                // тихо
            }
        }
    }
}