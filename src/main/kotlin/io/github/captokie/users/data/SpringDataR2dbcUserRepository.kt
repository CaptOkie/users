package io.github.captokie.users.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Repository
import org.springframework.util.IdGenerator
import java.util.concurrent.ConcurrentHashMap

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

    override suspend fun insert(newUser: NewUser): User {
        val id = idGenerator.generateId().toString()
        val version = idGenerator.generateId().toString()
        val user = newUser.toUser(id, version)
        users[id] = user
        return user
    }

    override suspend fun update(user: User): User {
        return users.computeIfPresent(user.id) { _, oldUser ->
            if (user.version != oldUser.version) {
                throw OptimisticLockingFailureException("expected version: ${oldUser.version}, actual version: ${user.version}")
            }
            user.copy(version = idGenerator.generateId().toString())
        } ?: throw OptimisticLockingFailureException("No user found for id: ${user.id}")
    }

    override suspend fun deleteById(id: String) {
        users.remove(id)
    }
}