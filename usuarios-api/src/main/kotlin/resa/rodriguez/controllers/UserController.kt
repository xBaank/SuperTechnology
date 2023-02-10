package resa.rodriguez.controllers

import kotlinx.coroutines.flow.toList
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import resa.rodriguez.config.APIConfig
import resa.rodriguez.dto.UserDTOresponse
import resa.rodriguez.mappers.UserMapper
import resa.rodriguez.models.Address
import resa.rodriguez.repositories.address.AddressRepositoryCached
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
    private val userMapper: UserMapper,
    private val userRepositoryCached: UserRepositoryCached,
    private val addressRepositoryCached: AddressRepositoryCached
) {

    // TODO: Limitarlo a rol de ADMIN/SUPER_ADMIN
    @GetMapping("/list")
    suspend fun listUsers(): ResponseEntity<List<UserDTOresponse>> {
        log.info { "Obteniendo listado de usuarios" }

        val res = userRepositoryCached.findAll().toList()

        return ResponseEntity.ok(userMapper.toDTO(res))
    }

    /*    @PostMapping("/register")
        suspend fun register(@Valid @RequestBody userDto: UserDTOregister): ResponseEntity<UserDTOresponse> {
            log.info { "Registro de usuario: ${userDto.username}" }

            try {
                val user = userDto.fromDTOtoUser()

                val userSaved = userRepositoryCached.save(user!!)
                // TODO: Implementacion de token
                return ResponseEntity.ok(userMapper.toDTO(userSaved))
            } catch (e: Exception) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
            }
        }*/

    // TODO: Limitarlo a rol de ADMIN/SUPER_ADMIN // Aplicar un posible DTO
    @GetMapping("/list/address")
    suspend fun listAddresses(): ResponseEntity<List<Address>> {
        log.info { "Obteniendo listado de direcciones" }

        return ResponseEntity.ok(addressRepositoryCached.findAll().toList())
    }
}