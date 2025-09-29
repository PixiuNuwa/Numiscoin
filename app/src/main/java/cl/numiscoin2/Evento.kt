package cl.numiscoin2

import java.util.Date

data class Evento(
    val idEvento: Int,
    val nombreEvento: String,
    val descripcion: String,
    val breveDescripcion: String,
    val foto: String,
    val fotoPoster: String,
    val fechaInicio: Date,
    val fechaFin: Date,
    val activo: Boolean,
    val fechaCreacion: Date
)