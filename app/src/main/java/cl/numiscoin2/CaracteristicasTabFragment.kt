package cl.numiscoin2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class CaracteristicasTabFragment : Fragment() {

    companion object {
        fun newInstance(objeto: ObjetoColeccion): CaracteristicasTabFragment {
            val fragment = CaracteristicasTabFragment()
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
        val view = inflater.inflate(R.layout.fragment_caracteristicas_tab, container, false)
        val objeto = arguments?.getParcelable<ObjetoColeccion>("objeto") ?: return view

        // Configurar los datos en la UI
        objeto.monedaInfo?.let { info ->
            view.findViewById<TextView>(R.id.tvVariante).text = info.variante ?: "No especificado"
            view.findViewById<TextView>(R.id.tvCeca).text = info.ceca ?: "No especificado"
            view.findViewById<TextView>(R.id.tvTipo).text = info.tipo ?: "No especificado"
            view.findViewById<TextView>(R.id.tvDisenador).text = info.disenador ?: "No especificado"
            view.findViewById<TextView>(R.id.tvEstado).text = info.estado ?: "No especificado"
            view.findViewById<TextView>(R.id.tvObservaciones).text = info.observaciones ?: "Sin observaciones"
        }

        return view
    }
}