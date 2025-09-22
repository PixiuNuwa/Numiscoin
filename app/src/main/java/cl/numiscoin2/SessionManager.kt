package cl.numiscoin2

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SessionManager {
    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    var usuario: Usuario? = null
    var isLoggedIn: Boolean = false

    // Constantes para las claves de preferencias
    private const val PREFS_NAME = "NumisCoinPrefs"
    private const val KEY_PAISES = "paises"
    private const val KEY_DIVISAS = "divisas"
    private const val KEY_METALES = "metales"
    private const val KEY_LAST_UPDATE_PAISES = "last_update_paises"
    private const val KEY_LAST_UPDATE_DIVISAS = "last_update_divisas"
    private const val KEY_LAST_UPDATE_METALES = "last_update_metales"

    // Tiempo de validez de la caché (24 horas en milisegundos)
    private const val CACHE_VALIDITY_MS = 24 * 60 * 60 * 1000

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun login(usuario: Usuario) {
        this.usuario = usuario
        this.isLoggedIn = true
    }

    fun logout() {
        this.usuario = null
        this.isLoggedIn = false
        // Limpiar datos sensibles pero mantener la caché
        prefs.edit().remove(KEY_LAST_UPDATE_PAISES)
            .remove(KEY_LAST_UPDATE_DIVISAS)
            .remove(KEY_LAST_UPDATE_METALES)
            .apply()
    }

    fun getUsuarioId(): Long {
        return usuario?.idUsuario ?: -1
    }

    // Métodos para paises
    fun savePaises(paises: List<Pais>) {
        val json = gson.toJson(paises)
        prefs.edit().putString(KEY_PAISES, json)
            .putLong(KEY_LAST_UPDATE_PAISES, System.currentTimeMillis())
            .apply()
    }

    fun getPaises(): List<Pais>? {
        val json = prefs.getString(KEY_PAISES, null)
        return if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<List<Pais>>() {}.type
            gson.fromJson(json, type)
        } else {
            null
        }
    }

    fun isPaisesCacheValid(): Boolean {
        val lastUpdate = prefs.getLong(KEY_LAST_UPDATE_PAISES, 0)
        return System.currentTimeMillis() - lastUpdate < CACHE_VALIDITY_MS
    }

    // Métodos para divisas
    fun saveDivisas(divisas: List<Divisa>) {
        val json = gson.toJson(divisas)
        prefs.edit().putString(KEY_DIVISAS, json)
            .putLong(KEY_LAST_UPDATE_DIVISAS, System.currentTimeMillis())
            .apply()
    }

    fun getDivisas(): List<Divisa>? {
        val json = prefs.getString(KEY_DIVISAS, null)
        return if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<List<Divisa>>() {}.type
            gson.fromJson(json, type)
        } else {
            null
        }
    }

    fun isDivisasCacheValid(): Boolean {
        val lastUpdate = prefs.getLong(KEY_LAST_UPDATE_DIVISAS, 0)
        return System.currentTimeMillis() - lastUpdate < CACHE_VALIDITY_MS
    }

    // Métodos para metales
    fun saveMetales(metales: List<Metal>) {
        val json = gson.toJson(metales)
        prefs.edit().putString(KEY_METALES, json)
            .putLong(KEY_LAST_UPDATE_METALES, System.currentTimeMillis())
            .apply()
    }

    fun getMetales(): List<Metal>? {
        val json = prefs.getString(KEY_METALES, null)
        return if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<List<Metal>>() {}.type
            gson.fromJson(json, type)
        } else {
            null
        }
    }

    fun isMetalesCacheValid(): Boolean {
        val lastUpdate = prefs.getLong(KEY_LAST_UPDATE_METALES, 0)
        return System.currentTimeMillis() - lastUpdate < CACHE_VALIDITY_MS
    }


    fun getUsdRate(): Double {
        return getDivisas()?.find { it.codigo == "USD" }?.valorEnCLP ?: 950.0
    }

    fun getEurRate(): Double {
        return getDivisas()?.find { it.codigo == "EUR" }?.valorEnCLP ?: 1020.0
    }

    fun getGoldPrice24kPerGram(): Double {
        return getMetales()?.find { it.quilates == 24 && it.unidad == "gramo" }?.precioClp ?: 112383.63
    }

    fun getGoldPrice22kPerGram(): Double {
        return getMetales()?.find { it.quilates == 22 && it.unidad == "gramo" }?.precioClp ?: 103018.4
    }
}