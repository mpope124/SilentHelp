// Created by Kelley Rosa 06-07-25
package com.silenthelp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.silenthelp.R
import com.silenthelp.model.Keyword

class KeywordAdapter(
    private var keywordList: MutableList<Keyword>,
    private val onEdit: (Int, Keyword) -> Unit,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<KeywordAdapter.KeywordViewHolder>() {

    private var editingIndex: Int? = null

    class KeywordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val keywordText: TextView = itemView.findViewById(R.id.text_keyword)
        val threatLevelText: TextView = itemView.findViewById(R.id.text_threat_level)
        val editButton: Button = itemView.findViewById(R.id.btn_edit_keyword)
        val deleteButton: Button = itemView.findViewById(R.id.btn_delete_keyword)
        val buttonLayout: LinearLayout = itemView.findViewById(R.id.edit_delete_buttons)
    }

    // Creates a new row for keyword list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeywordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_keyword, parent, false)
        return KeywordViewHolder(view)
    }

    // Binds keyword to a threat level
    override fun onBindViewHolder(holder: KeywordViewHolder, position: Int) {
        val keyword = keywordList[position]
        holder.keywordText.text = keyword.word
        holder.threatLevelText.text = "Level ${keyword.level + 1}"

        // Show or hide Edit/Delete buttons
        holder.buttonLayout.visibility =
            if (position == editingIndex) View.VISIBLE else View.GONE

        // Toggle visibility on click
        holder.itemView.setOnClickListener {
            editingIndex = if (editingIndex == position) null else position
            notifyDataSetChanged()
        }

        // Handle edit
        holder.editButton.setOnClickListener {
            onEdit(position, keyword)
        }

        // Handle delete
        holder.deleteButton.setOnClickListener {
            onDelete(position)
        }
    }

    override fun getItemCount(): Int = keywordList.size

    fun updateData(newList: List<Keyword>) {
        keywordList = newList.toMutableList()
        notifyDataSetChanged()
    }

    fun setEditingIndex(index: Int?) {
        editingIndex = index
        notifyDataSetChanged()
    }

    fun getEditingIndex(): Int? = editingIndex
}
