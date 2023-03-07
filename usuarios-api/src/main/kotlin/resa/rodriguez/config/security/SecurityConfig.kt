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
 * Configuration class for Spring Security.
 * @property service
 * @property jwtTokensUtils
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
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

            // For the JWT token.
            .authenticationManager(authenticationManager)

            // We don't use session states, therefore an Stateless policy.
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)

            .and()
            // We accept Http requests
            .authorizeHttpRequests()

            // This allows us to show the errors, instead of getting a code FORBIDDEN.
            .requestMatchers("/error/**").permitAll()

            // We allow swagger.
            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()

            // Now we open the /usuarios/** endpoint.
            .requestMatchers("/usuarios/**").permitAll()

            // And we allow the following endpoints without verification nor authentication.
            .requestMatchers("/usuarios", "/usuarios/", "/usuarios/login", "/usuarios/register").permitAll()

            // In order to access /me it will be necessary to be authenticated.
            .requestMatchers("/usuarios/me", "/usuarios/me/address", "/usuarios/me/avatar").authenticated()

            .requestMatchers(
                "/usuarios/create", "/usuarios/list", "/usuarios/list/paging",
                "/usuarios/list/{active}", "/usuarios/username/{username}",
                "/usuarios/id/{userId}", "/usuarios/email/{userEmail}",
                "/usuarios/phone/{userPhone}", "/usuarios/activity/{email}",
                "/usuarios/list/address", "/usuarios/list/address/paging",
                "/usuarios/list/address/{userId}",
                "/usuarios/address/{id}", "/usuarios/address"
            ).hasAnyRole("ADMIN", "SUPER_ADMIN")

            .requestMatchers(
                "/usuarios/role", "/usuarios/delete"
            ).hasRole("SUPER_ADMIN")
            
            .requestMatchers(
                HttpMethod.DELETE, "/usuarios/address", "/usuarios/storage/**"
            ).hasRole("SUPER_ADMIN")
            .requestMatchers(
                HttpMethod.POST, "/usuarios/storage"
            ).hasAnyRole("ADMIN", "SUPER_ADMIN")
            .requestMatchers(
                HttpMethod.GET, "/usuarios/storage/**"
            ).permitAll()

            // Any other request will need standard authentication.
            .anyRequest().authenticated()

            .and()

            // We add our authentication filter.
            .addFilter(JwtAuthenticationFilter(jwtTokensUtils, authenticationManager))
            // And lastly, our authorization filter.
            .addFilter(JwtAuthorizationFilter(jwtTokensUtils, service, authenticationManager))

        return http.build()
    }
}