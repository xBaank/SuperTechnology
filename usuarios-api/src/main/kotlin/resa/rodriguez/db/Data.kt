package resa.rodriguez.db

import resa.rodriguez.dto.UserDTOcreate
import resa.rodriguez.models.User

/**
 * Initial data that will be loaded when launching this microservice.
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 */
fun getUsersInit() = listOf(
    UserDTOcreate(
        username = "Test_User",
        email = "test@example.com",
        password = "1234567",
        phone = "123456789",
        role = User.UserRole.SUPER_ADMIN,
        addresses = setOf("C/1", "C/1_1"),
        active = true
    ),
    UserDTOcreate(
        username = "Test_User2",
        email = "test2@example.com",
        password = "1234567",
        phone = "123456788",
        role = User.UserRole.ADMIN,
        addresses = setOf("C/2", "C/2_2"),
        active = true
    ),
    UserDTOcreate(
        username = "Test_User3",
        email = "test3@example.com",
        password = "1234567",
        phone = "123456799",
        role = User.UserRole.USER,
        addresses = setOf("C/3"),
        active = true
    ),
    UserDTOcreate(
        username = "Test_User4",
        email = "test4@example.com",
        password = "1234567",
        phone = "123459999",
        role = User.UserRole.USER,
        addresses = setOf("C/4"),
        active = false
    ),
    UserDTOcreate(
        username = "Test_User5",
        email = "test5@example.com",
        password = "1234567",
        phone = "123499999",
        role = User.UserRole.USER,
        addresses = setOf("C/5", "C/5_5"),
        active = false
    )
)