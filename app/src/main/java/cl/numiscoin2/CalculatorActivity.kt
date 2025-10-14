package cl.numiscoin2

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.tan
import kotlin.math.PI

class CalculatorActivity : BaseActivity() {

    private lateinit var display: TextView
    private var currentInput = ""
    private var currentResult = ""
    private var isDegMode = true // true para grados, false para radianes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //
        window.navigationBarColor = ContextCompat.getColor(this, R.color.background_dark)
        window.statusBarColor = ContextCompat.getColor(this, R.color.background_dark)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        }
        //
        setContentView(R.layout.activity_calculator)

        display = findViewById(R.id.display)

        // Configurar menú inferior
        setupBottomMenu()
        highlightMenuItem(R.id.menuCalculator)

        // Configurar listeners de los botones
        setupCalculatorButtons()
    }

    private fun setupCalculatorButtons() {
        // Botones numéricos
        findViewById<TextView>(R.id.button_0).setOnClickListener { appendNumber("0") }
        findViewById<TextView>(R.id.button_1).setOnClickListener { appendNumber("1") }
        findViewById<TextView>(R.id.button_2).setOnClickListener { appendNumber("2") }
        findViewById<TextView>(R.id.button_3).setOnClickListener { appendNumber("3") }
        findViewById<TextView>(R.id.button_4).setOnClickListener { appendNumber("4") }
        findViewById<TextView>(R.id.button_5).setOnClickListener { appendNumber("5") }
        findViewById<TextView>(R.id.button_6).setOnClickListener { appendNumber("6") }
        findViewById<TextView>(R.id.button_7).setOnClickListener { appendNumber("7") }
        findViewById<TextView>(R.id.button_8).setOnClickListener { appendNumber("8") }
        findViewById<TextView>(R.id.button_9).setOnClickListener { appendNumber("9") }

        // Operadores básicos
        findViewById<TextView>(R.id.addition).setOnClickListener { appendOperator("+") }
        findViewById<TextView>(R.id.subtraction).setOnClickListener { appendOperator("-") }
        findViewById<TextView>(R.id.multiplication).setOnClickListener { appendOperator("*") }
        findViewById<TextView>(R.id.division).setOnClickListener { appendOperator("/") }
        findViewById<TextView>(R.id.decimal).setOnClickListener { appendDecimal() }

        // Funciones especiales
        //findViewById<Button>(R.id.btnSin).setOnClickListener { calculateFunction("sin") }
        //findViewById<Button>(R.id.btnCos).setOnClickListener { calculateFunction("cos") }
        //findViewById<Button>(R.id.btnCos).setOnClickListener { calculateFunction("tan") }

        // Botones de control
        findViewById<TextView>(R.id.C).setOnClickListener { clearAll() }
        findViewById<TextView>(R.id.erase).setOnClickListener { backspace() }
        findViewById<TextView>(R.id.equals).setOnClickListener { calculateResult() }
        //findViewById<Button>(R.id.btnToggleMode).setOnClickListener { toggleAngleMode() }
    }

    private fun appendNumber(number: String) {
        currentInput += number
        updateDisplay()
    }

    private fun appendOperator(operator: String) {
        if (currentInput.isNotEmpty() && !currentInput.endsWith(" ")) {
            currentInput += " $operator "
            updateDisplay()
        }
    }

    private fun appendDecimal() {
        if (currentInput.isEmpty() || !currentInput.split(" ").last().contains(".")) {
            currentInput += if (currentInput.isEmpty() || currentInput.endsWith(" ")) {
                "0."
            } else {
                "."
            }
            updateDisplay()
        }
    }

    private fun calculateFunction(function: String) {
        if (currentInput.isNotEmpty() && !currentInput.contains(" ")) {
            try {
                val number = currentInput.toDouble()
                val result = when (function) {
                    "sin" -> if (isDegMode) sin(number * PI / 180) else sin(number)
                    "cos" -> if (isDegMode) cos(number * PI / 180) else cos(number)
                    "tan" -> if (isDegMode) tan(number * PI / 180) else tan(number)
                    else -> 0.0
                }
                currentResult = result.toString()
                currentInput = result.toString()
                updateDisplay()
            } catch (e: Exception) {
                currentInput = "Error"
                updateDisplay()
            }
        }
    }

    private fun calculateResult() {
        try {
            val tokens = currentInput.split(" ")
            if (tokens.size >= 3) {
                var result = tokens[0].toDouble()
                var i = 1
                while (i < tokens.size) {
                    val operator = tokens[i]
                    val nextNumber = tokens[i + 1].toDouble()

                    result = when (operator) {
                        "+" -> result + nextNumber
                        "-" -> result - nextNumber
                        "*" -> result * nextNumber
                        "/" -> result / nextNumber
                        else -> result
                    }
                    i += 2
                }
                currentResult = result.toString()
                currentInput = result.toString()
                updateDisplay()
            }
        } catch (e: Exception) {
            currentInput = "Error"
            updateDisplay()
        }
    }

    private fun clearAll() {
        currentInput = ""
        currentResult = ""
        updateDisplay()
    }

    private fun backspace() {
        if (currentInput.isNotEmpty()) {
            currentInput = currentInput.dropLast(1).trim()
            updateDisplay()
        }
    }

    /*private fun toggleAngleMode() {
        isDegMode = !isDegMode
        val modeText = findViewById<TextView>(R.id.btnToggleMode)
        modeText.text = if (isDegMode) "DEG" else "RAD"
    }*/

    private fun updateDisplay() {
        display.text = currentInput.ifEmpty { "0" }
    }
}