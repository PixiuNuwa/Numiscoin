package cl.numiscoin2.network

object NetworkConfig {
    //url en servidor ae77ded235a2
    const val BASE_URL = "https://ae77ded235a2.ngrok-free.app"
    const val UPLOADS_BASE_URL = "https://numiscoin.store/uploads/"

    fun construirUrlCompleta(urlRelativa: String): String {
        return if (urlRelativa.startsWith("http")) {
            urlRelativa
        } else {
            UPLOADS_BASE_URL + urlRelativa
        }
    }
}