package com.example.data

import android.util.Base64 //binary da into string
import java.security.MessageDigest //sha256
import java.security.SecureRandom

object SecurityUtils {

    /**
     * Generates a cryptographically strong random salt.
     */
    fun generateSalt(): String {
        val random = SecureRandom()
        val bytes = ByteArray(16)
        random.nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    /**
     * Hashes a password using SHA-256 with the provided salt.
     */
    fun hashPassword(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val saltBytes = Base64.decode(salt, Base64.NO_WRAP)
        digest.update(saltBytes)
        val hashedBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(hashedBytes, Base64.NO_WRAP)
    }
}
