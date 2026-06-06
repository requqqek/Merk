package com.example.merk

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.merk.data.api.RetrofitClient
import com.example.merk.data.models.SubmissionRequest
import kotlinx.coroutines.launch

class SubmissionActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_ID = "assignment_id"
        const val EXTRA_TITLE = "assignment_title"
        const val EXTRA_DESC = "assignment_description"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submission)

        val assignmentId = intent.getIntExtra(EXTRA_ID, -1)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Задание"
        val desc = intent.getStringExtra(EXTRA_DESC) ?: ""

        findViewById<TextView>(R.id.tvToolbarTitle).text = title
        findViewById<TextView>(R.id.tvAssignmentTitle).text = title
        findViewById<TextView>(R.id.tvAssignmentDescription).text = desc
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        val etAnswer = findViewById<EditText>(R.id.etAnswer)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)
        val progress = findViewById<ProgressBar>(R.id.progressBar)
        val cardResult = findViewById<CardView>(R.id.cardResult)
        val tvGrade = findViewById<TextView>(R.id.tvGrade)
        val tvComment = findViewById<TextView>(R.id.tvComment)

        btnSubmit.setOnClickListener {
            val answer = etAnswer.text.toString().trim()
            if (answer.isEmpty()) {
                Toast.makeText(this, "Введите ответ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val userId = getSharedPreferences("merk_session", MODE_PRIVATE)
                .getInt("userId", -1)
            if (userId == -1) { Toast.makeText(this, "Ошибка сессии", Toast.LENGTH_SHORT).show(); return@setOnClickListener }

            progress.visibility = View.VISIBLE
            btnSubmit.isEnabled = false

            lifecycleScope.launch {
                try {
                    val resp = RetrofitClient.api.submitAnswer(
                        SubmissionRequest(userId, assignmentId, answer)
                    )
                    if (resp.isSuccessful) {
                        val r = resp.body()!!
                        cardResult.visibility = View.VISIBLE
                        tvGrade.text = "Оценка: ${r.grade}/100"
                        tvComment.text = r.comment
                        etAnswer.isEnabled = false
                    } else {
                        Toast.makeText(this@SubmissionActivity,
                            resp.errorBody()?.string() ?: "Ошибка", Toast.LENGTH_LONG).show()
                        btnSubmit.isEnabled = true
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@SubmissionActivity,
                        "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                    btnSubmit.isEnabled = true
                } finally {
                    progress.visibility = View.GONE
                }
            }
        }
    }
}
