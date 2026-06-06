package com.example.merk

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.merk.data.models.SubmissionItem

class SubmissionsAdapter(private val items: List<SubmissionItem>) :
    RecyclerView.Adapter<SubmissionsAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.tvAssignmentTitle)
        val answer: TextView = v.findViewById(R.id.tvStudentAnswer)
        val grade: TextView = v.findViewById(R.id.tvGrade)
        val comment: TextView = v.findViewById(R.id.tvComment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, vt: Int) = VH(
        LayoutInflater.from(parent.context).inflate(R.layout.item_submission, parent, false)
    )

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = items[pos]
        h.title.text = item.assignmentTitle ?: "Задание #${item.assignmentId}"
        h.answer.text = "Ответ: ${item.studentAnswer}"
        h.grade.text = item.grade?.toString() ?: "—"
        h.comment.text = item.comment ?: ""

        val grade = item.grade ?: 0
        val color = when {
            grade >= 80 -> Color.parseColor("#2E7D32")
            grade >= 50 -> Color.parseColor("#EF6C00")
            else -> Color.parseColor("#C62828")
        }
        h.grade.setTextColor(color)
    }

    override fun getItemCount() = items.size
}