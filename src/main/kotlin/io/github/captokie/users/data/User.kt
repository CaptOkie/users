package io.github.captokie.users.data

import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate

data class Permission(
        val type: String,
        val grantedDate: Instant
)

data class User(
        val id: String,
        val version: Long,
        val familyName: String,
        val givenName: String,
        val birthdate: LocalDate,
        val email: String,
        val password: String,
        val permissions: List<Permission>
)

data class NewUser(
        val familyName: String,
        val givenName: String,
        val birthdate: LocalDate,
        val email: String,
        val password: String,
        val permissions: List<Permission>
) {

    fun toUser(id: String, version: Long) =
            User(id, version, familyName, givenName, birthdate, email, password, permissions)
}

interface UserRepository {

    fun findAll(): Flow<User>

    suspend fun findById(id: String): User?

    suspend fun insert(newUser: NewUser): User

    suspend fun update(updatedUser: User): User

    suspend fun deleteById(id: String)
}