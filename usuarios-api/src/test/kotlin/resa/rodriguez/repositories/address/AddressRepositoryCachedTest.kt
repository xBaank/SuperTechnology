package resa.rodriguez.repositories.address

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
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import resa.rodriguez.models.Address
import resa.rodriguez.models.User
import resa.rodriguez.models.UserRole
import resa.rodriguez.repositories.user.UserRepository
import java.time.LocalDate
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
//@SpringBootTest
class UserRepositoryCachedTest {
    private val address = Address(
        UUID.randomUUID(),
        UUID.randomUUID(),
        "patata test"
    )

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
    lateinit var repo: AddressRepository

    @MockK
    lateinit var uRepo: UserRepository

    @InjectMockKs
    lateinit var repository: AddressRepositoryCached

    init { MockKAnnotations.init(this) }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findAll() = runTest {
        coEvery { repo.findAll() } returns flowOf(address)

        val result = repository.findAll().toList()

        assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertEquals(address.id, result[0].id) }
        )
        coVerify { repo.findAll() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findAllNF() = runTest {
        coEvery { repo.findAll() } returns flowOf()

        val result = repository.findAll().toList()

        assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertTrue(result.isEmpty()) }
        )
        coVerify { repo.findAll() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findAllFromUserId() = runTest {
        coEvery { repo.findAllByUserId(any()) } returns flowOf(address)

        val result = repository.findAllFromUserId(address.userId).toList()

        assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertEquals(address.id, result[0].id) }
        )
        coVerify { repo.findAllByUserId(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findAllFromUserIdNF() = runTest {
        coEvery { repo.findAllByUserId(any()) } returns flowOf()

        val result = repository.findAllFromUserId(address.userId).toList()

        assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertTrue(result.isEmpty()) }
        )
        coVerify { repo.findAllByUserId(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findByID() = runTest {
        coEvery { repo.findById(any()) } returns address

        val result = repository.findById(address.id!!)

        assertAll(
            { Assertions.assertEquals(address.id, result?.id) },
            { Assertions.assertEquals(address.address, result?.address) }
        )
        coVerify {repo.findById(any())}
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findByIDNF() = runTest {
        coEvery { repo.findById(any()) } returns null

        val result = repository.findById(address.id!!)

        assertAll(
            { Assertions.assertNull(result) }
        )
        coVerify {repo.findById(any())}
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findAllByAddress() = runTest {
        coEvery { repo.findFirstByAddress(any()) } returns flowOf(address)

        val result = repository.findAllByAddress(address.address).toList()

        assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertEquals(address.id, result[0].id) }
        )
        coVerify { repo.findFirstByAddress(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findAllByAddressNF() = runTest {
        coEvery { repo.findFirstByAddress(any()) } returns flowOf()

        val result = repository.findAllByAddress(address.address).toList()

        assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertTrue(result.isEmpty()) }
        )
        coVerify { repo.findFirstByAddress(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun save() = runTest {
        coEvery { repo.save(any()) } returns address

        val result = repository.save(address)

        assertAll(
            { Assertions.assertEquals(address.id, result.id) },
            { Assertions.assertEquals(address.address, result.address) },
        )

        coVerify { repo.save(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteById() = runTest {
        coEvery { repo.findById(any()) } returns address
        coEvery { repo.deleteById(any()) } returns Unit

        val result = repository.deleteById(address.id!!)

        assertAll(
            { Assertions.assertEquals(address.id, result?.id) },
            { Assertions.assertEquals(address.address, result?.address) },
        )

        coVerify { repo.deleteById(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteByIdNF() = runTest {
        coEvery { repo.findById(any()) } returns null
        coEvery { repo.deleteById(any()) } returns Unit

        val result = repository.deleteById(address.id!!)

        assertAll(
            { Assertions.assertNull(result) }
        )

        coVerify { repo.findById(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteAllByUserId() = runTest {
        coEvery { uRepo.findById(any()) } returns user
        coEvery { repo.findAllByUserId(any()) } returns flowOf(address)
        coEvery { repo.deleteAll(allAny()) } returns Unit

        val result = repository.deleteAllByUserId(address.userId).toList()

        assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertEquals(address.id, result[0].id) }
        )

        coVerify { repo.findAllByUserId(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteAllByUserIdNF() = runTest {
        coEvery { uRepo.findById(any()) } returns null
        coEvery { repo.findAllByUserId(any()) } returns flowOf()
        coEvery { repo.deleteAll(allAny()) } returns Unit

        val result = repository.deleteAllByUserId(address.userId).toList()

        assertAll(
            { Assertions.assertNotNull(result) },
            { Assertions.assertTrue(result.isEmpty()) }
        )

        coVerify { uRepo.findById(allAny()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun update() = runTest {
        coEvery { repo.findById(any()) } returns address
        coEvery { repo.save(any()) } returns address

        val result = repository.update(address.id!!, address.address)

        assertAll(
            { Assertions.assertEquals(address.id, result?.id) },
            { Assertions.assertEquals(address.address, result?.address) },
        )

        coVerify { repo.findById(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun updateNF() = runTest {
        coEvery { repo.findById(any()) } returns null
        coEvery { repo.save(any()) } returns address

        val result = repository.update(address.id!!, address.address)

        assertAll(
            { Assertions.assertNull(result) }
        )

        coVerify { repo.findById(any()) }
    }
}