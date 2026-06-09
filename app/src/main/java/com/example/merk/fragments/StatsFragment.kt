package com.example.merk.fragments

import android.content.Context
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

class StatsFragment : Fragment() {

    private var allStudents: List<StudentItem> = emptyList()
    private var allSubmissions: List<SubmissionItem> = emptyList()

    // Список групп среди студентов преподавателя: id -> название
    private var groupList: List<Pair<Int, String>> = emptyList()

    private var selectedGroupId: Int? = null
    private var selectedStudentId: Int? = null

    private lateinit var spinnerStudent: Spinner

    private lateinit var tvStudents: TextView
    private lateinit var tvAttempts: TextView
    private lateinit var tvAvg: TextView
    private lateinit var rv: RecyclerView
    private lateinit var tvEmpty: TextView

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        i.inflate(R.layout.fragment_stats, c, false)

    override fun onViewCreated(view: View, s: Bundle?) {
        super.onViewCreated(view, s)

        rv = view.findViewById(R.id.recyclerView)
        val pb = view.findViewById<ProgressBar>(R.id.progressBar)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        tvStudents = view.findViewById(R.id.tvStudentsCount)
        tvAttempts = view.findViewById(R.id.tvAttemptsCount)
        tvAvg = view.findViewById(R.id.tvAvgGrade)
        val spinnerGroup = view.findViewById<Spinner>(R.id.spinnerGroup)
        spinnerStudent = view.findViewById(R.id.spinnerStudent)

        rv.layoutManager = LinearLayoutManager(requireContext())
        pb.visibility = View.VISIBLE

        val teacherId = requireContext()
            .getSharedPreferences("merk_session", Context.MODE_PRIVATE)
            .getInt("userId", 0)

        lifecycleScope.launch {
            try {
                val studentsResp = RetrofitClient.api.getTeacherStudents(teacherId)
                val submissionsResp = RetrofitClient.api.getAllSubmissions(teacherId = teacherId)

                if (studentsResp.isSuccessful && submissionsResp.isSuccessful) {
                    allStudents = studentsResp.body() ?: emptyList()
                    allSubmissions = submissionsResp.body() ?: emptyList()

                    // Группы, в которых есть студенты этого преподавателя
                    groupList = allStudents
                        .filter { it.groupId != null }
                        .map { it.groupId!! to (it.groupName ?: "Группа ${it.groupId}") }
                        .distinct()
                        .sortedBy { it.second }

                    setupGroupSpinner(spinnerGroup)
                    refreshStudentSpinner()
                    updateStats()
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

    private fun setupGroupSpinner(spinnerGroup: Spinner) {
        val names = listOf("Все группы") + groupList.map { it.second }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGroup.adapter = adapter

        spinnerGroup.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                selectedGroupId = if (pos == 0) null else groupList[pos - 1].first
                selectedStudentId = null      // сбрасываем студента при смене группы
                refreshStudentSpinner()
                updateStats()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // Студенты в текущей группе (или все)
    private fun studentsInScope(): List<StudentItem> =
        if (selectedGroupId == null) allStudents
        else allStudents.filter { it.groupId == selectedGroupId }

    private fun refreshStudentSpinner() {
        val scope = studentsInScope()
        val names = listOf("Все студенты") + scope.map { it.login }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStudent.adapter = adapter

        spinnerStudent.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                selectedStudentId = if (pos == 0) null else scope[pos - 1].userId
                updateStats()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateStats() {
        val scope = studentsInScope()
        val scopeIds = scope.map { it.userId }.toSet()

        val filtered = allSubmissions.filter { sub ->
            sub.userId in scopeIds &&
                    (selectedStudentId == null || sub.userId == selectedStudentId)
        }

        tvStudents.text = scope.size.toString()
        tvAttempts.text = filtered.size.toString()

        // Средний балл — средний процент по попыткам (разные maxGrade)
        val percents = filtered.mapNotNull { s ->
            val max = s.maxGrade ?: 0
            val grade = s.grade ?: 0
            if (max > 0) grade.toDouble() / max * 100 else null
        }
        tvAvg.text = if (percents.isNotEmpty()) "%.1f%%".format(percents.average()) else "—"

        rv.adapter = SubmissionsAdapter(filtered)
        tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }
}