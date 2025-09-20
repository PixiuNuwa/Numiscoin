package cl.numiscoin2

object SessionManager {
    var usuario: Usuario? = null
    var isLoggedIn: Boolean = false

    fun login(usuario: Usuario) {
        this.usuario = usuario
        this.isLoggedIn = true
    }

    fun logout() {
        this.usuario = null
        this.isLoggedIn = false
    }

    fun getUsuarioId(): Long {
        return usuario?.idUsuario ?: -1
    }
}