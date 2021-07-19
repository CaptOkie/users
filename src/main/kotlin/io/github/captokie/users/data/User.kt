package io.github.captokie.users.data

import kotlinx.coroutines.flow.Flow
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.time.LocalDate

/**
 * Represents a single permission that can be granted to a [User].
 *
 * @property type The identifier of the permission. i.e. This is the value that will be checked when authorizing access
 *                to a particular resource.
 * @property grantedDate The timestamp at which the permission was granted to a [User].
 */
data class Permission(
        val type: String,
        val grantedDate: Instant
)

/**
 * Represents a single individual.
 *
 * @property id The ID assigned to the user by the system itself. If an ID hasn't been assigned yet, then the user has
 *              not yet been created
 * @property version The value to track modifications to an entry. This is used to catch concurrent modifications.
 * @property familyName The family name of the individual
 * @property givenName The given name of the individual
 * @property birthdate A local date without any timezone information. Generally timezones aren't considered when
 *                     determining a person birthdate.
 * @property email The email used by a individual. Each [User] must have a unique email address.
 * @property password The encoded password of the user. The precise encoding used is outside the scope of this entity.
 * @property permissions The list of permissions granted to an individual.
 */
@Document("users")
data class User(
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
        val permissions: List<Permission>
)

/**
 * The contract for managing [User] entities.
 */
interface UserRepository {

    /**
     * Returns all the users that have been saved to the system
     */
    fun findAll(): Flow<User>

    /**
     * Attempt to find a single user by the [User.id].
     *
     * @param id The ID to use to lookup the user
     */
    suspend fun findById(id: String): User?

    /**
     * Creates a new user. Both [User.id] and [User.version] must be `null`.
     *
     * @param user The user to insert into the repository
     */
    suspend fun insert(user: User): User

    /**
     * Replaces an existing user with a new user. Both [User.id] and [User.version] must not be `null`.
     *
     * @param user The user to replace the existing user
     */
    suspend fun update(user: User): User

    /**
     * Deletes an user by their [User.id] value. If the user doesn't already exist, then the operation will still be
     * treated as successful.
     *
     * @param id The ID of the user to delete
     */
    suspend fun deleteById(id: String)
}