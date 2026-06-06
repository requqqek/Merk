package com.example.merk

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.merk.fragments.*
import com.google.android.material.bottomnavigation.BottomNavigationView

class TeacherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher)

        val prefs = getSharedPreferences("merk_session", MODE_PRIVATE)
        findViewById<TextView>(R.id.tvUsername).text = prefs.getString("login", "")

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        if (savedInstanceState == null) loadFragment(CreateAssignmentFragment())

        bottomNav.setOnItemSelectedListener {
            loadFragment(when (it.itemId) {
                R.id.nav_stats -> StatsFragment()
                R.id.nav_create -> CreateAssignmentFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> CreateAssignmentFragment()
            })
            true
        }
    }

    private fun loadFragment(f: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, f).commit()
    }
}