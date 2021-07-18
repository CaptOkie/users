package io.github.captokie.users.web

import org.mapstruct.Mapper

@Mapper
interface WebMapper {

    fun toOutbound(user: User): OutboundUser

    fun fromInbound(user: InboundUser): User
}