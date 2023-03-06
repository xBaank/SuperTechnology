package blanco.maldonado.mendoza.apiproductos.service

import blanco.maldonado.mendoza.apiproductos.config.security.jwt.JwtTokensUtils
import blanco.maldonado.mendoza.apiproductos.user.User
import kotlinx.coroutines.runBlocking
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import java.util.*


@Service
    class UserService : UserDetailsService {
        override fun loadUserByUsername(token: String): UserDetails? = runBlocking {
            val tokenDecoded = JwtTokensUtils().decode(token) ?: return@runBlocking null

            val id = tokenDecoded.subject.toString().replace("\"", "")
            val username = tokenDecoded.getClaim("username").toString().replace("\"", "")
            val email = tokenDecoded.getClaim("email").toString().replace("\"", "")
            val password = tokenDecoded.getClaim("password").toString().replace("\"", "")
            val role = tokenDecoded.getClaim("role").toString().replace("\"", "")
            val active = tokenDecoded.getClaim("active").toString().replace("\"", "")

            User(UUID.fromString(id), username, email, password, User.UserRole.valueOf(role), active.toBoolean())
        }
    }