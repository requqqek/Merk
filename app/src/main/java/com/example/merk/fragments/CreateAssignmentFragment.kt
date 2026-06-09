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
import com.example.merk.data.api.GroupItem
import com.example.merk.data.api.RetrofitClient
import com.example.merk.data.models.Assignment
import com.example.merk.data.models.CreateAssignmentRequest
import kotlinx.coroutines.launch

class CreateAssignmentFragment : Fragment() {

    private var isEditing = false
    private var editingAssignmentId: Int? = null
    private var groups: List<GroupItem> = emptyList()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        i.inflate(R.layout.fragment_create_assignment, c, false)

    override fun onViewCreated(view: View, s: Bundle?) {
        super.onViewCreated(view, s)

        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val etCorrectAnswer = view.findViewById<EditText>(R.id.etCorrectAnswer)
        val etOptions = view.findViewById<EditText>(R.id.etOptions)
        val spinnerType = view.findViewById<Spinner>(R.id.spinnerType)
        val spinnerGroup = view.findViewById<Spinner>(R.id.spinnerGroup)
        val btnCreate = view.findViewById<Button>(R.id.btnCreate)
        val pb = view.findViewById<ProgressBar>(R.id.progressBar)
        val tvResult = view.findViewById<TextView>(R.id.tvResult)

        val rvAssignments = view.findViewById<RecyclerView>(R.id.rvAssignments)
        rvAssignments.layoutManager = LinearLayoutManager(requireContext())

        val types = arrayOf("SingleChoice", "MultipleChoice", "TextInput", "Code")
        val typeNames = arrayOf("Выбор одного ответа", "Выбор нескольких ответов", "Текстовый ответ", "Код")
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, typeNames)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = typeAdapter

        spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                val type = types[pos]
                if (type == "SingleChoice" || type == "MultipleChoice") {
                    etOptions.visibility = View.VISIBLE
                    etCorrectAnswer.hint = "Индексы (с 0): 1 или 0,2"
                } else {
                    etOptions.visibility = View.GONE
                    etCorrectAnswer.hint = if (type == "Code") "Ожидаемый вывод программы" else "Правильный ответ"
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        loadGroups(spinnerGroup)
        loadTeacherAssignments(rvAssignments, pb, tvResult)

        btnCreate.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val answer = etCorrectAnswer.text.toString().trim()
            val optionsText = etOptions.text.toString().trim()
            val type = types[spinnerType.selectedItemPosition]

            if (title.isEmpty() || description.isEmpty() || answer.isEmpty()) {
                showResult(tvResult, "Заполните все обязательные поля", false)
                return@setOnClickListener
            }

            val optionsJson = if (type == "SingleChoice" || type == "MultipleChoice") {
                if (optionsText.isEmpty()) {
                    showResult(tvResult, "Введите варианты ответов", false)
                    return@setOnClickListener
                }
                val options = optionsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                com.google.gson.Gson().toJson(options)
            } else null

            val groupId = selectedGroupId(spinnerGroup)
            val teacherId = requireContext()
                .getSharedPreferences("merk_session", android.content.Context.MODE_PRIVATE)
                .getInt("userId", 0)

            pb.visibility = View.VISIBLE
            btnCreate.isEnabled = false

            lifecycleScope.launch {
                try {
                    val request = CreateAssignmentRequest(
                        title = title,
                        description = description,
                        type = type,
                        options = optionsJson,
                        correctAnswer = answer,
                        teacherId = teacherId,
                        groupId = groupId
                    )
                    val resp = if (isEditing && editingAssignmentId != null)
                        RetrofitClient.api.updateAssignment(editingAssignmentId!!, request)
                    else
                        RetrofitClient.api.createAssignment(request)

                    if (resp.isSuccessful) {
                        showResult(tvResult, if (isEditing) "✓ Задание обновлено!" else "✓ Задание создано!", true)
                        clearForm(etTitle, etDescription, etCorrectAnswer, etOptions)
                        isEditing = false
                        editingAssignmentId = null
                        btnCreate.text = "Создать задание"
                        loadTeacherAssignments(rvAssignments, pb, tvResult)
                    } else {
                        showResult(tvResult, "Ошибка: ${resp.code()}", false)
                    }
                } catch (e: Exception) {
                    showResult(tvResult, "Ошибка: ${e.message}", false)
                } finally {
                    pb.visibility = View.GONE
                    btnCreate.isEnabled = true
                }
            }
        }
    }

    private fun showResult(tv: TextView, msg: String, ok: Boolean) {
        tv.text = msg
        tv.setTextColor(resources.getColor(if (ok) R.color.success_green else R.color.error_red, null))
        tv.visibility = View.VISIBLE
    }

    private fun loadGroups(spinnerGroup: Spinner) {
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.getGroups()
                if (resp.isSuccessful) {
                    groups = resp.body() ?: emptyList()
                    val names = listOf("Все группы") + groups.map { it.name }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerGroup.adapter = adapter
                }
            } catch (_: Exception) {}
        }
    }

    private fun selectedGroupId(spinnerGroup: Spinner): Int? {
        val pos = spinnerGroup.selectedItemPosition
        return if (pos <= 0 || pos - 1 >= groups.size) null else groups[pos - 1].id
    }

    private fun loadTeacherAssignments(rv: RecyclerView, pb: ProgressBar, tvResult: TextView) {
        val teacherId = requireContext()
            .getSharedPreferences("merk_session", android.content.Context.MODE_PRIVATE)
            .getInt("userId", 0)
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.getTeacherAssignments(teacherId)
                if (resp.isSuccessful) {
                    val list = resp.body() ?: emptyList()
                    rv.adapter = TeacherAssignmentsAdapter(
                        list,
                        onEdit = { editAssignment(it) },
                        onDelete = { deleteAssignment(it, pb, tvResult, rv) }
                    )
                }
            } catch (_: Exception) {}
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
        val spinnerGroup = view?.findViewById<Spinner>(R.id.spinnerGroup)
        val btnCreate = view?.findViewById<Button>(R.id.btnCreate)

        etTitle?.setText(assignment.title)
        etDescription?.setText(assignment.description)
        etCorrectAnswer?.setText(assignment.correctAnswer)

        val types = arrayOf("SingleChoice", "MultipleChoice", "TextInput", "Code")
        val typeIndex = types.indexOf(assignment.type)
        if (typeIndex >= 0) spinnerType?.setSelection(typeIndex)

        if (!assignment.options.isNullOrEmpty()) {
            try {
                val options = com.google.gson.Gson().fromJson(assignment.options, Array<String>::class.java)
                etOptions?.setText(options.joinToString(", "))
            } catch (_: Exception) {}
        }

        val gPos = if (assignment.groupId == null) 0
        else groups.indexOfFirst { it.id == assignment.groupId }.let { if (it < 0) 0 else it + 1 }
        spinnerGroup?.setSelection(gPos)

        btnCreate?.text = "Сохранить изменения"
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
                            showResult(tvResult, "✓ Задание удалено", true)
                            loadTeacherAssignments(rv, pb, tvResult)
                        } else showResult(tvResult, "Ошибка удаления: ${resp.code()}", false)
                    } catch (e: Exception) {
                        showResult(tvResult, "Ошибка: ${e.message}", false)
                    } finally {
                        pb.visibility = View.GONE
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun clearForm(vararg fields: EditText) = fields.forEach { it.text.clear() }
}