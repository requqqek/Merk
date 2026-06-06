package com.example.merk.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.merk.R
import com.example.merk.data.api.RetrofitClient
import com.example.merk.data.models.CreateAssignmentRequest
import kotlinx.coroutines.launch

class CreateAssignmentFragment : Fragment() {

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        i.inflate(R.layout.fragment_create_assignment, c, false)

    override fun onViewCreated(view: View, s: Bundle?) {
        super.onViewCreated(view, s)

        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val etCorrectAnswer = view.findViewById<EditText>(R.id.etCorrectAnswer)
        val etOptions = view.findViewById<EditText>(R.id.etOptions)
        val spinnerType = view.findViewById<Spinner>(R.id.spinnerType)
        val btnCreate = view.findViewById<Button>(R.id.btnCreate)
        val pb = view.findViewById<ProgressBar>(R.id.progressBar)
        val tvResult = view.findViewById<TextView>(R.id.tvResult)

        val types = arrayOf("SingleChoice", "MultipleChoice", "TextInput", "Code")
        val typeNames = arrayOf("Выбор одного ответа", "Выбор нескольких ответов", "Текстовый ответ", "Код")

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, typeNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = adapter

        spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                val type = types[pos]
                if (type == "SingleChoice" || type == "MultipleChoice") {
                    etOptions.visibility = View.VISIBLE
                    etCorrectAnswer.hint = "Индексы правильных ответов: 1 (или 0,2)"
                } else {
                    etOptions.visibility = View.GONE
                    etCorrectAnswer.hint = if (type == "Code") "Ожидаемый вывод программы" else "Правильный ответ"
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnCreate.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val answer = etCorrectAnswer.text.toString().trim()
            val optionsText = etOptions.text.toString().trim()
            val type = types[spinnerType.selectedItemPosition]

            if (title.isEmpty() || description.isEmpty() || answer.isEmpty()) {
                tvResult.text = "Заполните все обязательные поля"
                tvResult.setTextColor(resources.getColor(R.color.error_red, null))
                tvResult.visibility = View.VISIBLE
                return@setOnClickListener
            }

            val optionsJson = if (type == "SingleChoice" || type == "MultipleChoice") {
                if (optionsText.isEmpty()) {
                    tvResult.text = "Введите варианты ответов"
                    tvResult.setTextColor(resources.getColor(R.color.error_red, null))
                    tvResult.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                val options = optionsText.split(",").map { it.trim() }
                com.google.gson.Gson().toJson(options)
            } else null

            pb.visibility = View.VISIBLE
            btnCreate.isEnabled = false

            lifecycleScope.launch {
                try {
                    val resp = RetrofitClient.api.createAssignment(
                        CreateAssignmentRequest(
                            title = title,
                            description = description,
                            type = type,
                            options = optionsJson,
                            correctAnswer = answer
                        )
                    )
                    if (resp.isSuccessful) {
                        tvResult.text = "✓ Задание создано успешно!"
                        tvResult.setTextColor(resources.getColor(R.color.success_green, null))
                        tvResult.visibility = View.VISIBLE
                        etTitle.text.clear()
                        etDescription.text.clear()
                        etCorrectAnswer.text.clear()
                        etOptions.text.clear()
                    } else {
                        tvResult.text = "Ошибка: ${resp.code()}"
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