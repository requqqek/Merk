package com.example.merk.fragments

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.merk.SubmissionsAdapter
import com.example.merk.data.api.RetrofitClient
import com.example.merk.data.models.GroupItem
import com.example.merk.data.models.StudentItem
import kotlinx.coroutines.launch

class StatsFragment : Fragment() {

    private var selectedStudentId: Int? = null
    private var selectedGroupId: Int? = null
    private var students = listOf<StudentItem>()
    private var groups = listOf<GroupItem>()

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
        val spinnerGroup = view.findViewById<Spinner>(R.id.spinnerGroup)
        val btnFilter = view.findViewById<Button>(R.id.btnFilter)
        val btnReset = view.findViewById<Button>(R.id.btnReset)

        rv.layoutManager = LinearLayoutManager(requireContext())

        // Загрузка студентов и групп
        lifecycleScope.launch {
            try {
                val studentsResp = RetrofitClient.api.getTeacherStudents()
                val groupsResp = RetrofitClient.api.getGroups()

                if (studentsResp.isSuccessful) {
                    students = studentsResp.body() ?: emptyList()
                }
                if (groupsResp.isSuccessful) {
                    groups = groupsResp.body() ?: emptyList()
                }

                // Spinner студентов
                val studentNames = listOf("Все студенты") + students.map { it.login }
                val studentAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.simple_spinner_item,
                    studentNames
                )
                studentAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                spinnerStudent.adapter = studentAdapter

                // Spinner групп
                val groupNames = listOf("Все группы") + groups.map { it.name }
                val groupAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.simple_spinner_item,
                    groupNames
                )
                groupAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                spinnerGroup.adapter = groupAdapter

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка загрузки фильтров", Toast.LENGTH_SHORT).show()
            }
        }

        fun loadSubmissions() {
            pb.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE

            lifecycleScope.launch {
                try {
                    val resp = RetrofitClient.api.getAllSubmissions(
                        studentId = selectedStudentId,
                        groupId = selectedGroupId
                    )
                    if (resp.isSuccessful) {
                        val list = resp.body() ?: emptyList()
                        tvStudents.text = list.map { it.userId }.distinct().size.toString()
                        tvAttempts.text = list.size.toString()
                        val grades = list.mapNotNull { it.grade }
                        tvAvg.text = if (grades.isNotEmpty()) "%.1f".format(grades.average()) else "—"
                        rv.adapter = SubmissionsAdapter(list)
                        tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
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

        spinnerStudent.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                selectedStudentId = if (pos == 0) null else students[pos - 1].userId
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerGroup.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                selectedGroupId = if (pos == 0) null else groups[pos - 1].id
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnFilter.setOnClickListener { loadSubmissions() }
        btnReset.setOnClickListener {
            spinnerStudent.setSelection(0)
            spinnerGroup.setSelection(0)
            selectedStudentId = null
            selectedGroupId = null
            loadSubmissions()
        }

        loadSubmissions()
    }
}