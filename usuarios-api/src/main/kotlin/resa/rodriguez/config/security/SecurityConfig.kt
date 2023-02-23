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
            .authorizeHttpRequests()

            .requestMatchers("/usuarios/**")
            .permitAll()

            .requestMatchers("", "/", "/login", "/register").permitAll()

            .anyRequest().authenticated()

        return http.build()
    }
}