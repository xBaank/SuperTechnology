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
import resa.rodriguez.exceptions.AddressExceptionBadRequest
import resa.rodriguez.exceptions.AddressExceptionNotFound
import resa.rodriguez.exceptions.UserExceptionBadRequest
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
    private val dtoUpdatedMal2 = UserDTOUpdated(dto.password, setOf(""))
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
    fun bienvenida() = runTest {
        val result = controller.bienvenida()
        val res = result.body

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertNotNull(res) },
            { assertEquals(result.statusCode, HttpStatus.OK) },
            { assertEquals(
                "Microservicio de gestión de usuarios de una tienda de tecnología para las asignaturas de Acceso a Datos y " +
                "Programación de Procesos y Servicios del IES Luis Vives (Leganés) curso 22/23.",
                res) }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun register() = runTest {
        coEvery { service.register(any()) } returns entity
        coEvery { aRepo.findAllFromUserId(any()) } returns flowOf(address)
        coEvery { jwtTokenUtils.create(any()) } returns "token"

        val result = controller.register(dtoRegister)
        val res = result.body

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertNotNull(res) },
            { assertEquals(result.statusCode, HttpStatus.OK) },
            { assertEquals(dto.email, res?.user?.email) },
            { assertEquals(dto.username, res?.user?.username) },
            { assertEquals("token", res?.token) }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun registerMal1() = runTest {
        val result = assertThrows<UserExceptionBadRequest> {
            controller.register(dtoRegisterMal1)
        }
        assertEquals("Username cannot be blank.", result.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun registerMal2() = runTest {
        val result = assertThrows<UserExceptionBadRequest> {
            controller.register(dtoRegisterMal2)
        }
        assertEquals("Email cannot be blank.", result.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun registerMal3() = runTest {
        val result = assertThrows<UserExceptionBadRequest> {
            controller.register(dtoRegisterMal3)
        }
        assertEquals("Invalid email.", result.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun registerMal4() = runTest {
        val result = assertThrows<UserExceptionBadRequest> {
            controller.register(dtoRegisterMal4)
        }
        assertEquals("Passwords do not match.", result.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun registerMal5() = runTest {
        val result = assertThrows<UserExceptionBadRequest> {
            controller.register(dtoRegisterMal5)
        }
        assertEquals("Password must at least be 7 characters long.", result.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun registerMal6() = runTest {
        val result = assertThrows<UserExceptionBadRequest> {
            controller.register(dtoRegisterMal6)
        }
        assertEquals("Phone must at least be 9 characters long.", result.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun registerMal7() = runTest {
        val result = assertThrows<UserExceptionBadRequest> {
            controller.register(dtoRegisterMal7)
        }
        assertEquals("Must at least have one address.", result.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun registerMal8() = runTest {
        val result = assertThrows<UserExceptionBadRequest> {
            controller.register(dtoRegisterMal8)
        }
        assertEquals("Address cannot be blank.", result.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun registerMal9() = runTest {
        coEvery { service.register(any()) } throws UserExceptionBadRequest("Password and repeated password does not match.")
        val result = assertThrows<UserExceptionBadRequest> {
            controller.register(dtoRegister)
        }
        assertEquals("Password and repeated password does not match.", result.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun create() = runTest {
        coEvery { service.create(any()) } returns entity
        coEvery { aRepo.findAllFromUserId(any()) } returns flowOf(address)
        coEvery { jwtTokenUtils.create(any()) } returns "token"

        val result = controller.create(dto)
        val res = result.body

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertNotNull(res) },
            { assertEquals(result.statusCode, HttpStatus.CREATED) },
            { assertEquals(dto.email, res?.user?.email) },
            { assertEquals(dto.username, res?.user?.username) },
            { assertEquals("token", res?.token) }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun createMal1() = runTest {
        val result = assertThrows<UserExceptionBadRequest> {
            controller.create(dtoMal1)
        }
        assertEquals("Username cannot be blank.", result.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun createMal2() = runTest {
        val result = assertThrows<UserExceptionBadRequest> {
            controller.create(dtoMal2)
        }
        assertEquals("Email cannot be blank.", result.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun createMal3() = runTest {
        val result = assertThrows<UserExceptionBadRequest> {
            controller.create(dtoMal3)
        }
        assertEquals("Invalid email.", result.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun createMal4() = runTest {
        val result = assertThrows<UserExceptionBadRequest> {
            controller.create(dtoMal4)
        }
        assertEquals("Password must at least be 7 characters long.", result.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun createMal5() = runTest {
        val result = assertThrows<UserExceptionBadRequest> {
            controller.create(dtoMal5)
        }
        assertEquals("Phone must at least be 9 characters long.", result.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun createMal6() = runTest {
        val result = assertThrows<UserExceptionBadRequest> {
            controller.create(dtoMal6)
        }
        assertEquals("Must at least have one address.", result.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun createMal7() = runTest {
        val result = assertThrows<UserExceptionBadRequest> {
            controller.create(dtoMal7)
        }
        assertEquals("Address cannot be blank.", result.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun createByAdmin() = runTest {
        coEvery { service.create(any()) } returns entity
        coEvery { service.findAllFromUserId(any()) } returns flowOf(address)
        coEvery { jwtTokenUtils.create(any()) } returns "token"

        val result = controller.createByAdminInitializer(dto)
        val res = result.body

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertNotNull(res) },
            { assertEquals(result.statusCode, HttpStatus.CREATED) },
            { assertEquals(dto.email, res?.user?.email) },
            { assertEquals(dto.username, res?.user?.username) },
            { assertEquals("token", res?.token) }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun loginMal1() = runTest {
        val result = assertThrows<UserExceptionBadRequest> {
            controller.login(dtoLoginMal1)
        }
        assertEquals("Username cannot be blank.", result.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun loginMal2() = runTest {
        val result = assertThrows<UserExceptionBadRequest> {
            controller.login(dtoLoginMal2)
        }
        assertEquals("Password must at least be 7 characters long.", result.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun listUsers() = runTest {
        coEvery { service.listUsers() } returns listOf(entity)
        coEvery { aRepo.findAllFromUserId(any()) } returns flowOf(address)

        val result = controller.listUsers(entity)
        val res = result.body!!

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertNotNull(res) },
            { assertEquals(result.statusCode, HttpStatus.OK) },
            { assertEquals(dto.email, res[0].email) },
            { assertEquals(dto.username, res[0].username) },
            { assertEquals(dto.avatar, res[0].avatar) }
        )

        coVerify { service.listUsers() }
        coVerify { aRepo.findAllFromUserId(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun listUsersNF() = runTest {
        coEvery { service.listUsers() } returns listOf()
        coEvery { service.findAllFromUserId(any()) } returns flowOf()

        val result = controller.listUsers(entity)
        val res = result.body!!

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertNotNull(res) },
            { assertEquals(result.statusCode, HttpStatus.OK) },
            { assertEquals(0, res.size) }
        )

        coVerify { service.listUsers() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun listUsersActive() = runTest {
        coEvery { service.findAllByActive(any()) } returns listOf(entity)
        coEvery { service.findAllFromUserId(any()) } returns flowOf(address)

        val result = controller.listUsersActive(true, entity)
        val res = result.body!!

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertNotNull(res) },
            { assertEquals(result.statusCode, HttpStatus.OK) },
            { assertEquals(dto.email, res[0].email) },
            { assertEquals(dto.username, res[0].username) },
            { assertEquals(dto.avatar, res[0].avatar) }
        )

        coVerify { service.findAllByActive(any()) }
        coVerify { service.findAllFromUserId(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun listUsersActiveNF() = runTest {
        coEvery { service.findAllByActive(any()) } returns listOf()
        coEvery { service.findAllFromUserId(any()) } returns flowOf()

        val result = controller.listUsersActive(true, entity)
        val res = result.body!!

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertNotNull(res) },
            { assertEquals(result.statusCode, HttpStatus.OK) },
            { assertEquals(0, res.size) }
        )

        coVerify { service.findAllByActive(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findByUsername() = runTest {
        coEvery { service.findByUsername(any()) } returns entity
        coEvery { service.findAllFromUserId(any()) } returns flowOf(address)

        val result = controller.findByUsername(entity, entity.username)
        val res = result.body!!

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertNotNull(res) },
            { assertEquals(result.statusCode, HttpStatus.OK) },
            { assertEquals(dto.email, res.email) },
            { assertEquals(dto.username, res.username) },
            { assertEquals(dto.avatar, res.avatar) }
        )

        coVerify { service.findByUsername(any()) }
        coVerify { service.findAllFromUserId(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findByUsernameNF() = runTest {
        coEvery { service.findByUsername(any()) } throws
                UserExceptionNotFound("User with username ${entity.username} not found.")

        val result = assertThrows<UserExceptionNotFound> {
            controller.findByUsername(entity, entity.username)
        }

        assertEquals("User with username ${entity.username} not found.", result.message)

        coVerify { service.findByUsername(any()) }
    }

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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findByIdNF() = runTest {
        coEvery { service.findById(any()) } throws
            UserExceptionNotFound("User with id ${entity.id} not found.")

        val result = assertThrows<UserExceptionNotFound> {
            controller.findByUserId(entity, entity.id!!)
        }

        assertEquals("User with id ${entity.id} not found.", result.message)

        coVerify { service.findById(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findByEmail() = runTest {
        coEvery { service.findByEmail(any()) } returns entity
        coEvery { service.findAllFromUserId(any()) } returns flowOf(address)

        val result = controller.findByUserEmail(entity, entity.email)
        val res = result.body!!

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertNotNull(res) },
            { assertEquals(result.statusCode, HttpStatus.OK) },
            { assertEquals(dto.email, res.email) },
            { assertEquals(dto.username, res.username) },
            { assertEquals(dto.avatar, res.avatar) }
        )

        coVerify { service.findByEmail(any()) }
        coVerify { service.findAllFromUserId(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findByEmailNF() = runTest {
        coEvery { service.findByEmail(any()) } throws
                UserExceptionNotFound("User with email ${entity.email} not found.")

        val result = assertThrows<UserExceptionNotFound> {
            controller.findByUserEmail(entity, entity.email)
        }

        assertEquals("User with email ${entity.email} not found.", result.message)

        coVerify { service.findByEmail(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findByPhone() = runTest {
        coEvery { service.findByUserPhone(any()) } returns entity
        coEvery { service.findAllFromUserId(any()) } returns flowOf(address)

        val result = controller.findByUserPhone(entity, entity.phone)
        val res = result.body!!

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertNotNull(res) },
            { assertEquals(result.statusCode, HttpStatus.OK) },
            { assertEquals(dto.email, res.email) },
            { assertEquals(dto.username, res.username) },
            { assertEquals(dto.avatar, res.avatar) }
        )

        coVerify { service.findByUserPhone(any()) }
        coVerify { service.findAllFromUserId(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findByPhoneNF() = runTest {
        coEvery { service.findByUserPhone(any()) } throws
                UserExceptionNotFound("User with phone ${entity.phone} not found.")

        val result = assertThrows<UserExceptionNotFound> {
            controller.findByUserPhone(entity, entity.phone)
        }

        assertEquals("User with phone ${entity.phone} not found.", result.message)

        coVerify { service.findByUserPhone(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun updateMyself() = runTest {
        coEvery { service.updateMySelf(any(), any()) } returns entity
        coEvery { service.findAllFromUserId(any()) } returns flowOf(address)

        val result = controller.updateMySelf(entity, dtoUpdated)
        val res = result.body!!

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertNotNull(res) },
            { assertEquals(result.statusCode, HttpStatus.OK) },
            { assertEquals(dto.email, res.email) },
            { assertEquals(dto.username, res.username) },
            { assertEquals(dto.avatar, res.avatar) }
        )

        coVerify { service.updateMySelf(any(), any()) }
        coVerify { service.findAllFromUserId(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun updateMyselfMal1() = runTest {
        val result = assertThrows<UserExceptionBadRequest> {
            controller.updateMySelf(entity, dtoUpdatedMal1)
        }
        assertEquals("Password must at least be 7 characters long.", result.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun updateMyselfMal2() = runTest {
        val result = assertThrows<UserExceptionBadRequest> {
            controller.updateMySelf(entity, dtoUpdatedMal2)
        }
        assertEquals("Address cannot be blank.", result.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun switchActivityByEmail() = runTest {
        coEvery { service.switchActivity(any()) } returns entity
        coEvery { service.findAllFromUserId(any()) } returns flowOf(address)

        val result = controller.switchActivityByEmail(entity.email, entity)
        val res = result.body!!

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertNotNull(res) },
            { assertEquals(result.statusCode, HttpStatus.OK) },
            { assertEquals(dto.email, res.email) },
            { assertEquals(dto.username, res.username) },
            { assertEquals(dto.avatar, res.avatar) }
        )

        coVerify { service.switchActivity(any()) }
        coVerify { service.findAllFromUserId(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun switchActivityByEmailNF() = runTest {
        coEvery { service.switchActivity(any()) } throws
                UserExceptionNotFound("User with email: ${entity.email} not found.")

        val result = assertThrows<UserExceptionNotFound> {
            controller.switchActivityByEmail(entity.email, entity)
        }

        assertEquals("User with email: ${entity.email} not found.", result.message)

        coVerify { service.switchActivity(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun updateRoleByEmail() = runTest {
        coEvery { service.updateRoleByEmail(any()) } returns entity
        coEvery { service.findAllFromUserId(any()) } returns flowOf(address)

        val result = controller.updateRoleByEmail(dtoRoleUpdated, entity)
        val res = result.body!!

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertNotNull(res) },
            { assertEquals(result.statusCode, HttpStatus.OK) },
            { assertEquals(dto.email, res.email) },
            { assertEquals(dto.username, res.username) },
            { assertEquals(dto.avatar, res.avatar) }
        )

        coVerify { service.updateRoleByEmail(any()) }
        coVerify { service.findAllFromUserId(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun updateRoleByEmailNF() = runTest {
        coEvery { service.updateRoleByEmail(any()) } throws
                UserExceptionNotFound("User with email: ${dtoRoleUpdated.email} not found.")

        val result = assertThrows<UserExceptionNotFound> {
            controller.updateRoleByEmail(dtoRoleUpdated, entity)
        }

        assertEquals("User with email: ${dtoRoleUpdated.email} not found.", result.message)

        coVerify { service.updateRoleByEmail(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun uRoleMal1() = runTest {
        val result = assertThrows<UserExceptionBadRequest> {
            controller.updateRoleByEmail(dtoRoleUpdatedMal1, entity)
        }
        assertEquals("Email cannot be blank.", result.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun uRoleMal2() = runTest {
        val result = assertThrows<UserExceptionBadRequest> {
            controller.updateRoleByEmail(dtoRoleUpdatedMal2, entity)
        }
        assertEquals("Invalid email.", result.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteUser() = runTest {
        coEvery { service.delete(any()) } returns entity
        coEvery { service.findAllFromUserId(any()) } returns flowOf(address)

        val result = controller.deleteUser(entity.email, entity)
        val res = result.body!!

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertNotNull(res) },
            { assertEquals(result.statusCode, HttpStatus.OK) },
            { assertEquals(dto.email, res.email) },
            { assertEquals(dto.username, res.username) },
            { assertEquals(dto.avatar, res.avatar) }
        )

        coVerify { service.delete(any()) }
        coVerify { service.findAllFromUserId(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteNF() = runTest {
        coEvery { service.delete(any()) } throws
                UserExceptionNotFound("User with email: ${entity.email} not found.")

        val result = assertThrows<UserExceptionNotFound> {
            controller.deleteUser(entity.email, entity)
        }

        assertEquals("User with email: ${entity.email} not found.", result.message)

        coVerify { service.delete(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findMyself() = runTest {
        coEvery { service.findAllFromUserId(any()) } returns flowOf(address)

        val result = controller.findMySelf(entity)
        val res = result.body!!

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertNotNull(res) },
            { assertEquals(result.statusCode, HttpStatus.OK) },
            { assertEquals(dto.email, res.email) },
            { assertEquals(dto.username, res.username) },
            { assertEquals(dto.avatar, res.avatar) }
        )

        coVerify { service.findAllFromUserId(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun listAddresses() = runTest {
        coEvery { service.findAllAddresses() } returns listOf(address)

        val result = controller.listAddresses(entity)
        val res = result.body!!

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertNotNull(res) },
            { assertEquals(result.statusCode, HttpStatus.OK) },
            { assertEquals(address.address, res[0].address) },
            { assertEquals(address.userId, res[0].userId) },
            { assertEquals(address.id, res[0].id) }
        )

        coVerify { service.findAllAddresses() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun listAddressesNF() = runTest {
        coEvery { service.findAllAddresses() } returns listOf()

        val result = controller.listAddresses(entity)
        val res = result.body!!

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertNotNull(res) },
            { assertEquals(result.statusCode, HttpStatus.OK) },
            { assertEquals(0, res.size) }
        )

        coVerify { service.findAllAddresses() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun listAddressesByUserId() = runTest {
        coEvery { service.listAddressesByUserId(any()) } returns address.address

        val result = controller.listAddressesByUserId(entity.id!!, entity)
        val res = result.body!!

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertNotNull(res) },
            { assertEquals(result.statusCode, HttpStatus.OK) },
            { assertEquals(address.address, res) }
        )

        coVerify { service.listAddressesByUserId(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun listAddressesByUserIdNF() = runTest {
        coEvery { service.listAddressesByUserId(any()) } throws
                AddressExceptionNotFound("Addresses with userId: ${entity.id} not found.")

        val result = assertThrows<AddressExceptionNotFound> {
            controller.listAddressesByUserId(entity.id!!, entity)
        }

        assertEquals("Addresses with userId: ${entity.id} not found.", result.message)

        coVerify { service.listAddressesByUserId(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findAddressById() = runTest {
        coEvery { service.findAddressById(any()) } returns address.address

        val result = controller.findById(address.id!!, entity)
        val res = result.body!!

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertNotNull(res) },
            { assertEquals(result.statusCode, HttpStatus.OK) },
            { assertEquals(address.address, res) }
        )

        coVerify { service.findAddressById(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findAddressByIdNF() = runTest {
        coEvery { service.findAddressById(any()) } throws
                AddressExceptionNotFound("Addresses with id: ${address.id} not found.")

        val result = assertThrows<AddressExceptionNotFound> {
            controller.findById(address.id!!, entity)
        }

        assertEquals("Addresses with id: ${address.id} not found.", result.message)

        coVerify { service.findAddressById(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findAddressByName() = runTest {
        coEvery { service.findAddressByName(any()) } returns address.address

        val result = controller.findByName(address.address, entity)
        val res = result.body!!

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertNotNull(res) },
            { assertEquals(result.statusCode, HttpStatus.OK) },
            { assertEquals(address.address, res) }
        )

        coVerify { service.findAddressByName(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findAddressByNameNF() = runTest {
        coEvery { service.findAddressByName(any()) } throws
                AddressExceptionNotFound("Addresses with name: ${address.address} not found.")

        val result = assertThrows<AddressExceptionNotFound> {
            controller.findByName(address.address, entity)
        }

        assertEquals("Addresses with name: ${address.address} not found.", result.message)

        coVerify { service.findAddressByName(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteAddress() = runTest {
        coEvery { service.deleteAddress(any(), any()) } returns address.address

        val result = controller.deleteAddress(address.address, entity)
        val res = result.body!!

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertNotNull(res) },
            { assertEquals(result.statusCode, HttpStatus.OK) },
            { assertEquals(address.address, res) }
        )

        coVerify { service.deleteAddress(any(), any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteAddressAddressNotFound() = runTest {
        coEvery { service.deleteAddress(any(), any()) } throws
                AddressExceptionNotFound("Address not found.")

        val result = assertThrows<AddressExceptionNotFound> {
            controller.deleteAddress(address.address, entity)
        }

        assertEquals("Address not found.", result.message)

        coVerify { service.deleteAddress(any(), any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteAddressUserNotFound() = runTest {
        coEvery { service.deleteAddress(any(), any()) } throws
                AddressExceptionNotFound("User not found.")

        val result = assertThrows<AddressExceptionNotFound> {
            controller.deleteAddress(address.address, entity)
        }

        assertEquals("User not found.", result.message)

        coVerify { service.deleteAddress(any(), any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteAddressBadRequest() = runTest {
        coEvery { service.deleteAddress(any(), any()) } throws
                AddressExceptionBadRequest("No ha sido posible eliminar la direccion.")

        val result = assertThrows<AddressExceptionBadRequest> {
            controller.deleteAddress(address.address, entity)
        }

        assertEquals("No ha sido posible eliminar la direccion.", result.message)

        coVerify { service.deleteAddress(any(), any()) }
    }
}