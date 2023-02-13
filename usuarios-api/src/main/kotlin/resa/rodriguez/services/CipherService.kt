package resa.rodriguez.services

import com.toxicbakery.bcrypt.Bcrypt

fun cipher(message: String) : String {
    return Bcrypt.hash(message, 12).decodeToString()
}

fun matches(message: String, cipheredText: ByteArray) : Boolean {
    return Bcrypt.verify(message, cipheredText)
}