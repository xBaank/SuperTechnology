package resa.rodriguez.mappers

import kotlinx.coroutines.flow.toSet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import resa.rodriguez.dto.UserDTOcreate
import resa.rodriguez.dto.UserDTOlogin
import resa.rodriguez.dto.UserDTOregister
import resa.rodriguez.dto.UserDTOresponse
import resa.rodriguez.models.User
import resa.rodriguez.repositories.AddressRepository

@Service
class UserMapper
@Autowired constructor(
    private val aRepo: AddressRepository
) {
    suspend fun toDTO(user: User) : UserDTOresponse {
        val addresses = aRepo.findAllByUserId(user.id).toSet()
        val addressesString = mutableSetOf<String>()
        addresses.forEach { addressesString.add(it.address) }
        return UserDTOresponse(
            username = user.username,
            email = user.email,
            role = user.role,
            addresses = addressesString
        )
    }
}
/*
fun UserDTOregister.fromDTO() = User (

)

fun UserDTOcreate.fromDTO() = User (

)

fun UserDTOlogin.fromDTO() = User (

)

 */