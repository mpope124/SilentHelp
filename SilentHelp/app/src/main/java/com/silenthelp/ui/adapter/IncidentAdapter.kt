package com.silenthelp.ui.adapter
//Added By Michael
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.silenthelp.R
import com.silenthelp.models.Incident
import java.io.File

class IncidentAdapter(private var incidentList: List<Incident>) :
    RecyclerView.Adapter<IncidentAdapter.IncidentViewHolder>() {

        inner class IncidentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textTitle: TextView = itemView.findViewById(R.id.textTitle)
            val textDate: TextView = itemView.findViewById(R.id.textDate)
            val textContacts: TextView    = itemView.findViewById(R.id.textContacts)
            val textLocation: TextView    = itemView.findViewById(R.id.textLocation)
            val textSeverity: TextView    = itemView.findViewById(R.id.textSeverity)
            val textKeywords: TextView    = itemView.findViewById(R.id.textKeywords)
            val playButton: ImageButton = itemView.findViewById(R.id.btnPlayAudio)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncidentViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_incident, parent, false)
            return IncidentViewHolder(view)
        }

        override fun onBindViewHolder(holder: IncidentViewHolder, position: Int) {
            val incident = incidentList[position]

            holder.textTitle.text = incident.title
            holder.textDate.text = incident.timestamp
            holder.textContacts.text = "Contacts: " + incident.contact.joinToString()
            holder.textLocation.text = "Location: " + incident.location
            holder.textSeverity.text    = "Severity: ${incident.severity}"
            holder.textKeywords.text    = "Keywords: ${incident.keywordsDetected.joinToString()}"

            // Show play button only if we have a non-null, existing audio file
            val path = incident.audioPath
            if (path != null && File(path).exists()) {
                holder.playButton.visibility = View.VISIBLE
                holder.playButton.setOnClickListener {
                    // disable button while playing
                    holder.playButton.isEnabled = false
                    MediaPlayer().apply {
                        setDataSource(path)
                        setOnPreparedListener { it.start() }
                        setOnCompletionListener {
                            it.release()
                            holder.playButton.isEnabled = true
                        }
                        prepareAsync()
                    }
                }
            } else {
                holder.playButton.visibility = View.GONE
            }
        }

        override fun getItemCount() = incidentList.size

        fun updateData(newList: List<Incident>) {
            incidentList = newList
            notifyDataSetChanged()
        }
    }