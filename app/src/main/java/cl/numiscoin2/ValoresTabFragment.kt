package cl.numiscoin2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class ValoresTabFragment : Fragment() {

    companion object {
        fun newInstance(objeto: ObjetoColeccion): ValoresTabFragment {
            val fragment = ValoresTabFragment()
            val args = Bundle()
            args.putParcelable("objeto", objeto)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_valores_tab, container, false)
        val objeto = arguments?.getParcelable<ObjetoColeccion>("objeto") ?: return view

        // Configurar los datos en la UI
        objeto.monedaInfo?.let { info ->
            view.findViewById<TextView>(R.id.tvTotalProducido).text = info.totalProducido ?: "No especificado"
            //view.findViewById<TextView>(R.id.tvValorSinCircular).text = info.valorSinCircular ?: "No especificado"
            view.findViewById<TextView>(R.id.tvValorComercial).text = info.valorComercial ?: "No especificado"
            view.findViewById<TextView>(R.id.tvValorAdquirido).text = info.valorAdquirido ?: "No especificado"
        }

        return view
    }
}