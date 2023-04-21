package blanco.maldonado.mendoza.apiproductos.config.security

/**
 * @since 1/3/2023
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 */
import blanco.maldonado.mendoza.apiproductos.config.security.jwt.JwtAuthorizationFilter
import blanco.maldonado.mendoza.apiproductos.config.security.jwt.JwtTokensUtils
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

/**
 * Security config
 *
 * @property service
 * @property jwtTokensUtils
 * @constructor Create empty Security config
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
class SecurityConfig
@Autowired constructor(
    private val service: UserService,
    private val jwtTokensUtils: JwtTokensUtils
) {

    /**
     * Auth manager: ensures that only authorized users have access to the information and features they need to do their jobs.
     *
     * @param http
     * @return authenticationManagerBuilder
     */
    @Bean
    fun authManager(http: HttpSecurity): AuthenticationManager {
        val authenticationManagerBuilder = http.getSharedObject(
            AuthenticationManagerBuilder::class.java
        )
        authenticationManagerBuilder.userDetailsService(service)
        return authenticationManagerBuilder.build()
    }

    /**
     * Filter chain: Return the filter chain of security
     *
     * @param http
     * @return
     */
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        val authenticationManager = authManager(http)

        http
            .csrf()
            .disable()
            .exceptionHandling()
            .and()
            .authenticationManager(authenticationManager)
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests()
            .requestMatchers("/error/**").permitAll()
            .requestMatchers("/api/**").permitAll()
            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
            .requestMatchers(
                HttpMethod.GET,
                "/products",
                "/products/{id}",
                "/products/**",
                "/products/category/{categoria}",
                "/products/category/**",
                "/products/nombre/{nombre}",
                "/products/nombre/**"
            ).permitAll()
            .requestMatchers(
                HttpMethod.GET,
                "/products/admin",
                "/products/admin/{id}",
                "/products/admin/category/{categoria}",
                "/products/admin/name/{nombre}",
                "/products/admin/category/**",
                "/products/admin/name/**",
                "/products/admin/paging"
            ).hasAnyRole("SUPER_ADMIN", "ADMIN")
            .requestMatchers(HttpMethod.POST, "/products").hasRole("SUPER_ADMIN")
            .requestMatchers(HttpMethod.PUT, "/products/{id}").hasAnyRole("SUPER_ADMIN", "ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/products/{id}").hasRole("SUPER_ADMIN")
            .anyRequest().authenticated()
            .and()
            .addFilter(JwtAuthorizationFilter(jwtTokensUtils, authenticationManager, service))

        return http.build()
    }
}
