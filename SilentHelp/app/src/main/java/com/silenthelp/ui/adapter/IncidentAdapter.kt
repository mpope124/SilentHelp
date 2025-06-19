package com.silenthelp.ui.adapter
//Added By Michael
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.silenthelp.R
import com.silenthelp.models.Incident

class IncidentAdapter(private var incidentList: List<Incident>) :
    RecyclerView.Adapter<IncidentAdapter.IncidentViewHolder>() {

        inner class IncidentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textTitle: TextView = itemView.findViewById(R.id.textTitle)
            val textDescription: TextView = itemView.findViewById(R.id.textDescription)
            val textDate: TextView = itemView.findViewById(R.id.textDate)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncidentViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_incident, parent, false)
            return IncidentViewHolder(view)
        }

        override fun onBindViewHolder(holder: IncidentViewHolder, position: Int) {
            val incident = incidentList[position]
            holder.textTitle.text = incident.title
            holder.textDescription.text = incident.description
            holder.textDate.text = incident.date.take(10) // Just the YYYY-MM-DD part
        }

        override fun getItemCount() = incidentList.size

        fun updateData(newList: List<Incident>) {
            incidentList = newList
            notifyDataSetChanged()
        }
    }