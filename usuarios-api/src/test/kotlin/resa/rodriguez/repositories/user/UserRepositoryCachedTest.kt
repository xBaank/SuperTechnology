package resa.rodriguez.repositories.user

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
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import resa.rodriguez.models.Address
import resa.rodriguez.models.User
import resa.rodriguez.repositories.address.AddressRepository
import java.time.LocalDate
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
internal class UserRepositoryCachedTest {
    private val user = User(
        UUID.randomUUID(),
        "UserTest",
        "q@q.com",
        "uwu",
        "1234567",
        "",
        User.UserRole.SUPER_ADMIN,
        LocalDate.now(),
        true
    )
    private val address = Address(UUID.randomUUID(), user.id!!, "AddressTest")

    @MockK
    lateinit var repo: UserRepository

    @MockK
    lateinit var aRepo: AddressRepository

    @InjectMockKs
    lateinit var repository: UserRepositoryCached

    init { MockKAnnotations.init(this) }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findAll() = runTest {
        coEvery { repo.findAll() } returns flowOf(user)

        val result = repository.findAll().toList()

        assertAll(
            { assertNotNull(result) },
            { assertEquals(user.id, result[0].id) }
        )
        coVerify(exactly = 1) { repo.findAll() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findAllNF() = runTest {
        coEvery { repo.findAll() } returns flowOf()

        val result = repository.findAll().toList()

        assertAll(
            { assertNotNull(result) },
            { assertTrue(result.isEmpty()) }
        )
        coVerify { repo.findAll() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findAllPaged() = runTest {
        coEvery { repo.findAllBy(any()) } returns flowOf(user)
        coEvery { aRepo.findAllByUserId(any()) } returns flowOf(address)
        coEvery { repo.count() } returns 1

        val pageRequest = PageRequest.of(0, 10, Sort.Direction.ASC, "created_at")
        val result = repository.findAllPaged(pageRequest).toList()

        assertAll(
            { assertNotNull(result) },
            { assertEquals(user.username, result[0].get().toList()[0].username) }
        )
        coVerify { repo.findAllBy(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findAllPagedNF() = runTest {
        coEvery { repo.findAllBy(any()) } returns flowOf()
        coEvery { repo.count() } returns 0

        val pageRequest = PageRequest.of(0, 10, Sort.Direction.ASC, "created_at")
        val result = repository.findAllPaged(pageRequest).toList()

        assertAll(
            { assertNotNull(result) },
            { assertTrue(result.isEmpty()) }
        )
        coVerify { repo.findAllBy(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findByActivo() = runTest {
        coEvery { repo.findAllByActiveOrderByCreatedAt(true) } returns flowOf(user)

        val result = repository.findByActivo(true).toList()

        assertAll(
            { assertNotNull(result) },
            { assertEquals(user.id, result[0].id) }
        )
        coVerify { repo.findAll() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findByActivoNF() = runTest {
        coEvery { repo.findAllByActiveOrderByCreatedAt(true) } returns flowOf()

        val result = repository.findByActivo(true).toList()

        assertAll(
            { assertNotNull(result) },
            { assertTrue(result.isEmpty()) }
        )
        coVerify(exactly = 1) { repo.findAll() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findByID() = runTest {
        coEvery { repo.findById(any()) } returns user

        val result = repository.findById(user.id!!)

        assertAll(
            { assertEquals(user.id, result?.id)},
            { assertEquals(user.username, result?.username)}
        )
        coVerify {repo.findById(any())}
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findByIDNF() = runTest {
        coEvery { repo.findById(any()) } returns null

        val result = repository.findById(user.id!!)

        assertAll(
            { assertNull(result) }
        )
        coVerify {repo.findById(any())}
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findByUsername() = runTest {
        coEvery { repo.findByUsername(any()) } returns flowOf(user)

        val result = repository.findByUsername(user.username)

        assertAll(
            { assertNotNull(result) },
            { assertEquals(user.id, result?.id) }
        )
        coVerify { repo.findAll() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findByUsernameNF() = runTest {
        coEvery { repo.findByUsername(any()) } returns flowOf()

        val result = repository.findByUsername(user.username)

        assertAll(
            { assertNull(result) }
        )
        coVerify { repo.findAll() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findByEmail() = runTest {
        coEvery { repo.findFirstByEmail(any()) } returns flowOf(user)

        val result = repository.findByEmail(user.email)

        assertAll(
            { assertEquals(user.id, result?.id)},
            { assertEquals(user.username, result?.username)}
        )
        coVerify {repo.findById(any())}
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findByEmailNF() = runTest {
        coEvery { repo.findFirstByEmail(any()) } returns flowOf()

        val result = repository.findByEmail(user.email)

        assertAll(
            { assertNull(result) }
        )
        coVerify {repo.findById(any())}
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findByPhone() = runTest {
        coEvery { repo.findFirstByPhone(any()) } returns flowOf(user)

        val result = repository.findByPhone(user.phone)

        assertAll(
            { assertEquals(user.id, result?.id)},
            { assertEquals(user.username, result?.username)}
        )
        coVerify {repo.findById(any())}
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findByPhoneNF() = runTest {
        coEvery { repo.findFirstByPhone(any()) } returns flowOf()

        val result = repository.findByPhone(user.phone)

        assertAll(
            { assertNull(result) }
        )
        coVerify {repo.findById(any())}
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun save() = runTest {
        coEvery { repo.save(any()) } returns user

        val result = repository.save(user)

        assertAll(
            { assertEquals(user.id, result.id) },
            { assertEquals(user.username, result.username) },
        )

        coVerify { repo.save(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun update() = runTest {
        coEvery { repo.findById(any()) } returns user
        coEvery { repo.save(any()) } returns user

        val result = repository.update(user.id!!, user)

        assertAll(
            { assertEquals(user.id, result?.id) },
            { assertEquals(user.username, result?.username) },
        )

        coVerify { repo.findById(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun updateNF() = runTest {
        coEvery { repo.findById(any()) } returns null
        coEvery { repo.save(any()) } returns user

        val result = repository.update(user.id!!, user)

        assertAll(
            { assertNull(result) }
        )

        coVerify { repo.findById(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteById() = runTest {
        coEvery { repo.findById(any()) } returns user
        coEvery { repo.deleteById(any()) } returns Unit

        val result = repository.deleteById(user.id!!)

        assertAll(
            { assertEquals(user.id, result?.id) },
            { assertEquals(user.username, result?.username) },
        )

        coVerify { repo.deleteById(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteByIdNF() = runTest {
        coEvery { repo.findById(any()) } returns null
        coEvery { repo.deleteById(any()) } returns Unit

        val result = repository.deleteById(user.id!!)

        assertAll(
            { assertNull(result) }
        )

        coVerify { repo.findById(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun setActivity() = runTest {
        coEvery { repo.findById(any()) } returns user
        coEvery { repo.save(any()) } returns user

        val result = repository.setActivity(user.id!!, true)

        assertAll(
            { assertEquals(user.id, result?.id) },
            { assertEquals(user.username, result?.username) },
        )

        coVerify { repo.findById(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun setActivityNF() = runTest {
        coEvery { repo.findById(any()) } returns null
        coEvery { repo.save(any()) } returns user

        val result = repository.setActivity(user.id!!, true)

        assertAll(
            { assertNull(result) }
        )

        coVerify { repo.findById(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun updateCapado() = runTest {
        coEvery { repo.findById(any()) } returns user
        coEvery { repo.save(any()) } returns user

        val result = repository.updateCapado(user.id!!, user)

        assertAll(
            { assertEquals(user.id, result?.id) },
            { assertEquals(user.username, result?.username) },
        )

        coVerify { repo.findById(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun updateCapadoNF() = runTest {
        coEvery { repo.findById(any()) } returns null
        coEvery { repo.save(any()) } returns user

        val result = repository.updateCapado(user.id!!, user)

        assertAll(
            { assertNull(result) }
        )

        coVerify { repo.findById(any()) }
    }
}