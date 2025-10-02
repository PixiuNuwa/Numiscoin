package cl.numiscoin2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class InfoTabFragment : Fragment() {

    companion object {
        fun newInstance(objeto: ObjetoColeccion): InfoTabFragment {
            val fragment = InfoTabFragment()
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
        val view = inflater.inflate(R.layout.fragment_info_tab, container, false)
        val objeto = arguments?.getParcelable<ObjetoColeccion>("objeto") ?: return view

        // Configurar los datos en la UI
        view.findViewById<TextView>(R.id.tvDescripcion).text = objeto.descripcion ?: "No especificado"
        view.findViewById<TextView>(R.id.tvAnio).text = objeto.anio?.toString() ?: "No especificado"
        view.findViewById<TextView>(R.id.tvPais).text = objeto.nombrePais ?: "No especificado"

        return view
    }
}