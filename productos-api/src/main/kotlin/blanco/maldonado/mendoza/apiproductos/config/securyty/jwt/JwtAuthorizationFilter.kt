package blanco.maldonado.mendoza.apiproductos.config.securyty.jwt

import blanco.maldonado.mendoza.apiproductos.mapper.toModel
import blanco.maldonado.mendoza.apiproductos.model.Producto
import blanco.maldonado.mendoza.apiproductos.service.UserService
import blanco.maldonado.mendoza.apiproductos.user.User
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
import org.springframework.stereotype.Service
import java.io.IOException
import java.util.*


class JwtAuthorizationFilter(
    private val jwtTokensUtils: JwtTokensUtils,
    authManager: AuthenticationManager,
    private  val service : UserService
    ) : BasicAuthenticationFilter(authManager) {

    @Throws(IOException::class, ServletException::class)
    override fun doFilterInternal(
        req: HttpServletRequest,
        res: HttpServletResponse,
        chain: FilterChain
    ) {
        logger.info { "Filtrando" }
        val header = req.getHeader(AUTHORIZATION.toString())

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

        val user = service.loadUserByUsername(token)?:return@runBlocking null
        System.err.println(user)

        return@runBlocking UsernamePasswordAuthenticationToken(
            user,
            null,
            user.authorities
        )
    }
}
