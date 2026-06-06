package com.example.merk.fragments

import com.example.merk.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.merk.data.api.RetrofitClient
import com.example.merk.data.models.CreateAssignmentRequest
import kotlinx.coroutines.launch

class CreateAssignmentFragment : Fragment() {

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        i.inflate(com.example.merk.R.layout.fragment_create_assignment, c, false)

    override fun onViewCreated(view: View, s: Bundle?) {
        super.onViewCreated(view, s)

        val etTitle = view.findViewById<EditText>(com.example.merk.R.id.etTitle)
        val etDescription = view.findViewById<EditText>(com.example.merk.R.id.etDescription)
        val etCorrectAnswer = view.findViewById<EditText>(com.example.merk.R.id.etCorrectAnswer)
        val spinnerType = view.findViewById<Spinner>(com.example.merk.R.id.spinnerType)
        val btnCreate = view.findViewById<Button>(com.example.merk.R.id.btnCreate)
        val pb = view.findViewById<ProgressBar>(com.example.merk.R.id.progressBar)
        val tvResult = view.findViewById<TextView>(com.example.merk.R.id.tvResult)

        // Типы заданий
        val types = arrayOf("Code", "Test")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = adapter

        btnCreate.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val answer = etCorrectAnswer.text.toString().trim()
            val type = spinnerType.selectedItem.toString()

            if (title.isEmpty() || description.isEmpty() || answer.isEmpty()) {
                tvResult.text = "Заполните все обязательные поля"
                tvResult.setTextColor(resources.getColor(com.example.merk.R.color.error_red, null))
                tvResult.visibility = View.VISIBLE
                return@setOnClickListener
            }

            pb.visibility = View.VISIBLE
            btnCreate.isEnabled = false

            lifecycleScope.launch {
                try {
                    val resp = RetrofitClient.api.createAssignment(
                        CreateAssignmentRequest(title, description, type, answer)
                    )
                    if (resp.isSuccessful) {
                        tvResult.text = "Задание создано успешно!"
                        tvResult.setTextColor(resources.getColor(com.example.merk.R.color.success_green, null))
                        tvResult.visibility = View.VISIBLE
                        etTitle.text.clear()
                        etDescription.text.clear()
                        etCorrectAnswer.text.clear()
                    } else {
                        tvResult.text = "Ошибка: ${resp.errorBody()?.string()}"
                        tvResult.setTextColor(resources.getColor(com.example.merk.R.color.error_red, null))
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