package resa.rodriguez.db

import resa.rodriguez.dto.UserDTOcreate
import resa.rodriguez.models.UserRole

/**
 * Datos de prueba introducidos como SUPER_ADMIN
 *
 */
fun getUsersInit() = listOf(
    UserDTOcreate(
        username = "Test_User",
        email = "test@example.com",
        password = "1234",
        phone = "123456789",
        role = UserRole.SUPER_ADMIN,
        addresses = setOf("C/1"),
        active = true
    ),
    UserDTOcreate(
        username = "Test_User2",
        email = "test2@example.com",
        password = "12345",
        phone = "123456788",
        role = UserRole.ADMIN,
        addresses = setOf("C/2"),
        active = true
    ),
    UserDTOcreate(
        username = "Test_User3",
        email = "test3@example.com",
        password = "123456",
        phone = "123456799",
        role = UserRole.USER,
        addresses = setOf("C/3"),
        active = true
    )
)