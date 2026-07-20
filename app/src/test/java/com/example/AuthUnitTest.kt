package com.example

import com.example.data.SecurityUtils
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AuthUnitTest {

    @Test
    fun testGenerateSalt_isUniqueAndNotEmpty() {
        val salt1 = SecurityUtils.generateSalt()
        val salt2 = SecurityUtils.generateSalt()
        
        assertNotNull(salt1)
        assertNotNull(salt2)
        assertTrue(salt1.isNotEmpty())
        assertTrue(salt2.isNotEmpty())
        assertNotEquals(salt1, salt2)
    }

    @Test
    fun testHashPassword_isConsistentAndSecure() {
        val password = "my_secure_password"
        val salt = SecurityUtils.generateSalt()
        
        val hash1 = SecurityUtils.hashPassword(password, salt)
        val hash2 = SecurityUtils.hashPassword(password, salt)
        
        // Hashing is deterministic for the same password + salt
        assertEquals(hash1, hash2)
        
        // Different passwords produce different hashes with the same salt
        val differentHash = SecurityUtils.hashPassword("another_password", salt)
        assertNotEquals(hash1, differentHash)
        
        // Different salts produce different hashes for the same password
        val differentSalt = SecurityUtils.generateSalt()
        val differentSaltHash = SecurityUtils.hashPassword(password, differentSalt)
        assertNotEquals(hash1, differentSaltHash)
        
        // The hashed output must be secure and must never contain plain text
        assertFalse(hash1.contains(password))
    }
}
