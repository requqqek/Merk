package com.example.merk.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.merk.LoginActivity
import com.example.merk.R

class ProfileFragment : Fragment() {

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        i.inflate(R.layout.fragment_profile, c, false)

    override fun onViewCreated(view: View, s: Bundle?) {
        super.onViewCreated(view, s)
        val prefs = requireContext().getSharedPreferences("merk_session", Context.MODE_PRIVATE)
        view.findViewById<TextView>(R.id.tvLogin).text = prefs.getString("login", "—")
        view.findViewById<TextView>(R.id.tvRole).text =
            if (prefs.getString("role", "") == "Teacher") "Преподаватель" else "Студент"
        view.findViewById<TextView>(R.id.tvUserId).text = "ID: ${prefs.getInt("userId", 0)}"
        view.findViewById<Button>(R.id.btnLogout).setOnClickListener {
            prefs.edit().clear().apply()
            startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
    }
}
