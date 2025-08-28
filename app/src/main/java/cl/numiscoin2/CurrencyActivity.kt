package cl.numiscoin2

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.core.widget.addTextChangedListener

class CurrencyActivity : BaseActivity() {

    // Tasas de cambio (valores de ejemplo)
    private val usdRate = 950.0 // 1 USD = 950 CLP
    private val eurRate = 1020.0 // 1 EUR = 1020 CLP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_currency)

        val userName = intent.getStringExtra(EXTRA_USER_NAME) ?: "Usuario"

        val welcomeMessage = findViewById<TextView>(R.id.welcomeMessage)
        val usdRateText = findViewById<TextView>(R.id.usdRate)
        val eurRateText = findViewById<TextView>(R.id.eurRate)
        val amountInput = findViewById<EditText>(R.id.amountInput)
        val fromSpinner = findViewById<Spinner>(R.id.fromSpinner)
        val toSpinner = findViewById<Spinner>(R.id.toSpinner)
        val convertButton = findViewById<Button>(R.id.convertButton)
        val resultText = findViewById<TextView>(R.id.resultText)

        welcomeMessage.text = "Bienvenido $userName"
        usdRateText.text = "1 USD = ${usdRate.toInt()} CLP"
        eurRateText.text = "1 EUR = ${eurRate.toInt()} CLP"

        // Configurar spinners
        val currencies = arrayOf("CLP", "USD", "EUR")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        fromSpinner.adapter = adapter
        toSpinner.adapter = adapter

        // Establecer valores por defecto
        fromSpinner.setSelection(0) // CLP
        toSpinner.setSelection(1) // USD

        // Función para realizar la conversión
        fun performConversion() {
            val amountStr = amountInput.text.toString()
            if (amountStr.isBlank()) {
                resultText.text = "Ingrese un monto"
                return
            }

            val amount = amountStr.toDouble()
            val fromCurrency = fromSpinner.selectedItem.toString()
            val toCurrency = toSpinner.selectedItem.toString()

            val result = convertCurrency(amount, fromCurrency, toCurrency)
            resultText.text = String.format("%.2f %s = %.2f %s", amount, fromCurrency, result, toCurrency)
        }

        // Configurar eventos
        convertButton.setOnClickListener {
            performConversion()
        }

        amountInput.addTextChangedListener {
            if (it?.length!! > 0) {
                performConversion()
            }
        }

        fromSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                if (amountInput.text.isNotEmpty()) {
                    performConversion()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        toSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                if (amountInput.text.isNotEmpty()) {
                    performConversion()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Configurar menú inferior
        setupBottomMenu()
        highlightMenuItem(R.id.menuCurrency) // Marcar Divisas como seleccionado
    }

    private fun convertCurrency(amount: Double, from: String, to: String): Double {
        if (from == to) return amount

        // Convertir primero a CLP
        val amountInClp = when (from) {
            "USD" -> amount * usdRate
            "EUR" -> amount * eurRate
            else -> amount // Ya está en CLP
        }

        // Convertir de CLP a la moneda destino
        return when (to) {
            "USD" -> amountInClp / usdRate
            "EUR" -> amountInClp / eurRate
            else -> amountInClp // CLP
        }
    }

    companion object {
        const val EXTRA_USER_NAME = "extra_user_name"

        fun start(activity: ComponentActivity, userName: String) {
            val intent = Intent(activity, CurrencyActivity::class.java)
            intent.putExtra(EXTRA_USER_NAME, userName)
            activity.startActivity(intent)
        }
    }
}