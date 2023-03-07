package resa.rodriguez.config.security.jwt

import io.netty.handler.codec.http.HttpHeaderNames.AUTHORIZATION
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import resa.rodriguez.services.UserService
import java.io.IOException

private val log = KotlinLogging.logger {}

/**
 * Class for creating an authorization filter for Spring Security.
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 * @property jwtTokensUtils
 * @property service
 * @param authManager
 */
class JwtAuthorizationFilter(
    private val jwtTokensUtils: JwtTokensUtils,
    private val service: UserService,
    authManager: AuthenticationManager
) : BasicAuthenticationFilter(authManager) {

    @Throws(IOException::class, ServletException::class)
    override fun doFilterInternal(
        req: HttpServletRequest,
        res: HttpServletResponse,
        chain: FilterChain
    ) {
        log.info { "Filtrando" }
        val header = req.getHeader(AUTHORIZATION.toString())

        if (header == null || !header.startsWith(JwtTokensUtils.TOKEN_PREFIX)) {
            chain.doFilter(req, res)
            return
        }
        getAuthentication(header.substring(7))?.also {
            SecurityContextHolder.getContext().authentication = it
        }
        chain.doFilter(req, res)
    }

    private fun getAuthentication(token: String): UsernamePasswordAuthenticationToken? = runBlocking {
        log.info { "Obteniendo autenticación" }

        val tokenDecoded = jwtTokensUtils.decode(token) ?: return@runBlocking null

        val username = tokenDecoded.getClaim("username").toString().replace("\"", "")

        val user = service.loadUserByUsername(username)

        return@runBlocking UsernamePasswordAuthenticationToken(
            user,
            null,
            user.authorities
        )
    }
}