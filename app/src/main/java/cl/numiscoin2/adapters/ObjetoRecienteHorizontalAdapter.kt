package cl.numiscoin2.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cl.numiscoin2.ObjetoColeccion
import cl.numiscoin2.R
import cl.numiscoin2.network.NetworkConfig
import com.bumptech.glide.Glide

class ObjetoRecienteHorizontalAdapter(
    private var objetos: List<ObjetoColeccion> = emptyList()
) : RecyclerView.Adapter<ObjetoRecienteHorizontalAdapter.ObjetoViewHolder>() {

    private val TAG = "ObjetoRecienteHorizontalAdapter"

    var onItemClick: ((ObjetoColeccion) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObjetoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_objeto_reciente_horizontal, parent, false)
        return ObjetoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ObjetoViewHolder, position: Int) {
        val objeto = objetos[position]
        holder.bind(objeto)

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(objeto)
        }
    }

    override fun getItemCount(): Int = objetos.size

    fun actualizarObjetos(nuevosObjetos: List<ObjetoColeccion>) {
        this.objetos = nuevosObjetos
        notifyDataSetChanged()
    }

    inner class ObjetoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.objetoImageView)
        private val nombreTextView: TextView = itemView.findViewById(R.id.objetoNombreTextView)

        fun bind(objeto: ObjetoColeccion) {
            // Establecer nombre
            nombreTextView.text = objeto.nombre ?: "Sin nombre"

            // Cargar imagen
            val primeraFoto = objeto.fotos?.firstOrNull()
            if (primeraFoto?.url != null) {
                val imageUrl = NetworkConfig.construirUrlCompleta(primeraFoto.url)
                Glide.with(itemView.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.placeholder_image)
                    .centerCrop()
                    .into(imageView)
            } else {
                imageView.setImageResource(R.drawable.placeholder_image)
            }
        }
    }
}