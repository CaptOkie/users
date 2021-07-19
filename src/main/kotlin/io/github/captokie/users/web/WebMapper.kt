package io.github.captokie.users.web

import io.github.captokie.users.data.NewUser
import io.github.captokie.users.data.Permission
import io.github.captokie.users.data.User
import org.mapstruct.Context
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import java.time.Instant

@Mapper
interface WebMapper {

    fun toOutbound(user: User): OutboundUser

    fun fromInbound(user: InboundUser, @Context grantedDate: Instant): NewUser

    @Mapping(target = "grantedDate", expression = "java(grantedDate)")
    fun fromInbound(permission: InboundPermission, @Context grantedDate: Instant): Permission
}