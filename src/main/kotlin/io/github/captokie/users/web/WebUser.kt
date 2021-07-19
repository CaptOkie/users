package io.github.captokie.users.web

import io.github.captokie.users.data.NewUser
import io.github.captokie.users.data.Permission
import io.github.captokie.users.data.User
import io.swagger.v3.oas.annotations.media.Schema
import org.mapstruct.Context
import org.mapstruct.Mapper
import org.mapstruct.Mapping
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

@Mapper
interface WebUserMapper {

    fun toOutbound(user: User): OutboundUser

    fun toOutbound(permission: Permission): OutboundPermission

    fun fromInbound(user: InboundUser, @Context grantedDate: Instant): NewUser

    @Mapping(target = "grantedDate", expression = "java(grantedDate)")
    fun fromInbound(permission: InboundPermission, @Context grantedDate: Instant): Permission
}