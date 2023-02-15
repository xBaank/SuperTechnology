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
import resa.rodriguez.models.User
import resa.rodriguez.models.UserRole
import java.time.LocalDate
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class UserRepositoryCachedTest {
    private val user = User(
        UUID.randomUUID(),
        "UserTest",
        "q@q.com",
        "uwu",
        "1234567",
        "",
        UserRole.SUPER_ADMIN,
        LocalDate.now(),
        true
    )

    @MockK
    lateinit var repo: UserRepository

    @InjectMockKs
    lateinit var repository: UserRepositoryCached

    init { MockKAnnotations.init(this) }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findAll() = runTest {
        coEvery { repo.findAll() } returns flowOf(user)

        val result = repository.findAll().toList()

        assertAll(
            { Assertions.assertNotNull(result) },
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
            { Assertions.assertNotNull(result) },
            { assertTrue(result.isEmpty()) }
        )
        coVerify { repo.findAll() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findByActivo() = runTest {
        coEvery { repo.findAllByActiveOrderByCreatedAt(true) } returns flowOf(user)

        val result = repository.findByActivo(true).toList()

        assertAll(
            { Assertions.assertNotNull(result) },
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
            { Assertions.assertNotNull(result) },
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
            { Assertions.assertNull(result) }
        )
        coVerify {repo.findById(any())}
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findByUsername() = runTest {
        coEvery { repo.findByUsername(any()) } returns flowOf(user)

        val result = repository.findByUsername(user.username)

        assertAll(
            { Assertions.assertNotNull(result) },
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
            { Assertions.assertNull(result) }
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
            { Assertions.assertNull(result) }
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
            { Assertions.assertNull(result) }
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
            { Assertions.assertNull(result) }
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