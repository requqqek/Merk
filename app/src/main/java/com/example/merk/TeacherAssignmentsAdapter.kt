package com.example.merk

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.merk.data.models.Assignment

class TeacherAssignmentsAdapter(
    private val assignments: List<Assignment>,
    private val onEdit: (Assignment) -> Unit,
    private val onDelete: (Assignment) -> Unit
) : RecyclerView.Adapter<TeacherAssignmentsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.cardView)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvType: TextView = view.findViewById(R.id.tvType)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_teacher_assignment, parent, false)
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

        holder.btnEdit.setOnClickListener { onEdit(assignment) }
        holder.btnDelete.setOnClickListener { onDelete(assignment) }
    }

    override fun getItemCount() = assignments.size
}