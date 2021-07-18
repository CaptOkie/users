package io.github.captokie.users.web

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/users", produces = [MediaType.APPLICATION_JSON_VALUE])
class UserController(
        private val repository: UserRepository,
        private val mapper: WebMapper
) {

    @GetMapping
    fun getAll(): Flow<OutboundUser> {
        return repository.findAll().map { mapper.toOutbound(it) }
    }

    @GetMapping("/{id}")
    suspend fun getOne(@PathVariable("id") id: String): OutboundUser {
        val user = repository.findById(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        return mapper.toOutbound(user)
    }

    @PostMapping
    suspend fun post(@Validated @RequestBody inboundUser: InboundUser): OutboundUser {
        var user = mapper.fromInbound(inboundUser)
        user = repository.insert(user)
        return mapper.toOutbound(user)
    }

    // TODO: Add user updating

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun delete(@PathVariable("id") id: String) {
        repository.deleteById(id)
    }
}