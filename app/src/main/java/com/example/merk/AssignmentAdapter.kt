package com.example.merk

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
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
            val grade = assignment.grade ?: 0
            val max = assignment.maxGrade ?: 1

            holder.tvStatus.text = "✓ Выполнено"
            holder.tvStatus.setTextColor(holder.itemView.context.getColor(R.color.success_green))

            holder.tvGrade.text = "$grade/$max"
            holder.tvGrade.visibility = View.VISIBLE

            // Цвет по проценту
            val percentage = if (max > 0) (grade.toDouble() / max) * 100 else 0.0
            holder.tvGrade.setTextColor(
                when {
                    percentage == 100.0 -> holder.itemView.context.getColor(R.color.success_green)
                    percentage > 0 -> holder.itemView.context.getColor(android.R.color.holo_orange_dark)
                    else -> holder.itemView.context.getColor(R.color.error_red)
                }
            )

            holder.cardView.setCardBackgroundColor(holder.itemView.context.getColor(R.color.card_completed))

            // При клике на выполненное задание — показываем результат, но не даём пройти снова
            holder.cardView.setOnClickListener {
                Toast.makeText(
                    holder.itemView.context,
                    "Задание уже выполнено. Результат: $grade/$max",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            holder.tvStatus.text = "○ Не выполнено"
            holder.tvStatus.setTextColor(holder.itemView.context.getColor(R.color.error_red))
            holder.tvGrade.visibility = View.GONE
            holder.cardView.setCardBackgroundColor(holder.itemView.context.getColor(R.color.white))

            // Клик только для невыполненных заданий
            holder.cardView.setOnClickListener { onClick(assignment) }
        }
    }

    override fun getItemCount() = assignments.size
}