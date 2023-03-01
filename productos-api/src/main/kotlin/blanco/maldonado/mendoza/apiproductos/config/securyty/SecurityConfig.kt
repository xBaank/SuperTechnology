package blanco.maldonado.mendoza.apiproductos.config.securyty

import blanco.maldonado.mendoza.apiproductos.config.securyty.jwt.JwtAuthorizationFilter
import blanco.maldonado.mendoza.apiproductos.config.securyty.jwt.JwtTokensUtils
import blanco.maldonado.mendoza.apiproductos.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
class SecurityConfig
@Autowired constructor(
    private val service: UserService,
    private val jwtTokensUtils: JwtTokensUtils
) {

    @Bean
    fun authManager(http: HttpSecurity): AuthenticationManager {
        val authenticationManagerBuilder = http.getSharedObject(
            AuthenticationManagerBuilder::class.java
        )
        authenticationManagerBuilder.userDetailsService(service)
        return authenticationManagerBuilder.build()
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        val authenticationManager = authManager(http)

        http
            .csrf()
            .disable()
            .exceptionHandling()
            .and()

            // Para token JWT
            .authenticationManager(authenticationManager)

            // No usamos estado de sesion
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)

            .and()
            // Aceptamos request del tipo Http
            .authorizeHttpRequests()

            // Spring desplaza a esta ruta todos los errores y excepciones, asi podemos mostrarlas en vez de un Forbidden
            .requestMatchers("/error/**").permitAll()

            .requestMatchers("/**").permitAll()

            //todo rutas protegidas por rol
            .requestMatchers(
                "/usuarios/role/{email}", "/usuarios/delete/{email}"
            ).hasRole("SUPER_ADMIN")
            //todo para varios cambiar has rol por hasanyrol

            // El resto, se necesitara autenticacion estandar
            .anyRequest().authenticated()

            .and()

            .addFilter(JwtAuthorizationFilter(jwtTokensUtils, authenticationManager, service)) // Autorizacion

        return http.build()
    }
}
