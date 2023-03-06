package resa.rodriguez.services

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.crypto.password.PasswordEncoder
import resa.rodriguez.controllers.StorageController
import resa.rodriguez.dto.UserDTORoleUpdated
import resa.rodriguez.dto.UserDTOUpdated
import resa.rodriguez.dto.UserDTOcreate
import resa.rodriguez.dto.UserDTOregister
import resa.rodriguez.exceptions.AddressExceptionBadRequest
import resa.rodriguez.exceptions.AddressExceptionNotFound
import resa.rodriguez.exceptions.UserExceptionNotFound
import resa.rodriguez.models.Address
import resa.rodriguez.models.User
import resa.rodriguez.repositories.address.AddressRepositoryCached
import resa.rodriguez.repositories.user.UserRepositoryCached
import java.time.LocalDate
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class UserServiceTest {
    private val dto = UserDTOcreate(
        "test", "test@gmail.com", "1234567", "123456789", User.UserRole.ADMIN,
        setOf("calle test"), "", true
    )
    private val dtoRegister = UserDTOregister(
        dto.username, dto.email, dto.password, dto.password,
        dto.phone, dto.addresses
    )
    private val dtoUpdated = UserDTOUpdated(dto.password, dto.addresses)
    private val dtoRoleUpdated = UserDTORoleUpdated(dto.email, dto.role)
    private val entity = User(
        UUID.randomUUID(), dto.username, dto.email, dto.password, dto.phone,
        dto.avatar, dto.role, LocalDate.now(), dto.active
    )
    private val address = Address(UUID.randomUUID(), entity.id!!, dto.addresses.first())
    private val address2 = Address(UUID.randomUUID(), entity.id!!, dto.addresses.first())

    @MockK private lateinit var uRepo: UserRepositoryCached
    @MockK private lateinit var aRepo: AddressRepositoryCached
    @MockK private lateinit var passwordEncoder: PasswordEncoder
    @MockK private lateinit var storageController: StorageController
    @InjectMockKs private lateinit var service: UserService

    init { MockKAnnotations.init(this) }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun register() = runTest {
        coEvery { passwordEncoder.encode(any()) } returns dto.password
        coEvery { uRepo.save(any()) } returns entity
        coEvery { aRepo.save(any()) } returns address

        val result = service.register(dtoRegister)

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertEquals(dto.email, result.email) },
            { Assertions.assertEquals(dto.phone, result.phone) }
        )
        coVerify { passwordEncoder.encode(any()) }
        coVerify { uRepo.save(any()) }
        coVerify { uRepo.save(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findAll() = runTest {
        coEvery { uRepo.findAll() } returns flowOf(entity)

        val result = service.listUsers()

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertEquals(dto.email, result[0].email) },
            { Assertions.assertEquals(dto.phone, result[0].phone) }
        )
        coVerify(exactly = 1) { uRepo.findAll() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findAllNF() = runTest {
        coEvery { uRepo.findAll() } returns flowOf()

        val result = service.listUsers().toList()

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertTrue(result.isEmpty()) }
        )
        coVerify { uRepo.findAll() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findByUsername() = runTest {
        coEvery { uRepo.findByUsername(any()) } returns entity

        val result = service.findByUsername(dto.username)

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertEquals(dto.email, result.email) },
            { Assertions.assertEquals(dto.phone, result.phone) },
        )
        coVerify { uRepo.findByUsername(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findByUsernameNF() = runTest {
        coEvery { uRepo.findByUsername(any()) } returns null

        val result = assertThrows<UserExceptionNotFound> {
            service.findByUsername(dto.username)
        }

        Assertions.assertAll(
            { Assertions.assertEquals(
                "User with username ${dto.username} not found.", result.message
            ) }
        )
        coVerify { uRepo.findByUsername(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findById() = runTest {
        coEvery { uRepo.findById(any()) } returns entity

        val result = service.findById(entity.id!!)

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertEquals(dto.email, result.email) },
            { Assertions.assertEquals(dto.phone, result.phone) },
        )
        coVerify(exactly = 1) { uRepo.findById(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findByIdNF() = runTest {
        coEvery { uRepo.findById(any()) } returns null

        val result = assertThrows<UserExceptionNotFound> {
            service.findById(entity.id!!)
        }

        Assertions.assertAll(
            { Assertions.assertEquals(
                "User with id ${entity.id} not found.", result.message
            ) }
        )
        coVerify { uRepo.findById(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findByEmail() = runTest {
        coEvery { uRepo.findByEmail(any()) } returns entity

        val result = service.findByEmail(dto.email)

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertEquals(dto.email, result.email) },
            { Assertions.assertEquals(dto.phone, result.phone) },
        )
        coVerify { uRepo.findByEmail(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findByEmailNF() = runTest {
        coEvery { uRepo.findByEmail(any()) } returns null

        val result = assertThrows<UserExceptionNotFound> {
            service.findByEmail(dto.email)
        }

        Assertions.assertAll(
            { Assertions.assertEquals(
                "User with email ${dto.email} not found.", result.message
            ) }
        )
        coVerify { uRepo.findByEmail(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findByPhone() = runTest {
        coEvery { uRepo.findByPhone(any()) } returns entity

        val result = service.findByUserPhone(dto.phone)

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertEquals(dto.email, result.email) },
            { Assertions.assertEquals(dto.phone, result.phone) },
        )
        coVerify { uRepo.findByPhone(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findByPhoneNF() = runTest {
        coEvery { uRepo.findByPhone(any()) } returns null

        val result = assertThrows<UserExceptionNotFound> {
            service.findByUserPhone(dto.phone)
        }

        Assertions.assertAll(
            { Assertions.assertEquals(
                "User with phone ${dto.phone} not found.", result.message
            ) }
        )
        coVerify { uRepo.findByPhone(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun save() = runTest {
        coEvery { uRepo.save(any()) } returns entity
        coEvery { aRepo.save(any()) } returns address
        coEvery { passwordEncoder.encode(any()) } returns dto.password

        val result = service.create(dto)

        Assertions.assertAll(
            { Assertions.assertEquals(dto.email, result.email) },
            { Assertions.assertEquals(dto.phone, result.phone) },
        )

        coVerify { uRepo.save(any()) }
        coVerify { aRepo.save(any()) }
        coVerify { passwordEncoder.encode(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findAllfromUserID() = runTest {
        coEvery { aRepo.findAllFromUserId(any()) } returns flowOf(address)

        val result = service.findAllFromUserId(entity.id!!).toList()

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertEquals(address.address, result[0].address) }
        )
        coVerify(exactly = 1) { aRepo.findAllFromUserId(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findAllfromUserIDNF() = runTest {
        coEvery { aRepo.findAllFromUserId(any()) } returns flowOf()

        val result = service.findAllFromUserId(entity.id!!).toList()

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertTrue(result.isEmpty()) }
        )
        coVerify { aRepo.findAllFromUserId(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findAllByActive() = runTest {
        coEvery { uRepo.findByActivo(any()) } returns flowOf(entity)

        val result = service.findAllByActive(entity.active)

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertEquals(entity.email, result[0].email) },
            { Assertions.assertEquals(entity.phone, result[0].phone) }
        )
        coVerify(exactly = 1) { uRepo.findByActivo(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findAllByActiveNF() = runTest {
        coEvery { uRepo.findByActivo(any()) } returns flowOf()

        val result = service.findAllByActive(entity.active)

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertTrue(result.isEmpty()) }
        )
        coVerify { uRepo.findByActivo(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun updateMySelf() = runTest {
        coEvery { passwordEncoder.encode(any()) } returns dto.password
        coEvery { aRepo.findAllFromUserId(any()) } returns flowOf(address)
        coEvery { aRepo.deleteAllByUserId(any()) } returns flowOf(address)
        coEvery { aRepo.save(any()) } returns address
        coEvery { uRepo.save(any()) } returns entity

        val result = service.updateMySelf(entity, dtoUpdated)

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertEquals(entity.email, result.email) },
            { Assertions.assertEquals(entity.phone, result.phone) }
        )
        coVerify { uRepo.save(any()) }
        coVerify { aRepo.save(any()) }
        coVerify { aRepo.deleteAllByUserId(any()) }
        coVerify { aRepo.findAllFromUserId(any()) }
        coVerify { passwordEncoder.encode(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun switchActivity() = runTest {
        coEvery { uRepo.findByEmail(any()) } returns entity
        coEvery { uRepo.save(any()) } returns entity

        val result = service.switchActivity(dto.email)

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertEquals(dto.email, result.email) },
            { Assertions.assertEquals(dto.phone, result.phone) },
        )
        coVerify { uRepo.save(any()) }
        coVerify { uRepo.findByEmail(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun switchActivityNF() = runTest {
        coEvery { uRepo.findByEmail(any()) } returns null

        val result = assertThrows<UserExceptionNotFound> {
            service.switchActivity(dto.email)
        }

        Assertions.assertAll(
            { Assertions.assertEquals(
                "User with email: ${dto.email} not found.", result.message
            ) }
        )
        coVerify { uRepo.findByEmail(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun updateRolByEmail() = runTest {
        coEvery { uRepo.findByEmail(any()) } returns entity
        coEvery { uRepo.save(any()) } returns entity

        val result = service.updateRoleByEmail(dtoRoleUpdated)

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertEquals(dto.email, result.email) },
            { Assertions.assertEquals(dto.phone, result.phone) },
        )
        coVerify { uRepo.save(any()) }
        coVerify { uRepo.findByEmail(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun updateRolByEmailNF() = runTest {
        coEvery { uRepo.findByEmail(any()) } returns null

        val result = assertThrows<UserExceptionNotFound> {
            service.updateRoleByEmail(dtoRoleUpdated)
        }

        Assertions.assertAll(
            { Assertions.assertEquals(
                "User with email: ${dto.email} not found.", result.message
            ) }
        )
        coVerify { uRepo.findByEmail(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun delete() = runTest {
        coEvery { uRepo.findByEmail(any()) } returns entity
        coEvery { aRepo.deleteAllByUserId(any()) } returns flowOf(address)
        coEvery { uRepo.deleteById(any()) } returns entity

        val result = service.delete(dto.email)

        Assertions.assertAll(
            { Assertions.assertEquals(dto.email, result.email) },
            { Assertions.assertEquals(dto.phone, result.phone) },
        )

        coVerify { uRepo.deleteById(any()) }
        coVerify { aRepo.deleteAllByUserId(any()) }
        coVerify { uRepo.findByEmail(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteNF1() = runTest {
        coEvery { uRepo.findByEmail(any()) } returns null
        coEvery { aRepo.deleteAllByUserId(any()) } returns flowOf(address)
        coEvery { uRepo.deleteById(any()) } returns entity

        val result = assertThrows<UserExceptionNotFound> {
            service.delete(dto.email)
        }

        Assertions.assertAll(
            { Assertions.assertEquals(
                "User with email: ${dto.email} not found.", result.message
            ) }
        )

        coVerify { uRepo.findByEmail(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteNF2() = runTest {
        coEvery { uRepo.findByEmail(any()) } returns entity
        coEvery { aRepo.deleteAllByUserId(any()) } returns flowOf(address)
        coEvery { uRepo.deleteById(any()) } returns null

        val result = assertThrows<UserExceptionNotFound> {
            service.delete(dto.email)
        }

        Assertions.assertAll(
            { Assertions.assertEquals(
                "User with email: ${dto.email} not found.", result.message
            ) }
        )

        coVerify { uRepo.deleteById(any()) }
        coVerify { aRepo.deleteAllByUserId(any()) }
        coVerify { uRepo.findByEmail(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findAllAddresses() = runTest {
        coEvery { aRepo.findAll() } returns flowOf(address)

        val result = service.findAllAddresses()

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertEquals(address.address, result[0].address) }
        )
        coVerify { aRepo.findAll() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findAllAddressesNF() = runTest {
        coEvery { aRepo.findAll() } returns flowOf()

        val result = service.findAllAddresses()

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertTrue(result.isEmpty()) }
        )
        coVerify { aRepo.findAll() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun listAddressesByUserId() = runTest {
        coEvery { aRepo.findAllFromUserId(any()) } returns flowOf(address)

        val result = service.listAddressesByUserId(entity.id!!)

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertEquals(address.address, result) }
        )
        coVerify { aRepo.findAllFromUserId(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun listAddressesByUserIdNF() = runTest {
        coEvery { aRepo.findAllFromUserId(any()) } returns flowOf()

        val result = assertThrows<AddressExceptionNotFound> {
            service.listAddressesByUserId(entity.id!!)
        }

        Assertions.assertAll(
            { Assertions.assertEquals(
                "Addresses with userId: ${entity.id} not found.", result.message
            ) }
        )
        coVerify { aRepo.findAllFromUserId(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findAddressById() = runTest {
        coEvery { aRepo.findById(any()) } returns address

        val result = service.findAddressById(address.id!!)

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertEquals(address.address, result) }
        )
        coVerify { aRepo.findById(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findAddressByIdNF() = runTest {
        coEvery { aRepo.findById(any()) } returns null

        val result = assertThrows<AddressExceptionNotFound> {
            service.findAddressById(address.id!!)
        }

        Assertions.assertAll(
            { Assertions.assertEquals(
                "Address with id: ${address.id} not found.", result.message
            ) }
        )
        coVerify { aRepo.findById(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findAddressByName() = runTest {
        coEvery { aRepo.findAllByAddress(any()) } returns flowOf(address)

        val result = service.findAddressByName(address.address)

        Assertions.assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertEquals(address.address, result) }
        )
        coVerify { aRepo.findAllByAddress(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findAddressByNameNF() = runTest {
        coEvery { aRepo.findAllByAddress(any()) } returns flowOf()

        val result = assertThrows<AddressExceptionNotFound> {
            service.findAddressByName(address.address)
        }

        Assertions.assertAll(
            { Assertions.assertEquals(
                "Address with name: ${address.address} not found.", result.message
            ) }
        )
        coVerify { aRepo.findAllByAddress(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteAddress() = runTest {
        coEvery { aRepo.findAllByAddress(any()) } returns flowOf(address)
        coEvery { uRepo.findByEmail(any()) } returns entity
        coEvery { aRepo.findAllFromUserId(any()) } returns flowOf(address)
        coEvery { aRepo.deleteById(any()) } returns address

        val result = assertThrows<AddressExceptionBadRequest> {
            service.deleteAddress(address.address, entity.email)
        }

        Assertions.assertAll(
            { Assertions.assertEquals("No ha sido posible eliminar la direccion.", result.message) }
        )

        coVerify { aRepo.findAllByAddress(any()) }
        coVerify { uRepo.findByEmail(any()) }
        coVerify { aRepo.findAllFromUserId(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteAddress2() = runTest {
        coEvery { aRepo.findAllByAddress(any()) } returns flowOf(address)
        coEvery { uRepo.findByEmail(any()) } returns entity
        coEvery { aRepo.findAllFromUserId(any()) } returns flowOf(address, address2)
        coEvery { aRepo.deleteById(any()) } returns address

        val result = service.deleteAddress(address.address, entity.email)

        Assertions.assertAll(
            { Assertions.assertEquals("Direccion ${address?.address} eliminada.", result) }
        )

        coVerify { aRepo.findAllByAddress(any()) }
        coVerify { uRepo.findByEmail(any()) }
        coVerify { aRepo.findAllFromUserId(any()) }
        coVerify { aRepo.deleteById(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteAddressNF1() = runTest {
        coEvery { aRepo.findAllByAddress(any()) } returns flowOf()
        coEvery { uRepo.findByEmail(any()) } returns entity
        coEvery { aRepo.findAllFromUserId(any()) } returns flowOf(address)
        coEvery { aRepo.deleteById(any()) } returns address

        val result = assertThrows<AddressExceptionNotFound> {
            service.deleteAddress(address.address, entity.email)
        }

        Assertions.assertAll(
            { Assertions.assertEquals(
                "Address not found.", result.message
            ) }
        )

        coVerify { aRepo.findAllByAddress(any()) }
        coVerify { uRepo.findByEmail(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteAddressNF2() = runTest {
        coEvery { aRepo.findAllByAddress(any()) } returns flowOf(address)
        coEvery { uRepo.findByEmail(any()) } returns null
        coEvery { aRepo.findAllFromUserId(any()) } returns flowOf(address)
        coEvery { aRepo.deleteById(any()) } returns address

        val result = assertThrows<UserExceptionNotFound> {
            service.deleteAddress(address.address, entity.email)
        }

        Assertions.assertAll(
            { Assertions.assertEquals(
                "User not found.", result.message
            ) }
        )

        coVerify { aRepo.findAllByAddress(any()) }
        coVerify { uRepo.findByEmail(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteAddressNF3() = runTest {
        coEvery { aRepo.findAllByAddress(any()) } returns flowOf(address)
        coEvery { uRepo.findByEmail(any()) } returns entity
        coEvery { aRepo.findAllFromUserId(any()) } returns flowOf()
        coEvery { aRepo.deleteById(any()) } returns address

        val result = assertThrows<AddressExceptionBadRequest> {
            service.deleteAddress(address.address, entity.email)
        }

        Assertions.assertAll(
            { Assertions.assertEquals(
                "No ha sido posible eliminar la direccion.", result.message
            ) }
        )

        coVerify { aRepo.findAllByAddress(any()) }
        coVerify { uRepo.findByEmail(any()) }
        coVerify { aRepo.findAllFromUserId(any()) }
    }
}