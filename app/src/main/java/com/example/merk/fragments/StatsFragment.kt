package com.example.merk.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.merk.R
import com.example.merk.SubmissionsAdapter
import com.example.merk.data.api.RetrofitClient
import com.example.merk.data.api.StudentItem
import com.example.merk.data.models.SubmissionItem
import kotlinx.coroutines.launch
import kotlin.collections.map

class StatsFragment : Fragment() {

    private var allStudents: List<StudentItem> = emptyList()
    private var allSubmissions: List<SubmissionItem> = emptyList()
    private var selectedStudentId: Int? = null

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        i.inflate(R.layout.fragment_stats, c, false)

    override fun onViewCreated(view: View, s: Bundle?) {
        super.onViewCreated(view, s)

        val rv = view.findViewById<RecyclerView>(R.id.recyclerView)
        val pb = view.findViewById<ProgressBar>(R.id.progressBar)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)
        val tvStudents = view.findViewById<TextView>(R.id.tvStudentsCount)
        val tvAttempts = view.findViewById<TextView>(R.id.tvAttemptsCount)
        val tvAvg = view.findViewById<TextView>(R.id.tvAvgGrade)
        val spinnerStudent = view.findViewById<Spinner>(R.id.spinnerStudent)

        rv.layoutManager = LinearLayoutManager(requireContext())

        pb.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                // Загружаем студентов и попытки параллельно
                val studentsResp = RetrofitClient.api.getTeacherStudents()
                val submissionsResp = RetrofitClient.api.getAllSubmissions()

                if (studentsResp.isSuccessful && submissionsResp.isSuccessful) {
                    allStudents = studentsResp.body() ?: emptyList()
                    allSubmissions = submissionsResp.body() ?: emptyList()

                    // Заполняем спиннер
                    val studentNames = listOf("Все студенты") + allStudents.map { it.login }
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        studentNames
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerStudent.adapter = adapter

                    spinnerStudent.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                            selectedStudentId = if (pos == 0) null else allStudents[pos - 1].userId
                            updateStats(tvStudents, tvAttempts, tvAvg, rv, tvEmpty)
                        }
                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }

                    // Первая загрузка — все студенты
                    updateStats(tvStudents, tvAttempts, tvAvg, rv, tvEmpty)
                } else {
                    tvEmpty.text = "Ошибка загрузки"; tvEmpty.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                tvEmpty.text = "Нет соединения"; tvEmpty.visibility = View.VISIBLE
            } finally {
                pb.visibility = View.GONE
            }
        }
    }

    private fun updateStats(
        tvStudents: TextView,
        tvAttempts: TextView,
        tvAvg: TextView,
        rv: RecyclerView,
        tvEmpty: TextView
    ) {
        // Фильтруем попытки по выбранному студенту
        val filtered = if (selectedStudentId == null) {
            allSubmissions
        } else {
            allSubmissions.filter { it.userId == selectedStudentId }
        }

        tvStudents.text = allStudents.size.toString()
        tvAttempts.text = filtered.size.toString()

        val grades = filtered.mapNotNull { it.grade }
        tvAvg.text = if (grades.isNotEmpty()) "%.1f".format(grades.average()) else "—"

        rv.adapter = SubmissionsAdapter(filtered)
        tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }
}