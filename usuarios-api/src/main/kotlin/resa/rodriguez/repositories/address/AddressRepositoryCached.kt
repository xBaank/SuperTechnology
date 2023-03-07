package resa.rodriguez.repositories.address

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import resa.rodriguez.models.Address
import resa.rodriguez.repositories.user.UserRepository
import java.util.*

/**
 * Repository that will execute the different CRUD operations needed to work with the database.
 * It has a cache.
 * @property aRepo Address repository
 * @property uRepo User repository
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 */
@Repository
class AddressRepositoryCached
@Autowired constructor(
    private val aRepo: AddressRepository,
    private val uRepo: UserRepository
) : IAddressRepositoryCached {
    /**
     * Function that will return a flow with every address present in the database.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @return Flow with every address in the database.
     */
    override suspend fun findAll(): Flow<Address> = withContext(Dispatchers.IO) {
        aRepo.findAll()
    }

    /**
     * Function that will return a flow of pages of address DTOs.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param page Page request.
     * @return A flow of pages containing address DTOs.
     */
    override suspend fun findAllPaged(page: PageRequest): Flow<Page<Address>> {
        return aRepo.findAllBy(page).toList().windowed(page.pageSize, page.pageSize, true)
            .map { PageImpl(it, page, aRepo.count()) }
            .asFlow()
    }

    /**
     * Function that will return a flow with every address from the specified user.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param id UUID from the user whose addresses we want to search.
     * @return Flow with every address from the specified user.
     */
    override suspend fun findAllFromUserId(id: UUID): Flow<Address> = withContext(Dispatchers.IO) {
        aRepo.findAllByUserId(id)
    }

    /**
     * Function that will return an address whose id is the same as the one given,
     * or null if there is none.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param id UUID from the address we want to search.
     * @return Address with the specified id, or null if there is none.
     */
    @Cacheable("direcciones")
    override suspend fun findById(id: UUID): Address? = withContext(Dispatchers.IO) {
        aRepo.findById(id)
    }

    /**
     * Function that will return a flow of addresses whose names are the same as the one given.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param address Name of the address we want to search.
     * @return Flow of addresses with the specified name.
     */
    override suspend fun findAllByAddress(address: String): Flow<Address> = withContext(Dispatchers.IO) {
        aRepo.findFirstByAddress(address)
    }

    /**
     * Function that will return an address after saving it into the database.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param address Address to be saved.
     * @return The saved address.
     */
    @CachePut("direcciones")
    override suspend fun save(address: Address): Address = withContext(Dispatchers.IO) {
        aRepo.save(address)
    }

    /**
     * Function that will return an address after deleting it from the database,
     * or null if it could not find it first.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param id UUID of the address to be deleted.
     * @return The deleted address, or null if it was not found.
     */
    @CacheEvict("direcciones")
    override suspend fun deleteById(id: UUID): Address? = withContext(Dispatchers.IO) {
        val res = aRepo.findById(id) ?: return@withContext null
        aRepo.deleteById(id)
        res
    }

    /**
     * Function that will return a flow of addresses after deleting them from the database,
     * all of them belonging to the user whose ID is specified.
     * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
     * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
     * @param id UUID of the user whose addresses will be deleted.
     * @return The deleted addresses.
     */
    override suspend fun deleteAllByUserId(id: UUID): Flow<Address> = withContext(Dispatchers.IO) {
        uRepo.findById(id) ?: return@withContext flowOf()
        val addresses = aRepo.findAllByUserId(id)
        addresses.toList().forEach { aRepo.delete(it) }
        addresses
    }
}