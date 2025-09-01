package cl.numiscoin2

data class Moneda(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val pais: String,
    val anio:String,
    val estado:String,
    val valor:String
)
data class Coleccion(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val idUsuario: Int,
    val fechaCreacion: String
)

data class ObjetoColeccion(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val idPais: Int,
    val anio: Int,
    val idTipoObjeto: Int,
    val idUsuario: Int,
    val fechaCreacion: String,
    val monedaInfo: MonedaInfo?,
    val fechaAgregado: String
)

data class MonedaInfo(
    val id: Int,
    val idObjeto: Int,
    val familia: String,
    val idFamilia: Int,
    val variante: String,
    val ceca: String,
    val tipo: String,
    val disenador: String,
    val totalProducido: String,
    val valorSinCircular: String,
    val valorComercial: String,
    val valorAdquirido: String,
    val estado: String,
    val observaciones: String,
    val orden: Int,
    val acunada: String
)