package resa.rodriguez.config

import org.springframework.context.annotation.Configuration

/**
 * Configuration class for specifying the API path, version and default values for pagination.
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 */
@Configuration
class APIConfig {
    companion object {
        const val API_PATH = "/usuarios"

        const val API_VERSION = "1.0.0"

        const val PAGINATION_INIT = "0"

        const val PAGINATION_SIZE = "10"

        const val PAGINATION_SORT = "id"
    }
}