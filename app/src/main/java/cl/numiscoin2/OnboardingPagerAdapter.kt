package cl.numiscoin2

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OnboardingPagerAdapter(private val activity: Activity) : RecyclerView.Adapter<OnboardingPagerAdapter.ViewHolder>() {

    private val onboardingItems = listOf(
        OnboardingItem(
            "Tu colección, siempre contigo",
            "Organiza, visualiza y protege tus tesoros numismáticos en un solo lugar. Desde monedas raras hasta billetes históricos, tu colección cobra vida con orden y estilo.",
            R.drawable.onboarding_1
        ),
        OnboardingItem(
            "Herramientas que te organizan",
            "Planifica tus intercambios, calcula precios y lleva el control de tus eventos con nuestra agenda y calculadora integrada. Todo lo que necesitas, al alcance de tu dedo.",
            R.drawable.onboarding_2
        ),
        OnboardingItem(
            "Cambio en tiempo real",
            "Consulta el valor actual del euro, dólar y oro en tiempo real. Toma decisiones informadas y mantente al día con el mercado numismático.",
            R.drawable.onboarding_3
        )
    )

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image: ImageView = itemView.findViewById(R.id.onboardingImage)
        private val title: TextView = itemView.findViewById(R.id.onboardingTitle)
        private val description: TextView = itemView.findViewById(R.id.onboardingDescription)
        //private val btnSkip: Button = itemView.findViewById(R.id.btnSkipIndividual)

        fun bind(onboardingItem: OnboardingItem, position: Int) {
            image.setImageResource(onboardingItem.imageRes)
            title.text = onboardingItem.title
            description.text = onboardingItem.description

            /*btnSkip.setOnClickListener {
                val intent = Intent(activity, LoginActivity::class.java)
                activity.startActivity(intent)
                activity.finish()
            }*/
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_onboarding, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(onboardingItems[position], position)
    }

    override fun getItemCount(): Int = onboardingItems.size
}

data class OnboardingItem(
    val title: String,
    val description: String,
    val imageRes: Int
)