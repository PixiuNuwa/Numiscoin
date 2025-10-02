//<<ImagePagerAdapter
package cl.numiscoin2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import android.view.ScaleGestureDetector
import android.view.MotionEvent

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
        private var scaleGestureDetector: ScaleGestureDetector? = null
        private var scaleFactor = 1.0f

        init {
            setupZoom()
        }

        fun bind(foto: FotoObjeto) {
            val imageUrl = "https://numiscoin.store/uploads/" + foto.url

            // Cargar imagen con Glide
            Glide.with(itemView.context)
                .load(imageUrl)
                .into(imageView)
        }

        private fun setupZoom() {
            scaleGestureDetector = ScaleGestureDetector(itemView.context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    scaleFactor *= detector.scaleFactor
                    scaleFactor = scaleFactor.coerceIn(0.5f, 5.0f) // Límites de zoom
                    imageView.scaleX = scaleFactor
                    imageView.scaleY = scaleFactor
                    return true
                }
            })

            imageView.setOnTouchListener { _, event ->
                scaleGestureDetector?.onTouchEvent(event)
                true
            }
        }
    }
}
//>>ImagePagerAdapter
//>>ImagePagerAdapter
/*package cl.numiscoin2

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
}*/
