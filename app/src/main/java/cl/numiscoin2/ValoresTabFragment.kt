package cl.numiscoin2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.text.NumberFormat
import java.util.Locale

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
            val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
            formatoMoneda.maximumFractionDigits = 0

            view.findViewById<TextView>(R.id.tvTotalProducido).text = info.totalProducido ?: "No especificado"

            // Convertir String a Int antes de formatear
            val valorComercial = info.valorComercial?.toIntOrNull() ?: 0
            val valorAdquirido = info.valorAdquirido?.toIntOrNull() ?: 0

            view.findViewById<TextView>(R.id.tvValorComercial).text = formatoMoneda.format(valorComercial)
            view.findViewById<TextView>(R.id.tvValorAdquirido).text = formatoMoneda.format(valorAdquirido)
        }

        return view
    }
}
/*
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
            val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
            formatoMoneda.maximumFractionDigits = 0

            view.findViewById<TextView>(R.id.tvTotalProducido).text = info.totalProducido ?: "No especificado"
            //view.findViewById<TextView>(R.id.tvValorSinCircular).text = info.valorSinCircular ?: "No especificado"
            //view.findViewById<TextView>(R.id.tvValorComercial).text = info.valorComercial ?: "No especificado"
            //view.findViewById<TextView>(R.id.tvValorAdquirido).text = info.valorAdquirido ?: "No especificado"
            view.findViewById<TextView>(R.id.tvValorComercial).text = formatoMoneda.format(info.valorComercial ?: 0)
            view.findViewById<TextView>(R.id.tvValorAdquirido).text = formatoMoneda.format(info.valorAdquirido ?: 0)
        }

        return view
    }
}*/