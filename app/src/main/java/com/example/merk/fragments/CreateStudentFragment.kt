package com.example.merk.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.merk.R
import com.example.merk.data.api.RetrofitClient
import com.example.merk.data.models.CreateStudentRequest
import kotlinx.coroutines.launch

class CreateStudentFragment : Fragment() {

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        i.inflate(R.layout.fragment_create_student, c, false)

    override fun onViewCreated(view: View, s: Bundle?) {
        super.onViewCreated(view, s)
        val etLogin = view.findViewById<EditText>(R.id.etLogin)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPhone = view.findViewById<EditText>(R.id.etPhone)
        val etPass = view.findViewById<EditText>(R.id.etPassword)
        val btnCreate = view.findViewById<Button>(R.id.btnCreate)
        val pb = view.findViewById<ProgressBar>(R.id.progressBar)
        val tvResult = view.findViewById<TextView>(R.id.tvResult)

        btnCreate.setOnClickListener {
            val login = etLogin.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val pass = etPass.text.toString().trim()

            if (listOf(login, email, phone, pass).any { it.isEmpty() }) {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            pb.visibility = View.VISIBLE
            btnCreate.isEnabled = false
            tvResult.visibility = View.GONE

            lifecycleScope.launch {
                try {
                    val resp = RetrofitClient.api.createStudent(
                        CreateStudentRequest(login, pass, email, phone)
                    )
                    if (resp.isSuccessful) {
                        tvResult.text = "Студент $login создан успешно"
                        tvResult.setTextColor(resources.getColor(R.color.success_green, null))
                        tvResult.visibility = View.VISIBLE
                        etLogin.text.clear(); etEmail.text.clear()
                        etPhone.text.clear(); etPass.text.clear()
                    } else {
                        tvResult.text = resp.errorBody()?.string() ?: "Ошибка"
                        tvResult.setTextColor(resources.getColor(R.color.error_red, null))
                        tvResult.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    tvResult.text = "Ошибка: ${e.message}"
                    tvResult.setTextColor(resources.getColor(R.color.error_red, null))
                    tvResult.visibility = View.VISIBLE
                } finally {
                    pb.visibility = View.GONE
                    btnCreate.isEnabled = true
                }
            }
        }
    }
}
