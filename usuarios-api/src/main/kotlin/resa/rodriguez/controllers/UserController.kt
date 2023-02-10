package resa.rodriguez.controllers

import kotlinx.coroutines.flow.toList
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import resa.rodriguez.config.APIConfig
import resa.rodriguez.models.User
import resa.rodriguez.repositories.user.UserRepositoryCached

private val log = KotlinLogging.logger {}

/**
 * Controlador rest para realizar operaciones CRUD con los usuarios
 *
 */
@RestController
@RequestMapping(APIConfig.API_PATH + "/users")
class UserController
@Autowired constructor(
    private val userRepositoryCached: UserRepositoryCached
) {

    // TODO: Limitarlo a rol de ADMIN/SUPER_ADMIN // Aplicar un DTO
    @GetMapping("/list")
    suspend fun listUsers(): List<User> {
        log.info { "Obteniendo listado de usuarios" }

        return userRepositoryCached.findAll().toList()
    }
}