package blanco.maldonado.mendoza.apiproductos.config.security.jwt

import blanco.maldonado.mendoza.apiproductos.service.UserService
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import java.io.IOException

private val log = KotlinLogging.logger { }

class JwtAuthorizationFilter(
    private val jwtTokensUtils: JwtTokensUtils,
    authManager: AuthenticationManager,
    private val service: UserService
) : BasicAuthenticationFilter(authManager) {

    @Throws(IOException::class, ServletException::class)
    override fun doFilterInternal(
        req: HttpServletRequest,
        res: HttpServletResponse,
        chain: FilterChain
    ) {
        log.info { "Filtrando" }
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
        log.info { "Obteniendo autenticación" }
        val user = service.loadUserByUsername(token) ?: return@runBlocking null
        System.err.println(user)

        return@runBlocking UsernamePasswordAuthenticationToken(
            user,
            null,
            user.authorities
        )
    }
}
