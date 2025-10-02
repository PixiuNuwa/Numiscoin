package cl.numiscoin2

object NetworkConfig {
    const val BASE_URL = "https://f70ba7db6da1.ngrok-free.app"
    const val UPLOADS_BASE_URL = "https://numiscoin.store/uploads/"

    fun construirUrlCompleta(urlRelativa: String): String {
        return if (urlRelativa.startsWith("http")) {
            urlRelativa
        } else {
            UPLOADS_BASE_URL + urlRelativa
        }
    }
}