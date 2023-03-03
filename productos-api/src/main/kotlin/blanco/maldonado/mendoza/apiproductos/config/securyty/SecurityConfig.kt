package blanco.maldonado.mendoza.apiproductos.config.securyty

import blanco.maldonado.mendoza.apiproductos.config.securyty.jwt.JwtAuthorizationFilter
import blanco.maldonado.mendoza.apiproductos.config.securyty.jwt.JwtTokensUtils
import blanco.maldonado.mendoza.apiproductos.service.UserService
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
            .requestMatchers("/api/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/productos", "/productos/{id}", "/productos/**", "/productos/categoria/{categoria}", "/productos/categoria/**", "/productos/nombre/{nombre}","/productos/nombre/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/productos").hasRole("SUPER_ADMIN")
            .requestMatchers(HttpMethod.PUT, "/productos/{id}").hasAnyRole("SUPER_ADMIN", "ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/productos/{id}").hasRole("SUPER_ADMIN")
            .anyRequest().authenticated()
            .and()
            .addFilter(JwtAuthorizationFilter(jwtTokensUtils, authenticationManager, service)) // Autorizacion

        return http.build()
    }
}
