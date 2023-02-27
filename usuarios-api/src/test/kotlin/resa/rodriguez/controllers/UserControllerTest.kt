package resa.rodriguez.controllers

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import resa.rodriguez.config.security.jwt.JwtTokensUtils
import resa.rodriguez.dto.*
import resa.rodriguez.exceptions.UserExceptionNotFound
import resa.rodriguez.models.Address
import resa.rodriguez.models.User
import resa.rodriguez.repositories.address.AddressRepositoryCached
import resa.rodriguez.services.UserService
import java.time.LocalDate
import java.util.*

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest {
    private val dto = UserDTOcreate("test", "test@gmail.com", "1234567", "123456789", User.UserRole.ADMIN, setOf("calle test"), "", true)
    private val dtoRegister = UserDTOregister(dto.username, dto.email, dto.password, dto.password, dto.phone, dto.addresses)
    private val dtoLogin = UserDTOlogin(dto.username, dto.password)
    private val dtoUpdated = UserDTOUpdated(dto.password, dto.addresses)
    private val dtoRoleUpdated = UserDTORoleUpdated(dto.email, dto.role)
    private val entity = User(UUID.randomUUID(), dto.username, dto.email, dto.password, dto.phone, dto.avatar, dto.role, LocalDate.now(), dto.active)
    private val address = Address(UUID.randomUUID(), entity.id!!, dto.addresses.first())
    private val dtoMal1 = UserDTOcreate("", dto.email, dto.password, dto.phone, dto.role, dto.addresses, dto.avatar, dto.active)
    private val dtoMal2 = UserDTOcreate(dto.username, "", dto.password, dto.phone, dto.role, dto.addresses, dto.avatar, dto.active)
    private val dtoMal3 = UserDTOcreate(dto.username, "uwu", dto.password, dto.phone, dto.role, dto.addresses, dto.avatar, dto.active)
    private val dtoMal4 = UserDTOcreate(dto.username, dto.email, "", dto.phone, dto.role, dto.addresses, dto.avatar, dto.active)
    private val dtoMal5 = UserDTOcreate(dto.username, dto.email, dto.password, "", dto.role, dto.addresses, dto.avatar, dto.active)
    private val dtoMal6 = UserDTOcreate(dto.username, dto.email, dto.password, dto.phone, dto.role, setOf(), dto.avatar, dto.active)
    private val dtoMal7 = UserDTOcreate(dto.username, dto.email, dto.password, dto.phone, dto.role, setOf(""), dto.avatar, dto.active)
    private val dtoRegisterMal1 = UserDTOregister("", dto.email, dto.password, dto.password, dto.phone, dto.addresses)
    private val dtoRegisterMal2 = UserDTOregister(dto.username, "", dto.password, dto.password, dto.phone, dto.addresses)
    private val dtoRegisterMal3 = UserDTOregister(dto.username, "uwu", dto.password, dto.password, dto.phone, dto.addresses)
    private val dtoRegisterMal4 = UserDTOregister(dto.username, dto.email, "", dto.password, dto.phone, dto.addresses)
    private val dtoRegisterMal5 = UserDTOregister(dto.username, dto.email, "", "", dto.phone, dto.addresses)
    private val dtoRegisterMal6 = UserDTOregister(dto.username, dto.email, dto.password, dto.password, "", dto.addresses)
    private val dtoRegisterMal7 = UserDTOregister(dto.username, dto.email, dto.password, dto.password, dto.phone, setOf())
    private val dtoRegisterMal8 = UserDTOregister(dto.username, dto.email, dto.password, dto.password, dto.phone, setOf(""))
    private val dtoLoginMal1 = UserDTOlogin("", dto.password)
    private val dtoLoginMal2 = UserDTOlogin(dto.username, "")
    private val dtoUpdatedMal1 = UserDTOUpdated("", dto.addresses)
    private val dtoUpdatedMal2 = UserDTOUpdated(dto.password, setOf())
    private val dtoUpdatedMal3 = UserDTOUpdated(dto.password, setOf(""))
    private val dtoRoleUpdatedMal1 = UserDTORoleUpdated("", dto.role)
    private val dtoRoleUpdatedMal2 = UserDTORoleUpdated("uwu", dto.role)

    @MockK private lateinit var service: UserService
    @MockK private lateinit var aRepo: AddressRepositoryCached
    @MockK private lateinit var authenticationManager: AuthenticationManager
    @MockK private lateinit var jwtTokenUtils: JwtTokensUtils
    @InjectMockKs private lateinit var controller: UserController

    init { MockKAnnotations.init(this) }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findById() = runTest {
        coEvery { service.findById(any()) } returns entity
        coEvery { service.findAllFromUserId(any()) } returns flowOf(address)

        val result = controller.findByUserId(entity, entity.id!!)
        val res = result.body!!

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertNotNull(res) },
            { assertEquals(result.statusCode, HttpStatus.OK) },
            { assertEquals(dto.email, res.email) },
            { assertEquals(dto.username, res.username) },
            { assertEquals(dto.avatar, res.avatar) }
        )

        coVerify { service.findById(any()) }
        coVerify { service.findAllFromUserId(any()) }
    }
}