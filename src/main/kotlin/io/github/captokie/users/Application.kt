package io.github.captokie.users

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.DelegatingPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Clock

@SpringBootApplication
@OpenAPIDefinition(info = Info(title = "Users", version = "1.0.0"))
class Application {

    @Bean
    fun clock(): Clock = Clock.systemUTC()

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        // Wrap the bcrypt encoder in a delegating encoder to allow for switching encoders in the future

        val defaultEncoder = "bcrypt"
        val encoders = mutableMapOf<String, PasswordEncoder>()
        encoders[defaultEncoder] = BCryptPasswordEncoder()
        return DelegatingPasswordEncoder(defaultEncoder, encoders)
    }
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
