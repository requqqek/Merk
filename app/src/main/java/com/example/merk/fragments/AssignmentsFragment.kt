package com.example.merk.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kotlinx.coroutines.launch
import android.content.Context

class AssignmentsFragment : Fragment() {

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        i.inflate(R.layout.fragment_assignments, c, false)

    override fun onViewCreated(view: View, s: Bundle?) {
        super.onViewCreated(view, s)
        val swipe = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        val rv = view.findViewById<RecyclerView>(R.id.recyclerView)
        val pb = view.findViewById<ProgressBar>(R.id.progressBar)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)

        rv.layoutManager = LinearLayoutManager(requireContext())

        fun load() {
            pb.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE

            val prefs = requireContext().getSharedPreferences("merk_session", Context.MODE_PRIVATE)
            val userId = prefs.getInt("userId", 0)

            android.util.Log.d("AssignmentsFragment", "Загрузка заданий для userId=$userId")

            lifecycleScope.launch {
                try {
                    val resp = RetrofitClient.api.getAssignmentsForStudent(userId)
                    android.util.Log.d("AssignmentsFragment", "Ответ: ${resp.code()}")

                    if (resp.isSuccessful) {
                        val list = resp.body() ?: emptyList()
                        android.util.Log.d("AssignmentsFragment", "Получено заданий: ${list.size}")

                        val sorted = list.sortedBy { it.isCompleted }

                        rv.adapter = AssignmentsAdapter(sorted) { assignment ->
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

                        tvEmpty.visibility = if (sorted.isEmpty()) View.VISIBLE else View.GONE
                    } else {
                        android.util.Log.e("AssignmentsFragment", "Ошибка: ${resp.errorBody()?.string()}")
                        tvEmpty.text = "Ошибка загрузки: ${resp.code()}"
                        tvEmpty.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AssignmentsFragment", "Исключение: ${e.message}", e)
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
}
