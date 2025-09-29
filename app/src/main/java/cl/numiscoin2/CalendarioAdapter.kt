package cl.numiscoin2

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.util.*
import java.text.SimpleDateFormat

class CalendarioAdapter(
    private val context: Context,
    private val dias: List<String>,
    private val eventos: List<Evento>,
    private val año: Int,
    private val mes: Int
) : BaseAdapter() {

    override fun getCount(): Int = dias.size

    override fun getItem(position: Int): String = dias[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_calendario, parent, false)
            holder = ViewHolder()
            holder.diaText = view.findViewById(R.id.diaText)
            holder.marcadorEvento = view.findViewById(R.id.marcadorEvento)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val dia = getItem(position)

        if (dia.isNotEmpty()) {
            holder.diaText.text = dia
            holder.diaText.setTextColor(Color.parseColor("#000000"))
            holder.diaText.visibility = View.VISIBLE

            // Verificar si este día tiene eventos
            val tieneEvento = tieneEventoEnDia(dia.toInt())
            if (tieneEvento) {
                holder.marcadorEvento.visibility = View.VISIBLE
                holder.marcadorEvento.setBackgroundColor(Color.parseColor("#FF0000"))
            } else {
                holder.marcadorEvento.visibility = View.INVISIBLE
            }

            // Resaltar día actual
            val calendar = Calendar.getInstance()
            val diaActual = calendar.get(Calendar.DAY_OF_MONTH)
            val mesActual = calendar.get(Calendar.MONTH) + 1
            val añoActual = calendar.get(Calendar.YEAR)

            if (dia.toInt() == diaActual && mes == mesActual && año == añoActual) {
                view.setBackgroundColor(Color.parseColor("#E3F2FD")) // Azul claro para día actual
            } else {
                view.setBackgroundColor(Color.parseColor("#FFFFFF")) // Blanco para otros días
            }

            // Diferenciar días del mes actual vs días vacíos
            view.alpha = 1.0f

        } else {
            // Día vacío
            holder.diaText.visibility = View.INVISIBLE
            holder.marcadorEvento.visibility = View.INVISIBLE
            view.setBackgroundColor(Color.parseColor("#F5F5F5")) // Gris muy claro para días vacíos
            view.alpha = 0.5f
        }

        return view
    }

    private fun tieneEventoEnDia(dia: Int): Boolean {
        return eventos.any { evento ->
            val calendar = Calendar.getInstance().apply {
                time = evento.fechaInicio
            }
            val eventoDia = calendar.get(Calendar.DAY_OF_MONTH)
            val eventoMes = calendar.get(Calendar.MONTH) + 1
            val eventoAño = calendar.get(Calendar.YEAR)

            eventoDia == dia && eventoMes == mes && eventoAño == año
        }
    }

    private class ViewHolder {
        lateinit var diaText: TextView
        lateinit var marcadorEvento: View
    }
}