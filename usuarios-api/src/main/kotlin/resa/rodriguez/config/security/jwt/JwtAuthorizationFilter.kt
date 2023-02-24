package resa.rodriguez.config.security.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import resa.rodriguez.services.UserService
import java.io.IOException

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
        logger.info { "Filtrando" }
        val header = req.getHeader(AUTHORIZATION)

        if (header == null || !header.startsWith(JwtTokensUtils.TOKEN_PREFIX)) {
            chain.doFilter(req, res)
            return
        }
        getAuthentication(header.substring(7))?.also {
            SecurityContextHolder.getContext().authentication = it
            println(it)
        }
        chain.doFilter(req, res)
    }

    private fun getAuthentication(token: String): UsernamePasswordAuthenticationToken? = runBlocking {
        logger.info { "Obteniendo autenticación" }

        val tokenDecoded = jwtTokensUtils.decode(token) ?: return@runBlocking null

        val username = tokenDecoded.getClaim("username").toString().replace("\"", "")

        val user = service.loadUserByUsername(username)

        System.err.println(user)

        return@runBlocking UsernamePasswordAuthenticationToken(
            user,
            null,
            user.authorities
        )
    }
}