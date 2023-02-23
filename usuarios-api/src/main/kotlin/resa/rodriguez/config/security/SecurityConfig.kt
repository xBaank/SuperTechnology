package resa.rodriguez.config.security

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
import resa.rodriguez.config.security.jwt.JwtAuthenticationFilter
import resa.rodriguez.config.security.jwt.JwtTokensUtils
import resa.rodriguez.controllers.UserController

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
class SecurityConfig
@Autowired constructor(
    private val userController: UserController,
    private val jwtTokensUtils: JwtTokensUtils
) {

    @Bean
    fun authManager(http: HttpSecurity): AuthenticationManager {
        val authenticationManagerBuilder = http.getSharedObject(
            AuthenticationManagerBuilder::class.java
        )
        authenticationManagerBuilder.userDetailsService(userController)
        return authenticationManagerBuilder.build()
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        val authenticationManager = authManager(http)

        http.csrf().disable().exceptionHandling().and()

            // Para token JWT
            .authenticationManager(authenticationManager)

            // No usamos estado de sesion
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)

            .and()
            // Aceptamos request del tipo Http
            .authorizeHttpRequests()

            // Spring desplaza a esta ruta todos los errores y excepciones, asi podemos mostrarlas en vez de un Forbidden
            .requestMatchers("/error/**").permitAll()

            // Permitimos el acceso sin autenticacion ni verificacion a las siguientes rutas
            .requestMatchers("/usuarios", "/usuarios/", "/usuarios/login", "/usuarios/register").permitAll()

            // El resto, se necesitara autenticacion estandar
            .anyRequest().authenticated()

            .and()

            .addFilter(JwtAuthenticationFilter(jwtTokensUtils, authenticationManager))

        return http.build()
    }
}