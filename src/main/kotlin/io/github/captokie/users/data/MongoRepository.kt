package io.github.captokie.users.data

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import org.springframework.util.Assert

/**
 * The Spring Data repository interface that will handle the Mongo operations.
 */
@Repository
interface MongoUserRepository : CoroutineCrudRepository<User, String> {
}

/**
 * An implementation of [UserRepository] that uses Spring Data Mongo to manage [User] entries.
 */
@Repository
class SpringDataMongoUserRepository(
        private val userRepository: MongoUserRepository
) : UserRepository {

    override fun findAll(): Flow<User> {
        return userRepository.findAll()
    }

    override suspend fun findById(id: String): User? {
        return userRepository.findById(id)
    }

    override suspend fun insert(user: User): User {
        Assert.isNull(user.id, "ID must be null")
        Assert.isNull(user.version, "Version must be null")

        return userRepository.save(user)
    }

    override suspend fun update(user: User): User {
        Assert.hasLength(user.id, "ID must be set")
        Assert.notNull(user.version, "Version must be set")

        return userRepository.save(user)
    }

    override suspend fun deleteById(id: String) {
        userRepository.deleteById(id)
    }
}