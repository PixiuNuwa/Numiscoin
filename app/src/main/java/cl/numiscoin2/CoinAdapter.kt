package cl.numiscoin2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cl.numiscoin2.network.NetworkConfig
import com.bumptech.glide.Glide

class CoinAdapter(
    private val coins: List<Moneda>,
    private val objetosCompletos: List<ObjetoColeccion> // ← Agregar esta lista
) : RecyclerView.Adapter<CoinAdapter.CoinViewHolder>() {

    // Interface para manejar clicks (opcional, pero mejor práctica)
    interface OnItemClickListener {
        fun onItemClick(objeto: ObjetoColeccion)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    class CoinViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val coinImage: ImageView = itemView.findViewById(R.id.coinImage)
        val coinName: TextView = itemView.findViewById(R.id.coinName)
        val coinYear: TextView = itemView.findViewById(R.id.coinYear)
        val coinValue: TextView = itemView.findViewById(R.id.coinValue)
        val coinState: TextView = itemView.findViewById(R.id.coinState)
        val coinObservations: TextView = itemView.findViewById(R.id.coinObservations)
    }

    private fun construirUrlCompleta(urlRelativa: String): String {
        return NetworkConfig.construirUrlCompleta(urlRelativa)
//        val baseUrl = "https://numiscoin.store/uploads/" // ← Cambiar a esta URL según tu JSON
//        return if (urlRelativa.startsWith("http")) {
//            urlRelativa
//        } else {
//            baseUrl + urlRelativa
//        }
    }

    override fun onBindViewHolder(holder: CoinViewHolder, position: Int) {
        val coin = coins[position]
        val objetoCompleto = objetosCompletos[position] // ← Obtener el objeto completo

        // Mostrar la primera foto si existe
        val primeraFoto = coin.fotos?.firstOrNull()
        if (primeraFoto != null && primeraFoto.url.isNotBlank()) {
            val imageUrl = construirUrlCompleta(primeraFoto.url)
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_delete)
                .into(holder.coinImage)
            holder.coinImage.visibility = View.VISIBLE
        } else {
            holder.coinImage.setImageResource(android.R.drawable.ic_menu_gallery)
            holder.coinImage.visibility = View.VISIBLE
        }

        // Mostrar los demás datos
        holder.coinName.text = coin.nombre
        holder.coinYear.text = coin.anio
        holder.coinValue.text = coin.valor
        holder.coinState.text = coin.estado
        holder.coinObservations.text = coin.descripcion

        // Manejar click en el item
        holder.itemView.setOnClickListener {
            listener?.onItemClick(objetoCompleto)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoinViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_coin, parent, false)
        return CoinViewHolder(view)
    }

    override fun getItemCount(): Int = coins.size
}
