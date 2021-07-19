package io.github.captokie.users.web

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonPointer

data class Patch(
        @JsonProperty("op")
        val operation: Operation,
        val path: JsonPointer,
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
