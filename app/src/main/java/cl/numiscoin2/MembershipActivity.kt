package cl.numiscoin2

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import cl.numiscoin2.databinding.ActivityMembershipBinding

class MembershipActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMembershipBinding
    private lateinit var backButton: ImageView
    private lateinit var continueButton: Button
    private lateinit var termsLink: TextView
    private lateinit var privacyLink: TextView

    private var selectedMembership: String = "free" // Valor por defecto
    private var userId: Long = 0
    private var userEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMembershipBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Eliminar el título de la ActionBar
        supportActionBar?.hide()

        // Obtener datos del usuario desde el Intent
        userId = intent.getLongExtra("USER_ID", 0)
        userEmail = intent.getStringExtra("USER_EMAIL") ?: ""

        // Validar que tenemos un ID de usuario válido
        if (userId == 0L) {
            Toast.makeText(this, "Error: ID de usuario inválido", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        initializeViews()
        setupListeners()
        setupMembershipCards()
    }

    private fun initializeViews() {
        backButton = binding.backButton
        continueButton = binding.continueButton
        termsLink = binding.termsLink
        privacyLink = binding.privacyLink
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            onBackPressed()
        }

        continueButton.setOnClickListener {
            processMembershipSelection()
        }

        termsLink.setOnClickListener {
            // Abrir términos de servicio
            openWebView("https://tusitio.com/terminos")
        }

        privacyLink.setOnClickListener {
            // Abrir política de privacidad
            openWebView("https://tusitio.com/privacidad")
        }
    }

    private fun setupMembershipCards() {
        // Configurar listeners para cada tarjeta de membresía
        val freeCard = findViewById<CardView>(R.id.freeCard)
        val silverCard = findViewById<CardView>(R.id.silverCard)
        val goldCard = findViewById<CardView>(R.id.goldCard)
        val platinumCard = findViewById<CardView>(R.id.platinumCard)

        freeCard.setOnClickListener {
            selectMembership("free", freeCard, silverCard, goldCard, platinumCard)
        }

        silverCard.setOnClickListener {
            selectMembership("silver", freeCard, silverCard, goldCard, platinumCard)
        }

        goldCard.setOnClickListener {
            selectMembership("gold", freeCard, silverCard, goldCard, platinumCard)
        }

        platinumCard.setOnClickListener {
            selectMembership("platinum", freeCard, silverCard, goldCard, platinumCard)
        }

        // Seleccionar free por defecto
        selectMembership("free", freeCard, silverCard, goldCard, platinumCard)
    }

    private fun selectMembership(
        membershipType: String,
        freeCard: CardView,
        silverCard: CardView,
        goldCard: CardView,
        platinumCard: CardView
    ) {
        selectedMembership = membershipType

        // Resetear todas las tarjetas
        resetCardSelection(freeCard, silverCard, goldCard, platinumCard)

        // Seleccionar la tarjeta correspondiente
        when (membershipType) {
            "free" -> {
                setCardSelected(freeCard, true)
                updateContinueButtonText("Continuar con Free")
            }
            "silver" -> {
                setCardSelected(silverCard, true)
                updateContinueButtonText("Comprar Silver - $9.99/mes")
            }
            "gold" -> {
                setCardSelected(goldCard, true)
                updateContinueButtonText("Comprar Gold - $19.99/mes")
            }
            "platinum" -> {
                setCardSelected(platinumCard, true)
                updateContinueButtonText("Comprar Platinum - $29.99/mes")
            }
        }
    }

    private fun resetCardSelection(
        freeCard: CardView,
        silverCard: CardView,
        goldCard: CardView,
        platinumCard: CardView
    ) {
        setCardSelected(freeCard, false)
        setCardSelected(silverCard, false)
        setCardSelected(goldCard, false)
        setCardSelected(platinumCard, false)
    }

    private fun setCardSelected(cardView: CardView, selected: Boolean) {
        val selectionElement = cardView.findViewById<ImageView>(R.id.selection_element)

        if (selected) {
            selectionElement.setImageResource(R.drawable.ic_radio_selected)
            //cardView.setCardBackgroundColor(getColor(R.color.card_selected_background))
            cardView.cardElevation = 8f
        } else {
            selectionElement.setImageResource(R.drawable.ic_radio_unselected)
            cardView.setCardBackgroundColor(getColor(R.color.background_dark))
            cardView.cardElevation = 2f
        }
    }

    private fun updateContinueButtonText(text: String) {
        continueButton.text = text
    }

    private fun processMembershipSelection() {
        when (selectedMembership) {
            "free" -> {
                // Para membresía free, activar membresía gratuita
                completeRegistrationWithFreeMembership()
            }
            "silver", "gold", "platinum" -> {
                // Para membresías pagadas, ir al proceso de pago
                goToPaymentProcess()
            }
        }
    }

    private fun completeRegistrationWithFreeMembership() {
        // Mostrar loading
        showLoading(true)

        // Llamar al método para activar la membresía gratuita
        NetworkUserUtils.completeRegistrationWithFreeMembership(userId) { success, message, usuario ->
            runOnUiThread {
                showLoading(false)
                if (success && usuario != null) {
                    val cantidadMonedas = usuario?.cantidadMonedas ?: 0
                    Log.d("MembershipActivity", "Cantidad de monedas para este usuario es: $cantidadMonedas")
                    // Guardar el usuario en SessionManager (con la membresía actualizada)
                    SessionManager.login(usuario)

                    Toast.makeText(
                        this@MembershipActivity,
                        "¡Membresía gratuita activada exitosamente!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Ir a WelcomeActivity
                    goToWelcomeActivity()
                } else {
                    Toast.makeText(
                        this@MembershipActivity,
                        "Error: $message",
                        Toast.LENGTH_LONG
                    ).show()

                    // Permitir reintentar
                    enableRetryOption()
                }
            }
        }
    }

    private fun goToWelcomeActivity() {
        val intent = Intent(this, WelcomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun enableRetryOption() {
        // Habilitar el botón para reintentar
        continueButton.isEnabled = true
        continueButton.text = "Reintentar"
    }

    private fun goToPaymentProcess() {
        // Mostrar diálogo informativo OBLIGATORIO
        showExternalPaymentWarning()
    }

    private fun showExternalPaymentWarning() {
        AlertDialog.Builder(this)
            .setTitle("Compra de Membresía Externa")
            .setMessage("La membresía se gestiona a través de nuestro sitio web. Serás redirigido para completar la compra. Esta compra es independiente de la tienda de aplicaciones.")
            .setPositiveButton("Continuar al Sitio Web") { _, _ ->
                iniciarProcesoPagoExterno()
            }
            .setNegativeButton("Cancelar", null)
            .setNeutralButton("Más Información") { _, _ ->
                abrirPaginaInformacion()
            }
            .show()
    }

    private fun iniciarProcesoPagoExterno() {
        showLoading(true)

        val idMembresia = MembershipTypes.getMembershipId(selectedMembership)

        // Construir la URL directamente con los parámetros requeridos
        val paymentUrl = "https://f70ba7db6da1.ngrok-free.app/payment.html?idusuario=$userId&idmembresia=$idMembresia"

        runOnUiThread {
            showLoading(false)
            // Abrir directamente en navegador externo
            abrirEnNavegadorExterno(paymentUrl)
        }
    }

    private fun abrirEnNavegadorExterno(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

        // Verificar que hay un navegador disponible
        if (intent.resolveActivity(packageManager) != null) {
            try {
                startActivity(intent)
                mostrarMensajePostRedireccion()
            } catch (e: Exception) {
                // Si falla la apertura, mostrar opción para copiar URL
                mostrarDialogoURLCopia(url, "Error al abrir el navegador")
            }
        } else {
            // No hay navegador disponible, ofrecer copiar URL
            mostrarDialogoURLCopia(url, "Navegador no encontrado")
        }
    }

    private fun mostrarMensajePostRedireccion() {
        Toast.makeText(
            this,
            "✅ Abriendo navegador...\nDespués del pago, vuelve a la aplicación",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun mostrarDialogoURLCopia(url: String, motivo: String) {
        AlertDialog.Builder(this)
            .setTitle("Abrir en Navegador Externo")
            .setMessage("$motivo\n\nPuedes copiar el enlace de pago y abrirlo manualmente en tu navegador web.")
            .setPositiveButton("Copiar URL y Abrir") { _, _ ->
                copiarURLYProceder(url)
            }
            .setNeutralButton("Solo Copiar URL") { _, _ ->
                copiarURLAlPortapapeles(url)
                Toast.makeText(this, "URL copiada al portapapeles", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun copiarURLYProceder(url: String) {
        if (copiarURLAlPortapapeles(url)) {
            // Intentar abrir nuevamente por si acaso
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            try {
                startActivity(intent)
                Toast.makeText(this, "✅ URL copiada - Abriendo navegador...", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "✅ URL copiada - Pega en tu navegador: $url",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun copiarURLAlPortapapeles(url: String): Boolean {
        return try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("URL de Pago", url)
            clipboard.setPrimaryClip(clip)
            true
        } catch (e: Exception) {
            Toast.makeText(this, "Error al copiar URL", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun abrirPaginaInformacion() {
        val url = "https://f70ba7db6da1.ngrok-free.app/payment.html"
        abrirEnNavegadorExterno(url)
    }

    private fun openWebView(url: String) {
        // TODO: Implementar WebView para términos y privacidad
        Toast.makeText(this, "Abrir: $url", Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(show: Boolean) {
        continueButton.isEnabled = !show
        continueButton.text = if (show) "Procesando..." else getButtonTextForMembership()

        // Opcional: agregar un ProgressBar si no existe
        val progressBar = findViewById<ProgressBar?>(R.id.progressBar)
        progressBar?.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun getButtonTextForMembership(): String {
        return when (selectedMembership) {
            "free" -> "Continuar con Free"
            "silver" -> "Comprar Silver - $9.99/mes"
            "gold" -> "Comprar Gold - $19.99/mes"
            "platinum" -> "Comprar Platinum - $29.99/mes"
            else -> "Continuar"
        }
    }

    override fun onBackPressed() {
        // Preguntar si realmente quiere cancelar el registro
        AlertDialog.Builder(this)
            .setTitle("Cancelar registro")
            .setMessage("¿Estás seguro de que quieres cancelar el registro? Se perderán todos los datos.")
            .setPositiveButton("Sí, cancelar") { _, _ ->
                super.onBackPressed()
            }
            .setNegativeButton("Continuar", null)
            .show()
    }
}

object MembershipTypes {
    const val FREE = 1
    const val SILVER = 2
    const val GOLD = 3
    const val PLATINUM = 4

    fun getMembershipId(type: String): Int {
        return when (type) {
            "free" -> FREE
            "silver" -> SILVER
            "gold" -> GOLD
            "platinum" -> PLATINUM
            else -> FREE
        }
    }
}