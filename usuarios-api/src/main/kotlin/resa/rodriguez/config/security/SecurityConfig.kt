package resa.rodriguez.config.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import resa.rodriguez.config.security.jwt.JwtAuthenticationFilter
import resa.rodriguez.config.security.jwt.JwtAuthorizationFilter
import resa.rodriguez.config.security.jwt.JwtTokensUtils
import resa.rodriguez.services.UserService

/**
 * Clase de configuracion de Spring Security
 *
 * @property service
 * @property jwtTokensUtils
 */
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

            // No usamos estados de sesion
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)

            .and()
            // Aceptamos request del tipo Http
            .authorizeHttpRequests()

            // Spring desplaza a esta ruta todos los errores y excepciones, asi podemos mostrarlas en vez de un Forbidden
            .requestMatchers("/error/**").permitAll()

            // Permitimos el acceso de swagger
            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()

            // Abrimos el acceso a los end points, luego se limitaran segun el rol/acceso
            .requestMatchers("/usuarios/**").permitAll()

            // Permitimos el acceso sin autenticacion ni verificacion a las siguientes rutas
            .requestMatchers("/usuarios", "/usuarios/", "/usuarios/login", "/usuarios/register").permitAll()

            // Para acceder al /me se necesitara estar autenticado
            .requestMatchers("/usuarios/me", "/usuarios/me/address", "/usuarios/me/avatar").authenticated()

            .requestMatchers(
                "/usuarios/create", "/usuarios/list", "/usuarios/list/paging",
                "/usuarios/list/{active}", "/usuarios/username/{username}",
                "/usuarios/id/{userId}", "/usuarios/email/{userEmail}",
                "/usuarios/phone/{userPhone}", "/usuarios/activity/{email}",
                "/usuarios/list/address", "/list/address/paging",
                "/usuarios/list/address/{userId}",
                "/usuarios/address/{id}", "/usuarios/address"
            ).hasAnyRole("ADMIN", "SUPER_ADMIN")

            .requestMatchers(
                "/usuarios/role", "/usuarios/delete"
            ).hasRole("SUPER_ADMIN")
            .requestMatchers(
                HttpMethod.DELETE, "/usuarios/address"
            ).hasRole("SUPER_ADMIN")

            // El resto, se necesitara autenticacion estandar
            .anyRequest().authenticated()

            .and()

            .addFilter(JwtAuthenticationFilter(jwtTokensUtils, authenticationManager)) // Filtro de Autenticacion
            .addFilter(JwtAuthorizationFilter(jwtTokensUtils, service, authenticationManager)) // Filtro de Autorizacion

        return http.build()
    }
}