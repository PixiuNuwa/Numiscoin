package cl.numiscoin2.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cl.numiscoin2.Evento
import cl.numiscoin2.R
import cl.numiscoin2.network.NetworkConfig
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class EventoHorizontalAdapter : RecyclerView.Adapter<EventoHorizontalAdapter.EventoViewHolder>() {

    private var eventos: List<Evento> = emptyList()
    var onItemClick: ((Evento) -> Unit)? = null

    inner class EventoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val posterImage: ImageView = itemView.findViewById(R.id.posterImage)
        val eventName: TextView = itemView.findViewById(R.id.eventName)
        val eventDate: TextView = itemView.findViewById(R.id.eventDate)

        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(eventos[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_evento_horizontal, parent, false)
        return EventoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventos[position]

        // Cargar imagen del poster
        if (evento.fotoPoster.isNotEmpty()) {
            val posterUrl = NetworkConfig.construirUrlCompleta(evento.fotoPoster)
            Glide.with(holder.itemView.context)
                .load(posterUrl)
                .placeholder(R.drawable.circle_white_background)
                .error(R.drawable.circle_white_background)
                .into(holder.posterImage)
        } else {
            holder.posterImage.setBackgroundColor(Color.parseColor("#696969"))
        }

        holder.eventName.text = evento.nombreEvento
        holder.eventName.setTextColor(Color.parseColor("#000000"))

        holder.eventDate.text = formatDate(evento.fechaInicio)
        holder.eventDate.setTextColor(Color.parseColor("#696969"))
    }

    override fun getItemCount(): Int = eventos.size

    fun actualizarEventos(nuevosEventos: List<Evento>) {
        this.eventos = nuevosEventos
        notifyDataSetChanged()
    }

    private fun formatDate(date: Date): String {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return format.format(date)
    }
}