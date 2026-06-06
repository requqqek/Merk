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
import com.example.merk.R
import com.example.merk.SubmissionsAdapter
import com.example.merk.data.api.RetrofitClient
import kotlinx.coroutines.launch

class StatsFragment : Fragment() {

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

        rv.layoutManager = LinearLayoutManager(requireContext())
        pb.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.getAllSubmissions()
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
}