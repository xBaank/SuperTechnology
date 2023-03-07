package resa.rodriguez.validators

import resa.rodriguez.dto.*
import resa.rodriguez.exceptions.UserExceptionBadRequest

/**
 * Functions that validate every kind of user DTO along with their fields.
 * @author Mario Resa, Daniel Rodriguez, Jhoan Sebastian Mendoza,
 * Alfredo Rafael Maldonado, Azahara Blanco, Ivan Azagra, Roberto Blazquez
 */
fun UserDTOcreate.validate(): UserDTOcreate {
    if (this.username.isBlank())
        throw UserExceptionBadRequest("Username cannot be blank.")
    else if (this.email.isBlank())
        throw UserExceptionBadRequest("Email cannot be blank.")
    else if (!this.email.matches(Regex("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}\$")))
        throw UserExceptionBadRequest("Invalid email.")
    else if (this.password.length < 7 || this.password.isBlank())
        throw UserExceptionBadRequest("Password must at least be 7 characters long.")
    else if (this.phone.isBlank() || this.phone.length < 9)
        throw UserExceptionBadRequest("Phone must at least be 9 characters long.")
    else if (this.addresses.isEmpty())
        throw UserExceptionBadRequest("Must at least have one address.")
    else {
        this.addresses.forEach { if (it.isBlank()) throw UserExceptionBadRequest("Address cannot be blank.") }
        return this
    }
}

fun UserDTOlogin.validate(): UserDTOlogin {
    if (this.username.isBlank())
        throw UserExceptionBadRequest("Username cannot be blank.")
    else if (this.password.length < 7 || this.password.isBlank())
        throw UserExceptionBadRequest("Password must at least be 7 characters long.")
    else return this
}

fun UserDTOregister.validate(): UserDTOregister {
    if (this.username.isBlank())
        throw UserExceptionBadRequest("Username cannot be blank.")
    else if (this.email.isBlank())
        throw UserExceptionBadRequest("Email cannot be blank.")
    else if (!this.email.matches(Regex("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}\$")))
        throw UserExceptionBadRequest("Invalid email.")
    else if (this.password != this.repeatPassword)
        throw UserExceptionBadRequest("Passwords do not match.")
    else if (this.password.length < 7 || this.password.isBlank())
        throw UserExceptionBadRequest("Password must at least be 7 characters long.")
    else if (this.phone.isBlank() || this.phone.length < 9)
        throw UserExceptionBadRequest("Phone must at least be 9 characters long.")
    else if (this.addresses.isEmpty())
        throw UserExceptionBadRequest("Must at least have one address.")
    else {
        this.addresses.forEach { if (it.isBlank()) throw UserExceptionBadRequest("Address cannot be blank.") }
        return this
    }
}

fun UserDTOUpdated.validate(): UserDTOUpdated {
    if (this.password.length < 7 || this.password.isBlank())
        throw UserExceptionBadRequest("Password must at least be 7 characters long.")
    else {
        this.addresses.forEach { if (it.isBlank()) throw UserExceptionBadRequest("Address cannot be blank.") }
        return this
    }
}

fun UserDTORoleUpdated.validate(): UserDTORoleUpdated {
    if (this.email.isBlank())
        throw UserExceptionBadRequest("Email cannot be blank.")
    else if (!this.email.matches(Regex("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}\$")))
        throw UserExceptionBadRequest("Invalid email.")
    else return this
}
