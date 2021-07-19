package io.github.captokie.users.web

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import io.github.captokie.users.web.Patch.Operation
import org.springframework.web.server.ResponseStatusException
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

/**
 * Represents a single JSON patch operation
 *
 * @property operation The type of patch [Operation] to perform
 * @property path The path to attribute to apply the operation to
 * @property value The optional value to use in the operation, or `null` if it isn't needed
 */
data class Patch(
        @field:NotNull
        @JsonProperty("op")
        val operation: Operation,
        @field:NotBlank
        @field:Pattern(regexp = "^\\w{1,100}$")
        val path: String,
        val value: Any? = null
) {

    /**
     * A JSON patch operation. See [RFC 6902](https://datatracker.ietf.org/doc/html/rfc6902#section-4) for more info.
     */
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

/**
 * A complete patch request. This holds all the individual [Patch] items.
 *
 * Note: This is also necessary to allow for proper Spring validation, since there is issues with it validating
 * top-level lists that. See [this post](https://stackoverflow.com/questions/28150405/validation-of-a-list-of-objects-in-spring)
 * for more information.
 *
 * @property patches The actual list of [Patch] items. All list operations are delegated to this value. This must be
 *                   public for validation to work.
 */
data class PatchRequest @JsonCreator constructor(
        @field:Valid @field:JsonValue val patches: MutableList<Patch>
) : MutableList<Patch> by patches {

    /**
     * Creates an empty patch request. Primarily needed for Jackson to perform deserialization properly.
     */
    constructor() : this(mutableListOf())
}

/**
 * The component responsible for handling a single [Patch] item.
 *
 * @param T The type of item this handler processes
 */
interface PatchHandler<T> {

    /**
     * Attempts to apply the [patch] to the provided [target]. If this is a [Patch] supported by this handler then this
     * either:
     * - returns the modified [target] with the [patch] applied
     * - throws a [ResponseStatusException] to indicate an issue with the [patch]
     *
     * If this doesn't support the [patch] then `null` is returned.
     *
     * @param patch The [Patch] to apply to the [target]
     * @param target The item being patched
     * @return The updated [target] if the patch was handler, otherwise `null`
     */
    fun tryApply(patch: Patch, target: T): T?
}

/**
 * An implementation of [PatchHandler] that combines multiple handlers. Each handler is attempted in order. Once a
 * handler successfully handles a [Patch] operation, no further handlers are attempted.
 *
 * @param handlers The list of [PatchHandler]s to delegate to
 */
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
