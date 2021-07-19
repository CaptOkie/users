package io.github.captokie.users.web

import io.github.captokie.users.data.MongoUserRepository
import io.github.captokie.users.data.Permission
import io.github.captokie.users.data.User
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.core.ParameterizedTypeReference
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@SpringBootTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
@AutoConfigureWebTestClient
internal class UserControllerTest {

    @field:MockBean
    lateinit var userRepository: MongoUserRepository

    @field:MockBean
    lateinit var passwordEncoder: PasswordEncoder

    @field:Autowired
    lateinit var client: WebTestClient

    lateinit var user: User
    lateinit var inboundUser: InboundUser
    lateinit var outboundUser: OutboundUser
    lateinit var patchRequest: PatchRequest

    @BeforeEach
    fun setUp() {
        user = User(
                id = "user_id",
                version = 1234,
                familyName = "family",
                givenName = "given",
                birthdate = LocalDate.EPOCH,
                email = "123@ab.cd",
                password = "encoded_password",
                permissions = listOf(Permission("role_name", Instant.MAX))
        )

        inboundUser = InboundUser(
                familyName = user.familyName,
                givenName = user.givenName,
                birthdate = user.birthdate,
                email = user.email,
                password = "plaintext_password",
                permissions = user.permissions.map { InboundPermission(it.type) }
        )

        outboundUser = OutboundUser(
                id = user.id!!,
                version = user.version!!,
                familyName = user.familyName,
                givenName = user.givenName,
                birthdate = user.birthdate,
                email = user.email,
                permissions = user.permissions.map { OutboundPermission(it.type, it.grantedDate) }
        )

        patchRequest = PatchRequest(
                mutableListOf(
                        Patch(Patch.Operation.ADD, InboundUser::permissions.name, mapOf(Pair(InboundPermission::type.name, "new_role"))),
                        Patch(Patch.Operation.REMOVE, InboundUser::permissions.name, mapOf(Pair(InboundPermission::type.name, "role_name")))
                )
        )
    }

    @Test
    fun getAll() {
        whenever(userRepository.findAll()).thenReturn(flow { emit(user) })
        client.get().uri("/users").exchange()
                .expectStatus().isOk
                .expectBodyList(object : ParameterizedTypeReference<OutboundUser>() {}).contains(outboundUser).hasSize(1)
    }

    @Test
    fun getOne(): Unit = runBlocking {
        whenever(userRepository.findById("not_real")).thenReturn(null)
        client.get().uri("/users/not_real").exchange()
                .expectStatus().isNotFound
                .expectBody().consumeWith {
                    Assertions.assertNotNull(it.responseBody)
                }

        whenever(userRepository.findById(outboundUser.id)).thenReturn(user)
        client.get().uri("/users/{id}", outboundUser.id).exchange()
                .expectStatus().isOk
                .expectBody<OutboundUser>().isEqualTo(outboundUser)
    }

    @Test
    fun post(): Unit = runBlocking {
        val newUser = user.copy(id = null, version = null)

        whenever(passwordEncoder.encode(inboundUser.password)).thenReturn(newUser.password)
        whenever(userRepository.save(newUser)).thenReturn(user)
        client.post().uri("/users").bodyValue(inboundUser).exchange()
                .expectStatus().isOk
                .expectBody<OutboundUser>().isEqualTo(outboundUser)

        whenever(userRepository.save(newUser)).thenThrow(DuplicateKeyException("test_exception"))
        client.post().uri("/users").bodyValue(inboundUser).exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody().consumeWith {
                    Assertions.assertNotNull(it.responseBody)
                }
    }

    @Test
    fun patch(): Unit = runBlocking {
        whenever(userRepository.findById("not_real")).thenReturn(null)
        client.patch().uri("/users/not_real").bodyValue(patchRequest).exchange()
                .expectStatus().isNotFound
                .expectBody().consumeWith {
                    Assertions.assertNotNull(it.responseBody)
                }

        var patchRequest = PatchRequest(mutableListOf(Patch(Patch.Operation.TEST, "different_path")))
        whenever(userRepository.findById(outboundUser.id)).thenReturn(user)
        client.patch().uri("/users/{id}", outboundUser.id).bodyValue(patchRequest).exchange()
                .expectStatus().isBadRequest
                .expectBody().consumeWith {
                    Assertions.assertNotNull(it.responseBody)
                }

        patchRequest = this@UserControllerTest.patchRequest
        val user = this@UserControllerTest.user.copy(permissions = listOf(Permission("new_role", Instant.MAX)))
        val outboundUser = this@UserControllerTest.outboundUser.copy(permissions = user.permissions.map { OutboundPermission(it.type, it.grantedDate) })
        whenever(userRepository.save(user)).thenReturn(user)
        client.patch().uri("/users/{id}", outboundUser.id).bodyValue(patchRequest).exchange()
                .expectStatus().isOk
                .expectBody<OutboundUser>().isEqualTo(outboundUser)
    }

    @Test
    fun delete(): Unit = runBlocking {
        client.delete().uri("/users/user_id").exchange().expectStatus().isNoContent
        verify(userRepository).deleteById("user_id")
    }

    @TestConfiguration
    class Config {

        @Bean
        fun clock(): Clock = Clock.fixed(Instant.MAX, ZoneOffset.UTC)
    }
}
