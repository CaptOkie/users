package io.github.captokie.users.web

import io.github.captokie.users.data.User
import io.github.captokie.users.data.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.Clock

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
            patchHandlers: List<PatchHandler<User>>?
    ) : this(repository, userMapper, clock, passwordEncoder, CompositePatchHandler(patchHandlers ?: emptyList()))

    @GetMapping
    fun getAll(): Flow<OutboundUser> {
        return repository.findAll().map { userMapper.toOutbound(it) }
    }

    @GetMapping("/{id}")
    suspend fun getOne(@PathVariable("id") id: String): OutboundUser {
        val user = repository.findById(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        return userMapper.toOutbound(user)
    }

    @PostMapping
    suspend fun post(@Validated @RequestBody inboundUser: InboundUser): OutboundUser {
        val grantedDate = clock.instant()
        var newUser = userMapper.fromInbound(inboundUser, grantedDate)
        newUser = newUser.copy(password = passwordEncoder.encode(newUser.password))
        val user = repository.insert(newUser)
        return userMapper.toOutbound(user)
    }

    @PatchMapping("/{id}")
    suspend fun patch(@PathVariable("id") id: String, @Validated @RequestBody patches: List<Patch>): OutboundUser {
        var user = repository.findById(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        for (patch in patches) {
            user = patchHandler.tryApply(patch, user) ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST)
        }
        user = repository.update(user)
        return userMapper.toOutbound(user)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun delete(@PathVariable("id") id: String) {
        repository.deleteById(id)
    }
}