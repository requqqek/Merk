package com.example.merk.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.merk.LoginActivity
import com.example.merk.R
import com.example.merk.data.api.ChangePasswordRequest
import com.example.merk.data.api.RetrofitClient
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        i.inflate(R.layout.fragment_profile, c, false)

    override fun onViewCreated(view: View, s: Bundle?) {
        super.onViewCreated(view, s)

        val prefs = requireContext().getSharedPreferences("merk_session", Context.MODE_PRIVATE)
        val userId = prefs.getInt("userId", 0)
        val role = prefs.getString("role", "") ?: ""

        view.findViewById<TextView>(R.id.tvLogin).text = prefs.getString("login", "—")
        view.findViewById<TextView>(R.id.tvRole).text =
            if (role == "Teacher") "Преподаватель" else "Студент"
        view.findViewById<TextView>(R.id.tvUserId).text = "ID: $userId"

        // Статистика только для студента
        val cardStats = view.findViewById<CardView>(R.id.cardStats)
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

        // Кнопка смены пароля
        view.findViewById<Button>(R.id.btnChangePassword).setOnClickListener {
            showChangePasswordDialog(userId)
        }

        // Выход
        view.findViewById<Button>(R.id.btnLogout).setOnClickListener {
            prefs.edit().clear().apply()
            startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
    }

    private fun loadStudentStats(tvAttempts: TextView, tvGrade: TextView, tvAvg: TextView) {
        val prefs = requireContext().getSharedPreferences("merk_session", Context.MODE_PRIVATE)
        val userId = prefs.getInt("userId", 0)

        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.getStudentStats(userId)
                if (resp.isSuccessful) {
                    val stats = resp.body()
                    tvAttempts.text = "${stats?.totalAttempts ?: 0}"
                    tvGrade.text = "${stats?.totalGrade ?: 0}/${stats?.totalMax ?: 0}"
                    tvAvg.text = "${stats?.averagePercent ?: 0.0}%"
                }
            } catch (e: Exception) {
                // тихо
            }
        }
    }

    private fun showChangePasswordDialog(userId: Int) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null)
        val etOld = dialogView.findViewById<EditText>(R.id.etOldPassword)
        val etNew = dialogView.findViewById<EditText>(R.id.etNewPassword)
        val etConfirm = dialogView.findViewById<EditText>(R.id.etConfirmPassword)

        AlertDialog.Builder(requireContext())
            .setTitle("Изменить пароль")
            .setView(dialogView)
            .setPositiveButton("Изменить") { dialog, _ ->
                val old = etOld.text.toString().trim()
                val newP = etNew.text.toString().trim()
                val confirm = etConfirm.text.toString().trim()

                when {
                    old.isEmpty() || newP.isEmpty() || confirm.isEmpty() ->
                        Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
                    newP.length < 6 ->
                        Toast.makeText(requireContext(), "Минимум 6 символов", Toast.LENGTH_SHORT).show()
                    newP != confirm ->
                        Toast.makeText(requireContext(), "Пароли не совпадают", Toast.LENGTH_SHORT).show()
                    else -> changePassword(userId, old, newP)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun changePassword(userId: Int, oldPassword: String, newPassword: String) {
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.changePassword(
                    ChangePasswordRequest(userId, oldPassword, newPassword)
                )
                if (resp.isSuccessful) {
                    Toast.makeText(requireContext(), "Пароль изменён!", Toast.LENGTH_LONG).show()
                } else {
                    val err = resp.errorBody()?.string() ?: "Ошибка ${resp.code()}"
                    Toast.makeText(requireContext(), err, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}