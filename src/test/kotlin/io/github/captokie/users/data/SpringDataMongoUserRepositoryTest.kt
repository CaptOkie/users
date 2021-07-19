package io.github.captokie.users.data

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
internal class SpringDataMongoUserRepositoryTest {

    lateinit var repository: SpringDataMongoUserRepository

    @field:Mock
    lateinit var userRepository: MongoUserRepository
    lateinit var user: User

    @BeforeEach
    fun setUp() {
        repository = SpringDataMongoUserRepository(userRepository)
        user = User(
                id = "user_id",
                version = 1234,
                familyName = "family",
                givenName = "given",
                birthdate = LocalDate.EPOCH,
                email = "123@ab.cd",
                password = "shhh....",
                permissions = listOf(
                        Permission(
                                type = "a_role",
                                grantedDate = Instant.EPOCH
                        )
                )
        )
    }

    @Test
    fun findAll(): Unit = runBlocking {
        val users = flow { emit(user) }
        whenever(userRepository.findAll()).thenReturn(users)
        Assertions.assertEquals(listOf(user), repository.findAll().toList())
    }

    @Test
    fun findById(): Unit = runBlocking {
        whenever(userRepository.findById("not_real")).thenReturn(null)
        Assertions.assertNull(repository.findById("not_real"))

        whenever(userRepository.findById(user.id!!)).thenReturn(user)
        Assertions.assertEquals(user, repository.findById(user.id!!))
    }

    @Test
    fun insert(): Unit = runBlocking {
        val newUser = user.copy(id = null, version = null)
        whenever(userRepository.save(newUser)).thenReturn(user)
        Assertions.assertEquals(user, repository.insert(newUser))
    }

    @Test
    fun update(): Unit = runBlocking {
        val updatedUser = user.copy(version = 4321)
        whenever(userRepository.save(user)).thenReturn(updatedUser)
        Assertions.assertEquals(updatedUser, repository.update(user))
    }

    @Test
    fun deleteById(): Unit = runBlocking {
        repository.deleteById(user.id!!)
        verify(userRepository).deleteById(user.id!!)
    }
}