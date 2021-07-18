package io.github.captokie.users.web

import java.time.Instant
import java.time.LocalDate

data class User(
        val id: String?,
        val version: String?,
        val familyName: String,
        val givenName: String,
        val birthdate: LocalDate,
        val email: String,
        val password: String,
        val permissions: List<Permission> = emptyList()
)

data class Permission(
        val type: String,
        val grantedDate: Instant
)

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
        val permissions: List<Permission> = emptyList()
)