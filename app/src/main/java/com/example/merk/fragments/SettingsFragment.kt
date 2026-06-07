package com.example.merk.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.merk.R
import com.example.merk.data.api.ChangePasswordRequest
import com.example.merk.data.api.RetrofitClient
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        i.inflate(R.layout.fragment_settings, c, false)

    override fun onViewCreated(view: View, s: Bundle?) {
        super.onViewCreated(view, s)

        val prefs = requireContext().getSharedPreferences("merk_session", Context.MODE_PRIVATE)
        val userId = prefs.getInt("userId", 0)
        val role = prefs.getString("role", "") ?: ""

        val btnCreateAssignment = view.findViewById<Button>(R.id.btnCreateAssignment)
        val btnChangePassword = view.findViewById<Button>(R.id.btnChangePassword)

        // Показываем кнопку "Создать задание" только для учителя
        if (role == "Teacher") {
            btnCreateAssignment.visibility = View.VISIBLE
        } else {
            btnCreateAssignment.visibility = View.GONE
        }

        // Кнопка создания задания
        btnCreateAssignment.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, CreateAssignmentFragment())
                .addToBackStack(null)
                .commit()
        }

        // Кнопка смены пароля
        btnChangePassword.setOnClickListener {
            showChangePasswordDialog(userId)
        }
    }

    private fun showChangePasswordDialog(userId: Int) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_change_password, null)

        val etOldPassword = dialogView.findViewById<EditText>(R.id.etOldPassword)
        val etNewPassword = dialogView.findViewById<EditText>(R.id.etNewPassword)
        val etConfirmPassword = dialogView.findViewById<EditText>(R.id.etConfirmPassword)

        AlertDialog.Builder(requireContext())
            .setTitle("Изменить пароль")
            .setView(dialogView)
            .setPositiveButton("Изменить") { dialog, _ ->
                val oldPassword = etOldPassword.text.toString().trim()
                val newPassword = etNewPassword.text.toString().trim()
                val confirmPassword = etConfirmPassword.text.toString().trim()

                when {
                    oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty() -> {
                        Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
                    }
                    newPassword.length < 6 -> {
                        Toast.makeText(requireContext(), "Пароль должен содержать минимум 6 символов", Toast.LENGTH_SHORT).show()
                    }
                    newPassword != confirmPassword -> {
                        Toast.makeText(requireContext(), "Пароли не совпадают", Toast.LENGTH_SHORT).show()
                    }
                    else -> changePassword(userId, oldPassword, newPassword)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun changePassword(userId: Int, oldPassword: String, newPassword: String) {
        lifecycleScope.launch {
            try {
                val request = ChangePasswordRequest(
                    userId = userId,
                    oldPassword = oldPassword,
                    newPassword = newPassword
                )

                val response = RetrofitClient.api.changePassword(request)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "✓ Пароль успешно изменён!", Toast.LENGTH_LONG).show()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Ошибка ${response.code()}"
                    Toast.makeText(requireContext(), errorBody, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка сети: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}