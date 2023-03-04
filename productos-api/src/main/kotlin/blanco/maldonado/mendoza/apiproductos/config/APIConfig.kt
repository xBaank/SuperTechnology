/**
 * @since 16/02/2023
 * @author Azahara Blanco, Alfredo Maldonado, Sebastian Mendoza
 */
package blanco.maldonado.mendoza.apiproductos.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class APIConfig {
    companion object {
        @Value("\${api.path}")
        const val API_PATH = "/api"

        @Value("\${pagination.init}")
        const val PAGINATION_INIT = "0"

        @Value("\${pagination.size}")
        const val PAGINATION_SIZE = "10"
    }
}