package cl.numiscoin2

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Moneda(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val pais: String,
    val anio: String,
    val estado: String,
    val valor: String,
    val fotos: List<FotoObjeto>? = null // Agregado campo fotos
): Parcelable

data class Coleccion(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val idUsuario: Int,
    val fechaCreacion: String
)

@Parcelize
data class ObjetoColeccion(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val idPais: Int,
    val nombrePais: String? = null,
    val anio: Int,
    val idTipoObjeto: Int,
    val idUsuario: Int,
    val fotos: List<FotoObjeto>?, // ✅ Correcto
    val fechaCreacion: String,
    val monedaInfo: MonedaInfo?,
    val fechaAgregado: String
): Parcelable

@Parcelize
data class MonedaInfo(
    val id: Int,
    val idObjeto: Int, // Cambiado de Long a Int para consistencia
    val familia: String?,
    val idFamilia: Int?,
    val variante: String?,
    val ceca: String?,
    val tipo: String?,
    val disenador: String?,
    val totalProducido: String?,
    val valorSinCircular: String?,
    val valorComercial: String?,
    val valorAdquirido: String?,
    val estado: String?,
    val observaciones: String?,
    val orden: Int?,
    val acunada: String?
): Parcelable

@Parcelize
data class FotoObjeto(
    val id: Int, // Cambiado de Long a Int para consistencia
    val url: String,
    val nombre: String?,
    val descripcion: String?,
    val esPrincipal: Boolean?,
    val fechaCreacion: String?,
    val orden: Int?
): Parcelable

data class ParamRequest(
    val nombre: String,
    val valor: String
)

data class MonedaRequest(
    val nombre: String,
    val descripcion: String? = null,
    val idPais: Int,
    val anio: Int,
    val idTipoObjeto: Int = 1,
    val idUsuario: Int,
    val idColeccion: Int,
    val familia: String? = null,
    val idFamilia: Int? = null,
    val variante: String? = null,
    val ceca: String? = null,
    val tipo: String? = null,
    val disenador: String? = null,
    val totalProducido: String? = null,
    val valorSinCircular: String? = null,
    val valorComercial: String? = null,
    val valorAdquirido: String? = null,
    val estado: String? = null,
    val observaciones: String? = null,
    val orden: Int? = null,
    val acunada: String? = null
)