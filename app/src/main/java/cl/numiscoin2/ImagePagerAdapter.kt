package cl.numiscoin2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions

class ImagePagerAdapter(private val fotos: List<FotoObjeto>) : RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_coin_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val foto = fotos[position]
        holder.bind(foto)
    }

    override fun getItemCount(): Int = fotos.size

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.ivCoinImage)

        fun bind(foto: FotoObjeto) {
            // Asumiendo que FotoObjeto tiene una propiedad 'url'
            // Si no es 'url', ajusta según el nombre real de la propiedad
            val imageUrl = "https://numiscoin.store/uploads/" + foto.url

            // Cargar imagen con Glide aplicando forma circular
            Glide.with(itemView.context)
                .load(imageUrl)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .into(imageView)
        }
    }
}
/*import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import cl.numiscoin2.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions

class ImagePagerAdapter(private val imageUrls: List<String>) : RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_coin_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = imageUrls[position]
        holder.bind(imageUrl)
    }

    override fun getItemCount(): Int = imageUrls.size

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.ivCoinImage)

        fun bind(imageUrl: String) {
            // Cargar imagen con Glide o Picasso aplicando forma circular
            Glide.with(itemView.context)
                .load(imageUrl)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .into(imageView)
        }
    }
}*/