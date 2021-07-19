package io.github.captokie.users.web

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.captokie.users.data.Permission
import io.github.captokie.users.data.User
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.validation.Errors
import org.springframework.validation.Validator
import org.springframework.web.server.ResponseStatusException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@ExtendWith(MockitoExtension::class)
internal class UserPermissionPatchHandlerTest {

    lateinit var handler: UserPermissionPatchHandler
    lateinit var objectMapper: ObjectMapper

    @field:Mock
    lateinit var validator: Validator
    lateinit var clock: Clock

    @field:Mock
    lateinit var userMapper: WebUserMapper
    lateinit var patch: Patch
    lateinit var user: User

    @BeforeEach
    fun setUp() {
        objectMapper = Jackson2ObjectMapperBuilder.json().build()
        clock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC)
        handler = UserPermissionPatchHandler(objectMapper, validator, clock, userMapper)
        patch = Patch(
                Patch.Operation.ADD,
                InboundUser::permissions.name,
                mapOf(Pair(InboundPermission::type.name, "role_name_2"))
        )
        user = User(
                id = "user_id",
                version = 1234,
                familyName = "family",
                givenName = "given",
                birthdate = LocalDate.EPOCH,
                email = "123@ab.cd",
                password = "shhh...",
                permissions = listOf(Permission("role_name_1", Instant.EPOCH))
        )
    }

    @Test
    fun tryApply_Unsupported() {
        var patch = this.patch.copy(operation = Patch.Operation.COPY)
        Assertions.assertNull(handler.tryApply(patch, user))

        patch = this.patch.copy(path = "different_path")
        Assertions.assertNull(handler.tryApply(patch, user))

        patch = patch.copy(operation = Patch.Operation.REMOVE)
        Assertions.assertNull(handler.tryApply(patch, user))
    }

    @Test
    fun tryApply_Error() {
        var patch = this.patch.copy(value = null)
        assertThrows<ResponseStatusException> {
            handler.tryApply(patch, user)
        }

        patch = this.patch
        whenever(validator.validate(any<InboundPermission>(), any())).then {
            val errors: Errors = it.getArgument(1)
            errors.reject("error.code")
            null
        }
        assertThrows<ResponseStatusException> {
            handler.tryApply(patch, user)
        }
    }

    @Test
    fun tryApply_Add() {
        val newPermission = Permission("role_name_2", Instant.EPOCH)
        whenever(userMapper.toPermission(InboundPermission("role_name_2"), Instant.EPOCH)).thenReturn(newPermission)

        val actual = handler.tryApply(patch, user)
        Assertions.assertNotNull(actual)
        val newPermissions = user.permissions + newPermission
        Assertions.assertEquals(newPermissions, actual!!.permissions)

        // Check that duplicates are handled
        Assertions.assertEquals(actual, handler.tryApply(patch, user))
    }

    @Test
    fun tryApply_Remove() {
        val patch = this.patch.copy(operation = Patch.Operation.REMOVE)

        var actual = handler.tryApply(patch, user)
        Assertions.assertNotNull(actual)
        Assertions.assertEquals(user.permissions, actual!!.permissions)

        val user = this.user.copy(permissions = listOf(Permission("role_name_2", Instant.EPOCH)))
        actual = handler.tryApply(patch, user)
        Assertions.assertNotNull(actual)
        Assertions.assertEquals(emptyList<User>(), actual!!.permissions)
    }
}