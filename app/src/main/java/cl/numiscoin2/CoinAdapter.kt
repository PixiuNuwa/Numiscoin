package cl.numiscoin2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CoinAdapter(private val coins: List<Moneda>) : RecyclerView.Adapter<CoinAdapter.CoinViewHolder>() {

    class CoinViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val coinName: TextView = itemView.findViewById(R.id.coinName)
        val coinYear: TextView = itemView.findViewById(R.id.coinYear)
        val coinValue: TextView = itemView.findViewById(R.id.coinValue)
        val coinState: TextView = itemView.findViewById(R.id.coinState)
        val coinObservations: TextView = itemView.findViewById(R.id.coinObservations)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoinViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_coin, parent, false)
        return CoinViewHolder(view)
    }

    override fun onBindViewHolder(holder: CoinViewHolder, position: Int) {
        val coin = coins[position]

        holder.coinName.text = coin.nombre ?: "Sin nombre"
        holder.coinYear.text = coin.anio ?: "N/A"
        holder.coinValue.text = coin.pais ?: "N/A"
        holder.coinState.text = coin.estado ?: "N/A"
        holder.coinObservations.text = coin.descripcion ?: "Sin observaciones"
    }

    override fun getItemCount(): Int = coins.size
}