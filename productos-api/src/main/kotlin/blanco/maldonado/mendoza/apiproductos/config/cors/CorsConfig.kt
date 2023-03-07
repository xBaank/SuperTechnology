/**
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 * @since 5/3/2023
 */
package blanco.maldonado.mendoza.apiproductos.config.cors

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Cors config: Security configuration for the domains.
 */
@Configuration
class CorsConfig {
    @Bean
    fun corsConfigure(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:6963")
                    .allowedHeaders("*")
                    .allowedMethods("GET", "POST", "PUT", "DELETE")
                    .maxAge(3600)
            }
        }
    }
}