package com.example.merk.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.merk.R
import com.example.merk.data.api.RetrofitClient
import com.example.merk.data.models.AssignmentStatus
import com.example.merk.data.models.SubmissionRequest
import com.google.gson.Gson
import kotlinx.coroutines.launch

class AssignmentDetailFragment : Fragment() {

    private var assignment: AssignmentStatus? = null
    private var selectedIndices = mutableListOf<Int>()
    private var etAnswer: EditText? = null
    private var rootLayout: LinearLayout? = null

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        i.inflate(R.layout.fragment_assignment_detail, c, false)

    override fun onViewCreated(view: View, s: Bundle?) {
        super.onViewCreated(view, s)
        assignment = arguments?.getSerializable("assignment") as? AssignmentStatus

        if (assignment == null) {
            Toast.makeText(requireContext(), "Ошибка: задание не загружено", Toast.LENGTH_LONG).show()
            parentFragmentManager.popBackStack()
            return
        }

        val current = assignment!!

        // ОТЛАДКА
        Log.d("AssignmentDetail", "=== ЗАДАНИЕ ===")
        Log.d("AssignmentDetail", "Title: ${current.title}")
        Log.d("AssignmentDetail", "Type: ${current.type}")
        Log.d("AssignmentDetail", "Options: ${current.options}")
        Log.d("AssignmentDetail", "IsCompleted: ${current.isCompleted}")
        Log.d("AssignmentDetail", "Grade: ${current.grade}")

        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvDescription = view.findViewById<TextView>(R.id.tvDescription)
        val container = view.findViewById<LinearLayout>(R.id.optionsContainer)
        etAnswer = view.findViewById<EditText>(R.id.etAnswer)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmit)
        val tvResult = view.findViewById<TextView>(R.id.tvResult)

        tvTitle.text = current.title
        tvDescription.text = current.description

        // ========== ПРОВЕРКА: ЕСЛИ ЗАДАНИЕ УЖЕ ВЫПОЛНЕНО ==========
        if (current.isCompleted) {
            Log.d("AssignmentDetail", "Задание уже выполнено, блокируем ввод")

            val grade = current.grade ?: 0

            tvResult.text = "Результат: $grade/100\n${current.comment ?: ""}"
            tvResult.setTextColor(
                when {
                    grade == 100 -> resources.getColor(R.color.success_green, null)
                    else -> resources.getColor(R.color.error_red, null)
                }
            )
            tvResult.visibility = View.VISIBLE

            container.visibility = View.GONE
            etAnswer?.visibility = View.GONE
            btnSubmit.visibility = View.GONE

            val tvLocked = TextView(requireContext()).apply {
                text = "⛔ Задание уже выполнено. Повторное прохождение невозможно."
                textSize = 14f
                setTextColor(resources.getColor(R.color.gray_text, null))
                setPadding(0, 24, 0, 0)
            }
            rootLayout?.addView(tvLocked)

            return
        }
        // =============================================================

        // Если задание НЕ выполнено — показываем форму
        Log.d("AssignmentDetail", "Задание не выполнено, показываем форму")

        when (current.type) {
            "SingleChoice", "MultipleChoice" -> {
                container.visibility = View.VISIBLE
                etAnswer?.visibility = View.GONE

                val optionsJson = current.options ?: "[]"
                Log.d("AssignmentDetail", "Options JSON: $optionsJson")

                val options = try {
                    Gson().fromJson(optionsJson, Array<String>::class.java).toList()
                } catch (e: Exception) {
                    Log.e("AssignmentDetail", "Ошибка парсинга options: ${e.message}")
                    emptyList()
                }

                Log.d("AssignmentDetail", "Распаршено вариантов: ${options.size}")

                container.removeAllViews()
                selectedIndices.clear()

                options.forEachIndexed { index, option ->
                    val checkBox = CheckBox(requireContext()).apply {
                        text = option
                        tag = index
                        textSize = 16f
                        setPadding(8, 16, 8, 16)
                        setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked) {
                                if (current.type == "SingleChoice") {
                                    for (j in 0 until container.childCount) {
                                        val child = container.getChildAt(j)
                                        if (child is CheckBox && child.tag != index) {
                                            child.isChecked = false
                                        }
                                    }
                                    selectedIndices.clear()
                                    selectedIndices.add(index)
                                } else {
                                    selectedIndices.add(index)
                                }
                            } else {
                                selectedIndices.remove(index)
                            }
                        }
                    }
                    container.addView(checkBox)
                }
            }
            "TextInput", "Code" -> {
                container.visibility = View.GONE
                etAnswer?.visibility = View.VISIBLE
                etAnswer?.isEnabled = true  // ← ВАЖНО: разрешаем ввод
                etAnswer?.hint = if (current.type == "Code") "Введите код или вывод программы..." else "Введите ответ..."
            }
        }

        btnSubmit.setOnClickListener {
            val answer = when (current.type) {
                "SingleChoice", "MultipleChoice" -> selectedIndices.joinToString(",")
                else -> etAnswer?.text.toString() ?: ""
            }

            if (answer.isEmpty()) {
                Toast.makeText(requireContext(), "Введите ответ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            submitAnswer(answer, tvResult, btnSubmit, container)
        }
    }

    private fun submitAnswer(
        answer: String,
        tvResult: TextView,
        btnSubmit: Button,
        container: LinearLayout
    ) {
        val prefs = requireContext().getSharedPreferences("merk_session", Context.MODE_PRIVATE)
        val userId = prefs.getInt("userId", 0)
        val current = assignment ?: return

        btnSubmit.isEnabled = false
        tvResult.visibility = View.GONE


        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.submitAnswer(
                    SubmissionRequest(
                        userId = userId,
                        assignmentId = current.assignmentId,
                        studentAnswer = answer
                    )
                )
                if (response.isSuccessful) {
                    val result = response.body()
                    val grade = result?.grade ?: 0
                    val maxGrade = result?.maxGrade ?: 1

                    tvResult.text = "Оценка: $grade/$maxGrade\n${result?.comment}"
                    tvResult.setTextColor(
                        when {
                            grade == maxGrade -> resources.getColor(R.color.success_green, null)
                            grade > 0 -> resources.getColor(android.R.color.holo_orange_dark, null)
                            else -> resources.getColor(R.color.error_red, null)
                        }
                    )
                    tvResult.visibility = View.VISIBLE

                    // Блокируем после отправки
                    btnSubmit.visibility = View.GONE
                    container.visibility = View.GONE
                    etAnswer?.visibility = View.GONE
                    etAnswer?.isEnabled = false

                    val tvLocked = TextView(requireContext()).apply {
                        text = "⛔ Задание выполнено. Повторная отправка невозможна."
                        textSize = 14f
                        setTextColor(resources.getColor(R.color.gray_text, null))
                        setPadding(0, 24, 0, 0)
                    }
                    rootLayout?.addView(tvLocked)
                } else {
                    tvResult.text = "Ошибка: ${response.code()}"
                    tvResult.setTextColor(resources.getColor(R.color.error_red, null))
                    tvResult.visibility = View.VISIBLE
                    btnSubmit.isEnabled = true
                }
            } catch (e: Exception) {
                tvResult.text = "Ошибка соединения: ${e.message}"
                tvResult.setTextColor(resources.getColor(R.color.error_red, null))
                tvResult.visibility = View.VISIBLE
                btnSubmit.isEnabled = true
            }
        }
    }
}