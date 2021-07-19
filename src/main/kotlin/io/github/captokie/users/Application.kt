package io.github.captokie.users

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.util.IdGenerator
import org.springframework.util.JdkIdGenerator
import java.time.Clock

@SpringBootApplication
class Application {

    @Bean
    fun clock(): Clock = Clock.systemUTC()

    @Bean
    fun idGenerator(): IdGenerator = JdkIdGenerator()

    @Bean
    fun passwordEncoder(): PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
