package resa.rodriguez.config.security.password

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

/**
 * Clase de configuracion para la encriptacion de claves de usuario
 *
 */
@Configuration
class EncoderConfig {
    // BCrypt de Spring
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(12)
    }
}
