package resa.rodriguez.repositories.address

import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import resa.rodriguez.models.Address
import java.util.*

@Repository
interface AddressRepository : CoroutineCrudRepository<Address, UUID> {
    fun findFirstByAddress(direccion: String): Flow<Address>
    fun findAllByUserId(id: UUID): Flow<Address>
    fun findAllBy(page: Pageable?): Flow<Address>
}