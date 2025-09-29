package cl.numiscoin2

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener

class CurrencyActivity : BaseActivity() {

    private lateinit var usdRateText: TextView
    private lateinit var eurRateText: TextView
    private lateinit var amountInput: EditText
    private lateinit var fromSpinner: Spinner
    private lateinit var toSpinner: Spinner
    private lateinit var resultText: TextView

    private lateinit var goldAmountInput: EditText
    private lateinit var goldCaratSpinner: Spinner
    private lateinit var goldUnitSpinner: Spinner
    private lateinit var goldResultText: TextView

    private lateinit var gold24kPerGram: TextView
    private lateinit var gold22kPerGram: TextView
    private lateinit var gold24kPerOz: TextView
    private lateinit var gold22kPerOz: TextView

    private var usdRate: Double = 0.0
    private var eurRate: Double = 0.0
    private var gold24kPerGramPrice: Double = 0.0
    private var gold22kPerGramPrice: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_currency)

        initializeViews()
        setupCurrencyData()
        setupGoldData()
        setupSpinners()
        setupListeners()
        setupBottomMenu()
        highlightMenuItem(R.id.menuCurrency)
    }

    private fun initializeViews() {
        val welcomeMessage = findViewById<TextView>(R.id.welcomeMessage)
        usdRateText = findViewById(R.id.usdRate)
        eurRateText = findViewById(R.id.eurRate)
        amountInput = findViewById(R.id.amountInput)
        fromSpinner = findViewById(R.id.fromSpinner)
        toSpinner = findViewById(R.id.toSpinner)
        resultText = findViewById(R.id.resultText)

        goldAmountInput = findViewById(R.id.goldAmountInput)
        goldCaratSpinner = findViewById(R.id.goldCaratSpinner)
        goldUnitSpinner = findViewById(R.id.goldUnitSpinner)
        goldResultText = findViewById(R.id.goldResultText)

        gold24kPerGram = findViewById(R.id.gold24kPerGram)
        gold22kPerGram = findViewById(R.id.gold22kPerGram)
        gold24kPerOz = findViewById(R.id.gold24kPerOz)
        gold22kPerOz = findViewById(R.id.gold22kPerOz)

        val userName = SessionManager.usuario?.let { "${it.nombre} ${it.apellido}" } ?: "Usuario"
        welcomeMessage.text = "Bienvenido $userName"
    }

    private fun setupCurrencyData() {
        // Obtener divisas desde SessionManager
        val divisas = SessionManager.getDivisas()

        if (divisas != null) {
            divisas.forEach { divisa ->
                when (divisa.codigo) {
                    "USD" -> usdRate = divisa.valorEnCLP
                    "EUR" -> eurRate = divisa.valorEnCLP
                }
            }

            usdRateText.text = "1 USD = ${String.format("%.2f", usdRate)} CLP"
            eurRateText.text = "1 EUR = ${String.format("%.2f", eurRate)} CLP"
        } else {
            // Si no hay datos en caché, cargar desde servidor
            NetworkDataUtils.getDivisas { divisas, error ->
                runOnUiThread {
                    if (divisas != null) {
                        SessionManager.saveDivisas(divisas)
                        divisas.forEach { divisa ->
                            when (divisa.codigo) {
                                "USD" -> usdRate = divisa.valorEnCLP
                                "EUR" -> eurRate = divisa.valorEnCLP
                            }
                        }
                        usdRateText.text = "1 USD = ${String.format("%.2f", usdRate)} CLP"
                        eurRateText.text = "1 EUR = ${String.format("%.2f", eurRate)} CLP"
                    } else {
                        // Usar valores por defecto en caso de error
                        usdRate = 950.0
                        eurRate = 1020.0
                        usdRateText.text = "1 USD = ${usdRate.toInt()} CLP"
                        eurRateText.text = "1 EUR = ${eurRate.toInt()} CLP"
                        Toast.makeText(this, "Error cargando tasas de cambio", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupGoldData() {
        // Obtener metales desde SessionManager
        val metales = SessionManager.getMetales()

        if (metales != null) {
            processMetales(metales)
        } else {
            // Si no hay datos en caché, cargar desde servidor
            NetworkDataUtils.getMetales { metales, error ->
                runOnUiThread {
                    if (metales != null) {
                        SessionManager.saveMetales(metales)
                        processMetales(metales)
                    } else {
                        // Usar valores por defecto en caso de error
                        gold24kPerGramPrice = 112383.63
                        gold22kPerGramPrice = 103018.4
                        updateGoldReferencePrices()
                        Toast.makeText(this, "Error cargando precios del oro", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun processMetales(metales: List<Metal>) {
        metales.forEach { metal ->
            when {
                metal.quilates == 24 && metal.unidad == "gramo" -> gold24kPerGramPrice = metal.precioClp
                metal.quilates == 22 && metal.unidad == "gramo" -> gold22kPerGramPrice = metal.precioClp
            }
        }
        updateGoldReferencePrices()
    }

    private fun updateGoldReferencePrices() {
        // Calcular precios por onza (1 onza = 31.1035 gramos)
        val gold24kPerOzPrice = gold24kPerGramPrice * 31.1035
        val gold22kPerOzPrice = gold22kPerGramPrice * 31.1035

        gold24kPerGram.text = String.format("$%,.2f", gold24kPerGramPrice)
        gold22kPerGram.text = String.format("$%,.2f", gold22kPerGramPrice)
        gold24kPerOz.text = String.format("$%,.2f", gold24kPerOzPrice)
        gold22kPerOz.text = String.format("$%,.2f", gold22kPerOzPrice)
    }

    private fun setupSpinners() {
        // Spinners de divisas
        val currencies = arrayOf("CLP", "USD", "EUR")
        val currencyAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        fromSpinner.adapter = currencyAdapter
        toSpinner.adapter = currencyAdapter
        fromSpinner.setSelection(0) // CLP
        toSpinner.setSelection(1) // USD

        // Spinners de oro
        val carats = arrayOf("24 quilates", "22 quilates")
        val units = arrayOf("Gramos", "Onzas")

        val caratAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, carats)
        caratAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        goldCaratSpinner.adapter = caratAdapter

        val unitAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, units)
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        goldUnitSpinner.adapter = unitAdapter
    }

    private fun setupListeners() {
        // Conversor de divisas
        val convertButton = findViewById<Button>(R.id.convertButton)
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

        // Conversor de oro
        val convertGoldButton = findViewById<Button>(R.id.convertGoldButton)
        convertGoldButton.setOnClickListener {
            performGoldConversion()
        }

        goldAmountInput.addTextChangedListener {
            if (it?.length!! > 0) {
                performGoldConversion()
            }
        }

        goldCaratSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                if (goldAmountInput.text.isNotEmpty()) {
                    performGoldConversion()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        goldUnitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                if (goldAmountInput.text.isNotEmpty()) {
                    performGoldConversion()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun performConversion() {
        val amountStr = amountInput.text.toString()
        if (amountStr.isBlank()) {
            resultText.text = "Ingrese un monto"
            return
        }

        val amount = amountStr.toDouble()
        val fromCurrency = fromSpinner.selectedItem.toString()
        val toCurrency = toSpinner.selectedItem.toString()

        val result = convertCurrency(amount, fromCurrency, toCurrency)
        resultText.text = String.format("%,.2f %s = %,.2f %s", amount, fromCurrency, result, toCurrency)
    }

    private fun performGoldConversion() {
        val amountStr = goldAmountInput.text.toString()
        if (amountStr.isBlank()) {
            goldResultText.text = "Ingrese la cantidad de oro"
            return
        }

        val amount = amountStr.toDouble()
        val carat = goldCaratSpinner.selectedItem.toString()
        val unit = goldUnitSpinner.selectedItem.toString()

        val result = calculateGoldValue(amount, carat, unit)
        goldResultText.text = String.format("%,.2f %s de oro %s = $%,.2f CLP", amount, unit.toLowerCase(), carat, result)
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

    private fun calculateGoldValue(amount: Double, carat: String, unit: String): Double {
        // Determinar el precio base por gramo según los quilates
        val basePricePerGram = when (carat) {
            "24 quilates" -> gold24kPerGramPrice
            "22 quilates" -> gold22kPerGramPrice
            else -> gold24kPerGramPrice
        }

        // Ajustar según la unidad
        return when (unit) {
            "Gramos" -> amount * basePricePerGram
            "Onzas" -> amount * basePricePerGram * 31.1035 // 1 onza = 31.1035 gramos
            else -> amount * basePricePerGram
        }
    }

    companion object {
        const val EXTRA_USER_NAME = "extra_user_name"

        fun start(activity: AppCompatActivity, userName: String) {
            val intent = Intent(activity, CurrencyActivity::class.java)
            intent.putExtra(EXTRA_USER_NAME, userName)
            activity.startActivity(intent)
        }
    }
}