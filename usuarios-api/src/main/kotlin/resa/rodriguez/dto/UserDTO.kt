package resa.rodriguez.dto

import java.util.*

data class UserDTOlogin (
    val email: String,
    val password: String
)

data class UserDTOregister (
    val username: String,
    val email: String,
    val password: String,
    val repeatPassword: String,
    val phone: String
)

data class UserDTOcreate (
    val uuid: UUID? = UUID.randomUUID(),
    val username: String,
    val email: String,
    val password: String,
    val phone: String
)

data class UserDTOresponse (
    val username: String,
    val email: String
)