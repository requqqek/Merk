package com.example.merk.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.merk.AssignmentsAdapter
import com.example.merk.R
import com.example.merk.data.api.RetrofitClient
import com.example.merk.data.models.AssignmentStatus
import kotlinx.coroutines.launch

class AssignmentsFragment : Fragment() {

    private var allAssignments: List<AssignmentStatus> = emptyList()
    private var currentFilter: String = "all" // all, pending, done

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        i.inflate(R.layout.fragment_assignments, c, false)

    override fun onViewCreated(view: View, s: Bundle?) {
        super.onViewCreated(view, s)

        val swipe = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        val rv = view.findViewById<RecyclerView>(R.id.recyclerView)
        val pb = view.findViewById<ProgressBar>(R.id.progressBar)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)

        val btnAll = view.findViewById<Button>(R.id.btnFilterAll)
        val btnPending = view.findViewById<Button>(R.id.btnFilterPending)
        val btnDone = view.findViewById<Button>(R.id.btnFilterDone)

        rv.layoutManager = LinearLayoutManager(requireContext())

        // Обработчики кнопок фильтра
        fun setFilter(filter: String) {
            currentFilter = filter
            // Обновляем цвета кнопок
            btnAll.backgroundTintList = android.content.res.ColorStateList.valueOf(
                if (filter == "all") 0xFFB1E792.toInt() else 0xFFE0E0E0.toInt()
            )
            btnPending.backgroundTintList = android.content.res.ColorStateList.valueOf(
                if (filter == "pending") 0xFFB1E792.toInt() else 0xFFE0E0E0.toInt()
            )
            btnDone.backgroundTintList = android.content.res.ColorStateList.valueOf(
                if (filter == "done") 0xFFB1E792.toInt() else 0xFFE0E0E0.toInt()
            )
            applyFilter(rv, tvEmpty)
        }

        btnAll.setOnClickListener { setFilter("all") }
        btnPending.setOnClickListener { setFilter("pending") }
        btnDone.setOnClickListener { setFilter("done") }

        fun load() {
            pb.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE

            val prefs = requireContext().getSharedPreferences("merk_session", Context.MODE_PRIVATE)
            val userId = prefs.getInt("userId", 0)

            lifecycleScope.launch {
                try {
                    val resp = RetrofitClient.api.getAssignmentsForStudent(userId)
                    if (resp.isSuccessful) {
                        allAssignments = resp.body() ?: emptyList()
                        applyFilter(rv, tvEmpty)
                    } else {
                        tvEmpty.text = "Ошибка загрузки: ${resp.code()}"
                        tvEmpty.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    tvEmpty.text = "Нет соединения: ${e.message}"
                    tvEmpty.visibility = View.VISIBLE
                } finally {
                    pb.visibility = View.GONE
                    swipe.isRefreshing = false
                }
            }
        }

        swipe.setOnRefreshListener { load() }
        load()
    }

    private fun applyFilter(rv: RecyclerView, tvEmpty: TextView) {
        val filtered = when (currentFilter) {
            "pending" -> allAssignments.filter { !it.isCompleted }
            "done" -> allAssignments.filter { it.isCompleted }
            else -> allAssignments.sortedBy { it.isCompleted }
        }

        rv.adapter = AssignmentsAdapter(filtered) { assignment ->
            val fragment = AssignmentDetailFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("assignment", assignment)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }

        tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }
}