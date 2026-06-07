package com.example.merk.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.merk.R
import com.example.merk.TeacherAssignmentsAdapter
import com.example.merk.data.api.RetrofitClient
import com.example.merk.data.models.Assignment
import com.example.merk.data.models.CreateAssignmentRequest
import kotlinx.coroutines.launch

class CreateAssignmentFragment : Fragment() {

    private var isEditing = false
    private var editingAssignmentId: Int? = null

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

        // Список заданий учителя
        val rvAssignments = view.findViewById<RecyclerView>(R.id.rvAssignments)
        rvAssignments.layoutManager = LinearLayoutManager(requireContext())

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

        // Загружаем список заданий
        loadTeacherAssignments(rvAssignments, pb, tvResult)

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
                val json = com.google.gson.Gson().toJson(options)
                android.util.Log.d("CreateAssignment", "Отправка options JSON: $json")
                json
            } else null

            pb.visibility = View.VISIBLE
            btnCreate.isEnabled = false

            lifecycleScope.launch {
                try {
                    val request = CreateAssignmentRequest(title, description, type, optionsJson, answer)

                    val resp = if (isEditing && editingAssignmentId != null) {
                        RetrofitClient.api.updateAssignment(editingAssignmentId!!, request)
                    } else {
                        RetrofitClient.api.createAssignment(request)
                    }

                    if (resp.isSuccessful) {
                        tvResult.text = if (isEditing) "✓ Задание обновлено!" else "✓ Задание создано успешно!"
                        tvResult.setTextColor(resources.getColor(R.color.success_green, null))
                        tvResult.visibility = View.VISIBLE
                        clearForm(etTitle, etDescription, etCorrectAnswer, etOptions)
                        isEditing = false
                        editingAssignmentId = null
                        btnCreate.text = "Создать задание"
                        loadTeacherAssignments(rvAssignments, pb, tvResult)
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

    private fun loadTeacherAssignments(rv: RecyclerView, pb: ProgressBar, tvResult: TextView) {
        val prefs = requireContext().getSharedPreferences("merk_session", android.content.Context.MODE_PRIVATE)
        val teacherId = prefs.getInt("userId", 0)

        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.getTeacherAssignments(teacherId)
                if (resp.isSuccessful) {
                    val list = resp.body() ?: emptyList()
                    rv.adapter = TeacherAssignmentsAdapter(
                        list,
                        onEdit = { assignment -> editAssignment(assignment) },
                        onDelete = { assignment -> deleteAssignment(assignment, pb, tvResult, rv) }
                    )
                }
            } catch (e: Exception) {
                // Тихо игнорируем
            }
        }
    }

    private fun editAssignment(assignment: Assignment) {
        isEditing = true
        editingAssignmentId = assignment.id

        val etTitle = view?.findViewById<EditText>(R.id.etTitle)
        val etDescription = view?.findViewById<EditText>(R.id.etDescription)
        val etCorrectAnswer = view?.findViewById<EditText>(R.id.etCorrectAnswer)
        val etOptions = view?.findViewById<EditText>(R.id.etOptions)
        val spinnerType = view?.findViewById<Spinner>(R.id.spinnerType)
        val btnCreate = view?.findViewById<Button>(R.id.btnCreate)

        etTitle?.setText(assignment.title)
        etDescription?.setText(assignment.description)
        etCorrectAnswer?.setText(assignment.correctAnswer)

        val types = arrayOf("SingleChoice", "MultipleChoice", "TextInput", "Code")
        val typeIndex = types.indexOf(assignment.type)
        if (typeIndex >= 0) spinnerType?.setSelection(typeIndex)

        if (assignment.options != null) {
            try {
                val options = com.google.gson.Gson().fromJson(assignment.options, Array<String>::class.java)
                etOptions?.setText(options.joinToString(", "))
            } catch (e: Exception) {}
        }

        btnCreate?.text = "Сохранить изменения"

        // Прокручиваем к форме
        view?.findViewById<ScrollView>(R.id.scrollView)?.smoothScrollTo(0, 0)
    }

    private fun deleteAssignment(assignment: Assignment, pb: ProgressBar, tvResult: TextView, rv: RecyclerView) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Удалить задание?")
            .setMessage("Задание \"${assignment.title}\" будет удалено безвозвратно.")
            .setPositiveButton("Удалить") { _, _ ->
                lifecycleScope.launch {
                    try {
                        pb.visibility = View.VISIBLE
                        val resp = RetrofitClient.api.deleteAssignment(assignment.id)
                        if (resp.isSuccessful) {
                            tvResult.text = "✓ Задание удалено"
                            tvResult.setTextColor(resources.getColor(R.color.success_green, null))
                            tvResult.visibility = View.VISIBLE
                            loadTeacherAssignments(rv, pb, tvResult)
                        } else {
                            tvResult.text = "Ошибка удаления: ${resp.code()}"
                            tvResult.setTextColor(resources.getColor(R.color.error_red, null))
                            tvResult.visibility = View.VISIBLE
                        }
                    } catch (e: Exception) {
                        tvResult.text = "Ошибка: ${e.message}"
                        tvResult.setTextColor(resources.getColor(R.color.error_red, null))
                        tvResult.visibility = View.VISIBLE
                    } finally {
                        pb.visibility = View.GONE
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun clearForm(vararg fields: EditText) {
        fields.forEach { it.text.clear() }
    }
}