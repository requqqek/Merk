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
import com.example.merk.AssignmentAdapter
import com.example.merk.R
import com.example.merk.data.api.RetrofitClient
import kotlinx.coroutines.launch

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
            lifecycleScope.launch {
                try {
                    val resp = RetrofitClient.api.getAssignments()
                    if (resp.isSuccessful) {
                        val list = resp.body() ?: emptyList()
                        rv.adapter = AssignmentAdapter(list)
                        tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                    } else {
                        tvEmpty.text = "Ошибка загрузки"
                        tvEmpty.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    tvEmpty.text = "Нет соединения с сервером"
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
