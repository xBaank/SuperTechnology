package resa.rodriguez.mappers

import kotlinx.coroutines.flow.toSet
import resa.rodriguez.dto.UserDTOUpdated
import resa.rodriguez.dto.UserDTOcreate
import resa.rodriguez.dto.UserDTOregister
import resa.rodriguez.dto.UserDTOresponse
import resa.rodriguez.models.Address
import resa.rodriguez.models.User
import resa.rodriguez.repositories.address.AddressRepositoryCached
import java.time.LocalDate
import java.util.*

/**
 * Clase encargada del paso de dto a modelo y al contrario, tanto de user como de address
 *
 * @property aRepo
 */

fun User.toDTO(addresses: Set<Address>): UserDTOresponse {
    val addressesString = mutableSetOf<String>()
    addresses.forEach { addressesString.add(it.address) }
    return UserDTOresponse(
        username = username,
        email = email,
        role = role,
        addresses = addressesString,
        avatar = avatar,
        createdAt = createdAt,
        active = active
    )
}

suspend fun List<User>.toDTOlist(aRepo: AddressRepositoryCached): List<UserDTOresponse> {
    val listDTO = mutableListOf<UserDTOresponse>()
    forEach { listDTO.add(it.toDTO(it.id?.let { it1 -> aRepo.findAllFromUserId(it1).toSet() } ?: setOf())) }
    return listDTO
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