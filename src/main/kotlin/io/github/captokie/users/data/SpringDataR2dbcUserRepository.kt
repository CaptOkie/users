package io.github.captokie.users.data

import io.github.captokie.users.web.User
import io.github.captokie.users.web.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.dao.DataRetrievalFailureException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Repository
import org.springframework.util.IdGenerator
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@Repository
class SpringDataR2dbcUserRepository(
        private val idGenerator: IdGenerator
) : UserRepository {

    private val users: MutableMap<String, User> = ConcurrentHashMap()

    override fun findAll(): Flow<User> = flow {
        for (user in users.values) {
            emit(user)
        }
    }

    override suspend fun findById(id: String): User? {
        return users[id]
    }

    override suspend fun insert(user: User): User {
        val id = idGenerator.generateId().toString()
        val version = idGenerator.generateId().toString()
        val fullUser = user.copy(id = id, version = version)
        users[id] = fullUser
        return fullUser
    }

    override suspend fun update(user: User): User {
        val id = user.id ?: throw IllegalArgumentException("ID must be set")
        val version = user.version ?: throw IllegalArgumentException("ID must be set")

        return users.computeIfPresent(id) { _, oldUser ->
            if (version != oldUser.version) {
                throw OptimisticLockingFailureException("expected version: ${oldUser.version}, actual version: $version")
            }
            user.copy(version = idGenerator.generateId().toString())
        } ?: throw OptimisticLockingFailureException("No user found for id: $id")
    }

    override suspend fun deleteById(id: String) {
        users.remove(id)
    }
}