package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// DataStore delegate to persist the login session
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs") //name of the data store
// auth_prefs = We create a singleton DataStore using Kotlin property delegation.
interface AuthRepository {
    suspend fun register(username: String, password: String): Result<Unit>
    suspend fun login(username: String, password: String): Result<Unit>
    suspend fun logout(): Result<Unit>
    fun getSessionUser(): Flow<String?>
}

class AuthRepositoryImpl(
    private val userDao: UserDao,
    private val context: Context
) : AuthRepository {

    companion object {
        private val KEY_USERNAME = stringPreferencesKey("logged_in_username")
    }

    override suspend fun register(username: String, password: String): Result<Unit> {
        val trimmedUsername = username.trim()
        if (trimmedUsername.isBlank()) {
            return Result.failure(IllegalArgumentException("Username cannot be empty."))
        }
        if (password.length < 4) {
            return Result.failure(IllegalArgumentException("Password must be at least 4 characters."))
        }
        
        val existing = userDao.getUserByUsername(trimmedUsername)
        if (existing != null) {
            return Result.failure(IllegalArgumentException("Username already exists"))
        }

        val salt = SecurityUtils.generateSalt()
        val passwordHash = SecurityUtils.hashPassword(password, salt)
        val user = UserEntity(username = trimmedUsername, passwordHash = passwordHash, salt = salt)
        
        userDao.insertUser(user)
        return Result.success(Unit)
    }

    override suspend fun login(username: String, password: String): Result<Unit> {
        val trimmedUsername = username.trim()
        val user = userDao.getUserByUsername(trimmedUsername)
            ?: return Result.failure(IllegalArgumentException("Invalid username or password"))

        val computedHash = SecurityUtils.hashPassword(password, user.salt)
        if (computedHash != user.passwordHash) {
            return Result.failure(IllegalArgumentException("Invalid username or password"))
        }

        // Persist session flag
        context.dataStore.edit { preferences ->
            preferences[KEY_USERNAME] = trimmedUsername
        }

        return Result.success(Unit)
    }

    override suspend fun logout(): Result<Unit> {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_USERNAME)
        }
        return Result.success(Unit)
    }

    override fun getSessionUser(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[KEY_USERNAME]
        }
    }
}
