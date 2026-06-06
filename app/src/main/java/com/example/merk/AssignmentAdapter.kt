package com.example.merk

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.merk.data.models.Assignment

class AssignmentAdapter(private val items: List<Assignment>) :
    RecyclerView.Adapter<AssignmentAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.tvTitle)
        val desc: TextView = v.findViewById(R.id.tvDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, vt: Int) = VH(
        LayoutInflater.from(parent.context).inflate(R.layout.item_assignment, parent, false)
    )

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = items[pos]
        h.title.text = item.title
        h.desc.text = item.description
        h.itemView.setOnClickListener {
            it.context.startActivity(
                Intent(it.context, SubmissionActivity::class.java).apply {
                    putExtra(SubmissionActivity.EXTRA_ID, item.id)
                    putExtra(SubmissionActivity.EXTRA_TITLE, item.title)
                    putExtra(SubmissionActivity.EXTRA_DESC, item.description)
                }
            )
        }
    }

    override fun getItemCount() = items.size
}
