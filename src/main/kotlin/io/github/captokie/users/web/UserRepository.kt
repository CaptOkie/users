package io.github.captokie.users.web

import kotlinx.coroutines.flow.Flow

interface UserRepository {

    fun findAll(): Flow<User>

    suspend fun findById(id: String): User?

    suspend fun insert(user: User): User

    suspend fun update(user: User): User

    suspend fun deleteById(id: String)
}