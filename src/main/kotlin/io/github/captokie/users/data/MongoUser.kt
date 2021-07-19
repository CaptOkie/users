package io.github.captokie.users.data

import org.mapstruct.Mapper
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.time.LocalDate

data class MongoPermission(
        val type: String,
        val grantedDate: Instant
)

@Document("users")
data class MongoUser(
        @field:Id
        val id: String?,
        @field:Version
        val version: Long?,
        val familyName: String,
        val givenName: String,
        val birthdate: LocalDate,
        @field:Indexed(unique = true)
        val email: String,
        val password: String,
        val permissions: List<MongoPermission>
)

@Mapper
interface MongoUserMapper {

    fun toUser(user: MongoUser): User

    fun toPermission(permission: MongoPermission): Permission

    fun fromUser(user: NewUser): MongoUser

    fun fromUser(user: User): MongoUser

    fun fromPermission(permission: Permission): MongoPermission
}
