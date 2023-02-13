package resa.rodriguez.repositories.address

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository
import resa.rodriguez.models.Address
import resa.rodriguez.repositories.user.UserRepository
import java.util.*

/**
 * Repositorio cacheado de direcciones
 *
 * @property aRepo
 * @property uRepo
 */
@Repository
class AddressRepositoryCached
@Autowired constructor(
    private val aRepo: AddressRepository,
    private val uRepo: UserRepository
) : IAddressRepositoryCached {
    override suspend fun findAll(): Flow<Address> = withContext(Dispatchers.IO) {
        aRepo.findAll()
    }

    override suspend fun findAllFromUserId(id: UUID): Flow<Address> = withContext(Dispatchers.IO) {
        aRepo.findAllByUserId(id)
    }

    @Cacheable("direcciones")
    override suspend fun findById(id: UUID): Address? = withContext(Dispatchers.IO) {
        aRepo.findById(id)
    }

    override suspend fun findAllByAddress(address: String): Flow<Address> = withContext(Dispatchers.IO) {
        aRepo.findFirstByAddress(address)
    }

    @CachePut("direcciones")
    override suspend fun save(address: Address): Address = withContext(Dispatchers.IO) {
        aRepo.save(address)
    }

    @CacheEvict("direcciones")
    override suspend fun deleteById(id: UUID): Address? = withContext(Dispatchers.IO) {
        val res = aRepo.findById(id) ?: return@withContext null
        aRepo.deleteById(id)
        res
    }

    override suspend fun deleteAllByUserId(id: UUID): Flow<Address> = withContext(Dispatchers.IO) {
        uRepo.findById(id) ?: return@withContext flowOf()
        val addresses = aRepo.findAllByUserId(id)
        aRepo.deleteAll(addresses)
        addresses
    }

    @CachePut("direcciones")
    override suspend fun update(id: UUID, address: String): Address? = withContext(Dispatchers.IO) {
        val add = aRepo.findById(id) ?: return@withContext null
        val res = Address(
            id = add.id,
            userId = add.userId,
            address = address
        )
        aRepo.save(res)
    }
}