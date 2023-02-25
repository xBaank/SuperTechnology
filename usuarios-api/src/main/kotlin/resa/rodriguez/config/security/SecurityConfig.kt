package resa.rodriguez.config.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import resa.rodriguez.config.security.jwt.JwtAuthenticationFilter
import resa.rodriguez.config.security.jwt.JwtAuthorizationFilter
import resa.rodriguez.config.security.jwt.JwtTokensUtils
import resa.rodriguez.services.UserService

@Configuration
//@EnableWebSecurity
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

            // Permitimos el acceso sin autenticacion ni verificacion a las siguientes rutas
            .requestMatchers("/usuarios", "/usuarios/", "/usuarios/login", "/usuarios/register").permitAll()

            .requestMatchers(HttpMethod.GET,"/usuarios/list").hasRole("ADMIN")

            .requestMatchers(
                "/usuarios/create", "/usuarios/list/paging",
                "/usuarios/list/activity/{active}", "/usuarios/username/{username}",
                "/usuarios/id/{userId}", "/usuarios/email/{userEmail}",
                "/usuarios/phone/{userPhone}", "/usuarios/activity/{email}",
                "/usuarios/list/address", "/usuarios/list/address/user/{userId}",
                "/usuarios/address/{id}", "/usuarios/address/{name}"
            ).hasAnyRole("ADMIN", "SUPER_ADMIN")

            .requestMatchers(
                "/usuarios/role/{email}", "/usuarios/delete/{email}"
            ).hasRole("SUPER_ADMIN")

            // El resto, se necesitara autenticacion estandar
            .anyRequest().authenticated()

            .and()

            .addFilter(JwtAuthenticationFilter(jwtTokensUtils, authenticationManager)) // Autenticacion
            .addFilter(JwtAuthorizationFilter(jwtTokensUtils, service, authenticationManager)) // Autorizacion

        return http.build()
    }
}