package resa.rodriguez.config.security.jwt

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import resa.rodriguez.dto.UserDTOlogin

class JwtAuthenticationFilter(
    private val jwtTokenUtil: JwtTokensUtils,
    private val authenticationManager: AuthenticationManager
) : UsernamePasswordAuthenticationFilter() {

    @OptIn(ExperimentalSerializationApi::class)
    override fun attemptAuthentication(request: HttpServletRequest?, response: HttpServletResponse?): Authentication {
        val credentials = request?.inputStream?.let { Json.decodeFromStream<UserDTOlogin>(it) }
        TODO()
    }
}