package resa.rodriguez.config

import org.springframework.context.annotation.Configuration

/**
 * Clase de configuracion que se encarga de configurar la ruta principal de la API, ademas de marcar la version
 */
@Configuration
class APIConfig {
    companion object {
        const val API_PATH = "/usuarios"

        const val API_VERSION = "1.0"

        const val PAGINATION_INIT = "0"

        const val PAGINATION_SIZE = "10"

        const val PAGINATION_SORT = "id"
    }
}