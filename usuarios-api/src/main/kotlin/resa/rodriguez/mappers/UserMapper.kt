package resa.rodriguez.mappers

import kotlinx.coroutines.flow.toSet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import resa.rodriguez.dto.UserDTOUpdated
import resa.rodriguez.dto.UserDTOcreate
import resa.rodriguez.dto.UserDTOregister
import resa.rodriguez.dto.UserDTOresponse
import resa.rodriguez.models.Address
import resa.rodriguez.models.User
import resa.rodriguez.repositories.address.AddressRepository
import java.time.LocalDate
import java.util.*

/**
 * Clase encargada del paso de dto a modelo y al contrario, tanto de user como de address
 *
 * @property aRepo
 */
@Service
class UserMapper
@Autowired constructor(private val aRepo: AddressRepository) {
    suspend fun toDTO(user: User): UserDTOresponse {
        val addresses = aRepo.findAllByUserId(user.id!!).toSet()
        val addressesString = mutableSetOf<String>()
        addresses.forEach { addressesString.add(it.address) }
        return UserDTOresponse(
            username = user.username,
            email = user.email,
            role = user.role,
            addresses = addressesString,
            avatar = user.avatar,
            createdAt = user.createdAt,
            active = user.active
        )
    }

    suspend fun toDTO(users: List<User>): List<UserDTOresponse> {
        val res = mutableListOf<UserDTOresponse>()
        users.forEach { res.add(toDTO(it)) }
        return res
    }
}

fun UserDTOregister.fromDTOtoUser(): User? {
    return if (password != repeatPassword) null
    else User(
        username = username,
        email = email,
        password = password,
        phone = phone,
        role = User.UserRole.USER,
        createdAt = LocalDate.now(),
        avatar = "",
        active = true
    )
}

fun UserDTOregister.fromDTOtoAddresses(id: UUID): Set<Address> {
    val result = mutableSetOf<Address>()
    addresses.forEach {
        result.add(
            Address(
                userId = id,
                address = it
            )
        )
    }
    return result.toSet()
}

fun UserDTOcreate.fromDTOtoUser() = User(
    username = username,
    email = email,
    password = password,
    phone = phone,
    role = role,
    createdAt = LocalDate.now(),
    avatar = avatar,
    active = active
)

fun UserDTOcreate.fromDTOtoAddresses(id: UUID): Set<Address> {
    val result = mutableSetOf<Address>()
    addresses.forEach {
        result.add(
            Address(
                userId = id,
                address = it
            )
        )
    }
    return result.toSet()
}

fun UserDTOUpdated.fromDTOtoAddresses(id: UUID): Set<Address> {
    val result = mutableSetOf<Address>()
    addresses.forEach {
        result.add(
            Address(
                userId = id,
                address = it
            )
        )
    }
    return result.toSet()
}

fun toAddress(id: UUID, address: String): Address {
    return Address(
        userId = id,
        address = address
    )
}