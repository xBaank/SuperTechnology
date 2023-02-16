package resa.rodriguez.repositories.address

import kotlinx.coroutines.flow.Flow
import resa.rodriguez.models.Address
import java.util.*

interface IAddressRepositoryCached {
    suspend fun findAll(): Flow<Address>
    suspend fun findAllFromUserId(id: UUID): Flow<Address>
    suspend fun findById(id: UUID): Address?
    suspend fun findAllByAddress(address: String): Flow<Address>
    suspend fun save(address: Address): Address
    suspend fun deleteById(id: UUID): Address?
    suspend fun deleteAllByUserId(id: UUID): Flow<Address>
    suspend fun update(id: UUID, address: String): Address?
}