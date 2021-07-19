package io.github.captokie.users.web

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.time.LocalDate

data class OutboundPermission(
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
        val permissions: List<OutboundPermission> = emptyList()
)

data class InboundPermission(
        val type: String
)

data class InboundUser(
        val familyName: String,
        val givenName: String,
        val birthdate: LocalDate,
        val email: String,
        @Schema(format = "password")
        val password: String,
        val permissions: List<InboundPermission> = emptyList()
)
