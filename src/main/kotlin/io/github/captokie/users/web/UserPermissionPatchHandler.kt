package io.github.captokie.users.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import io.github.captokie.users.data.User
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.Validator
import org.springframework.web.server.ResponseStatusException
import java.time.Clock

/**
 * All the JSON paths used in the [PatchHandler]s
 */
private object JsonPaths {
    /**
     * This path references the user's list of permissions
     */
    val permissions = InboundUser::permissions.name
}

/**
 * An implementation of [PatchHandler] that handles adding and removing new permissions to a user. If the permission
 * being added already exists on the user then this handler will still succeed. If the permission being removed doesn't
 * exist on the user then this handler will still succeed.
 */
@Service
class UserPermissionPatchHandler(
        private val objectMapper: ObjectMapper,
        private val validator: Validator,
        private val clock: Clock,
        private val userMapper: WebUserMapper
) : PatchHandler<User> {

    override fun tryApply(patch: Patch, target: User): User? {
        if ((patch.operation != Patch.Operation.ADD && patch.operation != Patch.Operation.REMOVE)
                || patch.path != JsonPaths.permissions) {
            return null
        }

        val rawValue = patch.value ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        val value: InboundPermission = objectMapper.convertValue(rawValue)

        val errors = BeanPropertyBindingResult(value, "permission")
        validator.validate(value, errors)
        if (errors.hasErrors()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }

        return when (patch.operation) {
            Patch.Operation.ADD -> doAdd(target, value)
            Patch.Operation.REMOVE -> doRemove(target, value)
            else -> throw IllegalStateException("Invalid operation: ${patch.operation}")
        }
    }

    private fun doAdd(user: User, permission: InboundPermission): User {

        if (user.permissions.any { it.type == permission.type }) {
            // From an API consumer point of view, it is nicer if adding a permission that exists doesn't cause an error
            return user
        }
        val permissions = user.permissions + userMapper.toPermission(permission, clock.instant())
        return user.copy(permissions = permissions)
    }

    private fun doRemove(user: User, permission: InboundPermission): User {

        val permissions = user.permissions.filter { it.type != permission.type }
        if (permissions.size == user.permissions.size) {
            // From an API consumer point of view, it is nicer if removing a permission that doesn't exist doesn't cause
            // an error
            return user
        }
        return user.copy(permissions = permissions)
    }
}