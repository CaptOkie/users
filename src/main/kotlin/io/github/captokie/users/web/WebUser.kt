package io.github.captokie.users.web

import io.github.captokie.users.data.Permission
import java.time.LocalDate

data class OutboundUser(
        val id: String,
        val version: String,
        val familyName: String,
        val givenName: String,
        val birthdate: LocalDate,
        val email: String,
        val permissions: List<Permission> = emptyList()
)

data class InboundUser(
        val familyName: String,
        val givenName: String,
        val birthdate: LocalDate,
        val email: String,
        val password: String,
        val permissions: List<InboundPermission> = emptyList()
)

data class InboundPermission(val type: String)