package resa.rodriguez.config.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
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

    /*@Bean
    fun authManager(http: HttpSecurity): AuthenticationManager {
        val authenticationManagerBuilder = http.getSharedObject(
            AuthenticationManagerBuilder::class.java
        )
        authenticationManagerBuilder.userDetailsService()
        return authenticationManagerBuilder.build()
    }*/

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf().disable().exceptionHandling().and()
            // Aceptamos request del tipo Http
            .authorizeHttpRequests()

            // Spring desplaza a esta ruta todos los errores y excepciones, asi podemos mostrarlas en vez de un Forbidden
            .requestMatchers("/error/**").permitAll()

            // Permitimos el acceso a las rutas que comiencen por /usuarios, aunque no quita que se siga habiendo seguridad
            .requestMatchers("/usuarios/**")
            .permitAll()

            // Permitimos el acceso sin autenticacion ni verificacion a las siguientes rutas
            .requestMatchers("", "/", "/login", "/register").permitAll()

            // El resto, se necesitara autenticacion estandar
            .anyRequest().authenticated()

        return http.build()
    }
}