package cl.numiscoin2

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class Usuario(
    @SerializedName("id_usuario") val idUsuario: Long,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellido") val apellido: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String? = null,
    @SerializedName("fecha_creacion") val fechaCreacion: String,
    @SerializedName("foto") val foto: String,
    @SerializedName("cantidadMonedas") val cantidadMonedas: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?:"",
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(idUsuario)
        parcel.writeString(nombre)
        parcel.writeString(apellido)
        parcel.writeString(email)
        parcel.writeString(password)
        parcel.writeString(fechaCreacion)
        parcel.writeString(foto)
        parcel.writeInt(cantidadMonedas)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Usuario> {
        override fun createFromParcel(parcel: Parcel): Usuario {
            return Usuario(parcel)
        }

        override fun newArray(size: Int): Array<Usuario?> {
            return arrayOfNulls(size)
        }
    }
}

data class Divisa(
    val id: Int,
    val codigo: String,
    val nombre: String,
    val simbolo: String,
    val valorEnCLP: Double,
    val ultimaActualizacion: String,
    val esMonedaBase: Boolean
)

data class Metal(
    val id: Int,
    val tipoMetal: Int,
    val quilates: Int,
    val pureza: Double,
    val unidad: String,
    val precioClp: Double,
    val fuente: String,
    val creadoEn: String,
    val actualizadoEn: String
)