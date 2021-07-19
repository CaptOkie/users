package io.github.captokie.users.web

import io.github.captokie.users.data.Permission
import io.github.captokie.users.data.User
import io.swagger.v3.oas.annotations.media.Schema
import org.mapstruct.Context
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import java.time.Instant
import java.time.LocalDate
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.PastOrPresent

/**
 * The permission information that is exposed when returning data from the API.
 *
 * @property type The identifier of the permission. i.e. This is the value that will be checked when authorizing access
 *                to a particular resource.
 * @property grantedDate The timestamp at which the permission was granted to a [User].
 * @see Permission
 */
data class OutboundPermission(
        val type: String,
        val grantedDate: Instant
)

/**
 * The user information that is exposed when returning data from the API.
 *
 * @property id The ID assigned to the user by the system itself
 * @property version The value to track modifications to an entry. This is used to catch concurrent modifications.
 * @property familyName The family name of the individual
 * @property givenName The given name of the individual
 * @property birthdate A local date without any timezone information. Generally timezones aren't considered when
 *                     determining a person birthdate.
 * @property email The email used by a individual. Each [User] must have a unique email address.
 * @property permissions The list of permissions granted to an individual.
 * @see User
 */
data class OutboundUser(
        val id: String,
        val version: Long,
        val familyName: String,
        val givenName: String,
        val birthdate: LocalDate,
        val email: String,
        val permissions: List<OutboundPermission> = emptyList()
)

/**
 * The permission information that is accepted when receiving data from the API.
 *
 * @property type The identifier of the permission. i.e. This is the value that will be checked when authorizing access
 *                to a particular resource.
 * @see Permission
 */
data class InboundPermission(
        @field:StrictString
        val type: String
)

/**
 * The user information that is accepted when receiving data from the API.
 *
 * @property familyName The family name of the individual
 * @property givenName The given name of the individual
 * @property birthdate A local date without any timezone information. Generally timezones aren't considered when
 *                     determining a person birthdate.
 * @property email The email used by a individual. Each [User] must have a unique email address.
 * @property password The plaintext password of the user.
 * @property permissions The list of permissions granted to an individual.
 * @see User
 */
data class InboundUser(
        @field:StrictString
        val familyName: String,
        @field:StrictString
        val givenName: String,
        @field:NotNull
        @field:PastOrPresent
        val birthdate: LocalDate,
        @field:NotBlank
        @field:Email
        val email: String,
        @field:StrictString
        @Schema(format = "password")
        val password: String,
        @field:Valid
        val permissions: List<InboundPermission> = emptyList()
)

/**
 * The contract for mapping between the external API and the internal models.
 */
@Mapper
interface WebUserMapper {

    /**
     * Maps from an internal [User] to an external [OutboundUser]
     *
     * @param user The user to map
     */
    fun fromUser(user: User): OutboundUser

    /**
     * Maps from an internal [Permission] to an external [OutboundPermission]
     *
     * @param permission The permission to map
     */
    fun fromPermission(permission: Permission): OutboundPermission

    /**
     * Maps from an external [InboundUser] to an internal [User].
     *
     * @param user The user to map
     * @param grantedDate The [Instant] to assign as the [Permission.grantedDate] for all permissions in the user.
     */
    fun toUser(user: InboundUser, @Context grantedDate: Instant): User

    /**
     * Maps from an external [InboundPermission] to an internal [Permission].
     *
     * @param permission The permission to map
     * @param grantedDate The [Instant] to assign as the [Permission.grantedDate] for the mapped permission.
     */
    @Mapping(target = "grantedDate", expression = "java(grantedDate)")
    fun toPermission(permission: InboundPermission, @Context grantedDate: Instant): Permission
}