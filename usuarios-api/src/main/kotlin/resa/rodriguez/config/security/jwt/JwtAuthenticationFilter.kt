package resa.rodriguez.config.security.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import resa.rodriguez.dto.UserDTOlogin
import resa.rodriguez.models.User
import java.util.*

/**
 * Class for creating an authentication filter for Spring Security.
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 * @property jwtTokenUtil
 * @property authenticationManager
 */
class JwtAuthenticationFilter(
    private val jwtTokenUtil: JwtTokensUtils,
    private val authenticationManager: AuthenticationManager
) : UsernamePasswordAuthenticationFilter() {

    @OptIn(ExperimentalSerializationApi::class)
    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
        val credentials = Json.decodeFromStream<UserDTOlogin>(request.inputStream)

        val auth = UsernamePasswordAuthenticationToken(
            credentials.username,
            credentials.password
        )
        return authenticationManager.authenticate(auth)
    }

    override fun successfulAuthentication(
        request: HttpServletRequest?,
        response: HttpServletResponse,
        chain: FilterChain?,
        authResult: Authentication
    ) {
        val user = authResult.principal as User

        val token: String = jwtTokenUtil.create(user)

        response.addHeader("Authorization", token)
        response.addHeader("Access-Control-Expose-Headers", JwtTokensUtils.TOKEN_HEADER)
    }

    override fun unsuccessfulAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse,
        failed: AuthenticationException
    ) {
        val error = BadCredentialsError()
        response.status = error.status
        response.contentType = "application/json"
        response.writer.append(error.toString())
    }

    private data class BadCredentialsError(
        val timestamp: Long = Date().time,
        val status: Int = 401,
        val message: String = "Error: Incorrect user or password."
    )
}