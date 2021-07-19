package io.github.captokie.users

import com.mongodb.reactivestreams.client.MongoClient
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

@SpringBootTest
internal class ApplicationTests {

    @field:MockBean
    lateinit var client: MongoClient

    @Test
    fun contextLoads() {
    }
}
