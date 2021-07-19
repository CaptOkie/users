package io.github.captokie.users.web

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import io.github.captokie.users.data.User
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.Validator
import org.springframework.web.server.ResponseStatusException
import java.time.Clock
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

data class Patch(
        @field:NotNull
        @JsonProperty("op")
        val operation: Operation,
        @field:NotBlank
        @field:Pattern(regexp = "^\\w+$")
        val path: String,
        val value: Any?
) {

    enum class Operation {
        @JsonProperty("add")
        ADD,

        @JsonProperty("remove")
        REMOVE,

        @JsonProperty("replace")
        REPLACE,

        @JsonProperty("move")
        MOVE,

        @JsonProperty("copy")
        COPY,

        @JsonProperty("test")
        TEST,
    }
}

data class PatchRequest @JsonCreator constructor(
        @field:Valid @field:JsonValue val patches: MutableList<Patch>
) : MutableList<Patch> by patches {

    constructor() : this(mutableListOf())
}

interface PatchHandler<T> {

    fun tryApply(patch: Patch, target: T): T?
}

class CompositePatchHandler<T>(
        private val handlers: List<PatchHandler<T>>
) : PatchHandler<T> {

    override fun tryApply(patch: Patch, target: T): T? {
        for (handler in handlers) {
            val newTarget = handler.tryApply(patch, target)
            if (newTarget != null) {
                return newTarget
            }
        }
        return null
    }
}

private object JsonPointers {
    val permissions = InboundUser::permissions.name
}

@Service
class AddUserPermissionPatchHandler(
        private val objectMapper: ObjectMapper,
        private val validator: Validator,
        private val clock: Clock,
        private val userMapper: WebUserMapper
) : PatchHandler<User> {

    override fun tryApply(patch: Patch, target: User): User? {
        if (patch.operation != Patch.Operation.ADD || patch.path != JsonPointers.permissions) {
            return null
        }

        val rawValue = patch.value ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        val value: InboundPermission = objectMapper.convertValue(rawValue)

        val errors = BeanPropertyBindingResult(value, "permission")
        validator.validate(value, errors)
        if (errors.hasErrors()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }

        if (target.permissions.any { it.type == value.type }) {
            // From an API consumer point of view, it is nicer if adding a permission that exists doesn't cause an error
            return target
        }
        val permissions = target.permissions + userMapper.fromInbound(value, clock.instant())
        return target.copy(permissions = permissions)
    }
}

@Service
class RemoveUserPermissionPatchHandler(
        private val objectMapper: ObjectMapper,
        private val validator: Validator
) : PatchHandler<User> {

    override fun tryApply(patch: Patch, target: User): User? {
        if (patch.operation != Patch.Operation.REMOVE || patch.path != JsonPointers.permissions) {
            return null
        }

        val rawValue = patch.value ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        val value: InboundPermission = objectMapper.convertValue(rawValue)

        val errors = BeanPropertyBindingResult(value, "permission")
        validator.validate(value, errors)
        if (errors.hasErrors()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }

        val permissions = target.permissions.filter { it.type != value.type }
        if (permissions.size == target.permissions.size) {
            // From an API consumer point of view, it is nicer if removing a permission that doesn't exist doesn't cause
            // an error
            return target
        }
        return target.copy(permissions = permissions)
    }
}