package cl.numiscoin2

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import cl.numiscoin2.network.NetworkConfig
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class EventoAdapter(private val context: Context, private val eventos: List<Evento>) : BaseAdapter() {

    override fun getCount(): Int = eventos.size

    override fun getItem(position: Int): Evento = eventos[position]

    override fun getItemId(position: Int): Long = eventos[position].idEvento.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_evento, parent, false)
            holder = ViewHolder()
            holder.posterImage = view.findViewById(R.id.posterImage)
            holder.eventName = view.findViewById(R.id.eventName)
            holder.eventDate = view.findViewById(R.id.eventDate)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val evento = getItem(position)

        // Cargar imagen del poster
        if (evento.fotoPoster.isNotEmpty()) {
            val posterUrl = NetworkConfig.construirUrlCompleta(evento.fotoPoster)
            Glide.with(context)
                .load(posterUrl)
                .placeholder(R.drawable.circle_white_background)
                .error(R.drawable.circle_white_background)
                .into(holder.posterImage)
        } else {
            // Si no hay imagen, mostrar placeholder
            holder.posterImage.setBackgroundColor(Color.parseColor("#696969"))
        }

        holder.eventName.text = evento.nombreEvento
        holder.eventName.setTextColor(Color.parseColor("#000000"))

        holder.eventDate.text = formatDate(evento.fechaInicio)
        holder.eventDate.setTextColor(Color.parseColor("#696969"))

        return view
    }

    private fun formatDate(date: Date): String {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return format.format(date)
    }

    private class ViewHolder {
        lateinit var posterImage: ImageView
        lateinit var eventName: TextView
        lateinit var eventDate: TextView
    }
}