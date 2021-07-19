package io.github.captokie.users.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MongoUserRepository : CoroutineCrudRepository<MongoUser, String> {
}

@Repository
class SpringDataMongoUserRepository(
        private val userRepository: MongoUserRepository,
        private val userMapper: MongoUserMapper
) : UserRepository {

    override fun findAll(): Flow<User> {
        return userRepository.findAll().map { userMapper.toUser(it) }
    }

    override suspend fun findById(id: String): User? {
        val user = userRepository.findById(id) ?: return null
        return userMapper.toUser(user)
    }

    override suspend fun insert(newUser: NewUser): User {
        var user = userMapper.fromUser(newUser)
        user = userRepository.save(user)
        return userMapper.toUser(user)
    }

    override suspend fun update(updatedUser: User): User {
        var user = userMapper.fromUser(updatedUser)
        user = userRepository.save(user)
        return userMapper.toUser(user)
    }

    override suspend fun deleteById(id: String) {
        userRepository.deleteById(id)
    }
}