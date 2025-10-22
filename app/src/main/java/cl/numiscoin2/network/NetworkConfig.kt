package cl.numiscoin2.network

object NetworkConfig {
    //url en servidor https://dev.osu.xecuoia.com:8445
    const val BASE_URL = "https://dev.osu.xecuoia.com:8445"
    const val UPLOADS_BASE_URL = "https://numiscoin.store/uploads/"

    fun construirUrlCompleta(urlRelativa: String): String {
        return if (urlRelativa.startsWith("http")) {
            urlRelativa
        } else {
            UPLOADS_BASE_URL + urlRelativa
        }
    }
}