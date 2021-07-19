package io.github.captokie.users.web

import io.github.captokie.users.data.User
import io.github.captokie.users.data.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.Clock

/**
 * The Spring controller responsible for handling requests for [User] items
 */
@RestController
@RequestMapping("/users", produces = [MediaType.APPLICATION_JSON_VALUE])
class UserController(
        private val repository: UserRepository,
        private val userMapper: WebUserMapper,
        private val clock: Clock,
        private val passwordEncoder: PasswordEncoder,
        private val patchHandler: PatchHandler<User>
) {

    @Autowired
    constructor(
            repository: UserRepository,
            userMapper: WebUserMapper,
            clock: Clock,
            passwordEncoder: PasswordEncoder,
            patchHandlers: List<PatchHandler<User>> = emptyList()
    ) : this(repository, userMapper, clock, passwordEncoder, CompositePatchHandler(patchHandlers))

    /**
     * Lists all of the users that exist
     */
    @GetMapping
    fun getAll(): Flow<OutboundUser> {
        return repository.findAll().map { userMapper.fromUser(it) }
    }

    /**
     * Attempts to retrieve a specific user by their [id]
     *
     * @param id The ID of the user to lookup
     * @throws ResponseStatusException If no user is found
     */
    @GetMapping("/{id}")
    suspend fun getOne(@PathVariable("id") id: String): OutboundUser {
        val user = repository.findById(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        return userMapper.fromUser(user)
    }

    /**
     * Creates a new user based on the requested [inboundUser]
     *
     * @param inboundUser The information about the new user
     */
    @PostMapping
    suspend fun post(@Validated @RequestBody inboundUser: InboundUser): OutboundUser {
        val grantedDate = clock.instant()
        var newUser = userMapper.toUser(inboundUser, grantedDate)
        newUser = newUser.copy(password = passwordEncoder.encode(newUser.password))
        val user = repository.insert(newUser)
        return userMapper.fromUser(user)
    }

    /**
     * Applies a list of patch operations to the user with the specific [id]
     *
     * @param id The ID of the user to patch
     * @param request The [PatchRequest] with all the patch operations
     * @throws ResponseStatusException If no user is found, or a patch couldn't be applied
     */
    @PatchMapping("/{id}")
    suspend fun patch(@PathVariable("id") id: String, @Validated @RequestBody request: PatchRequest): OutboundUser {
        var user = repository.findById(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        for (patch in request.patches) {
            user = patchHandler.tryApply(patch, user) ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }
        user = repository.update(user)
        return userMapper.fromUser(user)
    }

    /**
     * Deletes a user with the specified [id]. This will always succeed, even if the user doesn't exist.
     *
     * @param id The ID of the user to delete
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun delete(@PathVariable("id") id: String) {
        repository.deleteById(id)
    }
}

/**
 * The controller advice for the [UserController]
 */
@ControllerAdvice
class UserControllerAdvice {

    /**
     * Converts a [DuplicateKeyException] into a [ResponseStatusException] with the appropriate response status. This
     * will always throw a [ResponseStatusException].
     */
    @ExceptionHandler
    suspend fun handle(e: DuplicateKeyException) {
        throw ResponseStatusException(HttpStatus.CONFLICT, null, e)
    }
}