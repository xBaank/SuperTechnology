package resa.rodriguez.repositories.address

import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import resa.rodriguez.models.Address
import java.util.*

/**
 * Interface that the Cached Repository will implement.
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 */
interface IAddressRepositoryCached {
    suspend fun findAll(): Flow<Address>
    suspend fun findAllPaged(page: PageRequest): Flow<Page<Address>>
    suspend fun findAllFromUserId(id: UUID): Flow<Address>
    suspend fun findById(id: UUID): Address?
    suspend fun findAllByAddress(address: String): Flow<Address>
    suspend fun save(address: Address): Address
    suspend fun deleteById(id: UUID): Address?
    suspend fun deleteAllByUserId(id: UUID): Flow<Address>
}