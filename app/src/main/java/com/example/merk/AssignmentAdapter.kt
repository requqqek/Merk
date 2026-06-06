package com.example.merk

import com.example.merk.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.merk.data.models.AssignmentStatus

class AssignmentsAdapter(
    private val assignments: List<AssignmentStatus>,
    private val onClick: (AssignmentStatus) -> Unit
) : RecyclerView.Adapter<AssignmentsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.cardView)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvType: TextView = view.findViewById(R.id.tvType)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvGrade: TextView = view.findViewById(R.id.tvGrade)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_assignment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val assignment = assignments[position]

        holder.tvTitle.text = assignment.title
        holder.tvType.text = when (assignment.type) {
            "SingleChoice" -> "Выбор одного ответа"
            "MultipleChoice" -> "Выбор нескольких ответов"
            "TextInput" -> "Текстовый ответ"
            "Code" -> "Программный код"
            else -> assignment.type
        }

        if (assignment.isCompleted) {
            holder.tvStatus.text = "✓ Выполнено"
            holder.tvStatus.setTextColor(holder.itemView.context.getColor(R.color.success_green))
            holder.tvGrade.text = "Оценка: ${assignment.grade}/100"
            holder.tvGrade.visibility = View.VISIBLE
            holder.cardView.setCardBackgroundColor(holder.itemView.context.getColor(R.color.card_completed))
        } else {
            holder.tvStatus.text = "○ Не выполнено"
            holder.tvStatus.setTextColor(holder.itemView.context.getColor(R.color.gray_text))
            holder.tvGrade.visibility = View.GONE
            holder.cardView.setCardBackgroundColor(holder.itemView.context.getColor(R.color.white))
        }

        holder.cardView.setOnClickListener { onClick(assignment) }
    }

    override fun getItemCount() = assignments.size
}