package cl.numiscoin2

object NetworkConfig {
    const val BASE_URL = "https://ad407004253a.ngrok-free.app"
    const val UPLOADS_BASE_URL = "https://numiscoin.store/uploads/"

    fun construirUrlCompleta(urlRelativa: String): String {
        return if (urlRelativa.startsWith("http")) {
            urlRelativa
        } else {
            UPLOADS_BASE_URL + urlRelativa
        }
    }
}