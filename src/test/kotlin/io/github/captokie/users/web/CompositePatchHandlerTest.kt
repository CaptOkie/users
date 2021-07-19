package io.github.captokie.users.web

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
internal class CompositePatchHandlerTest {

    lateinit var handler: CompositePatchHandler<String>

    @field:Mock
    lateinit var handler1: PatchHandler<String>

    @field:Mock
    lateinit var handler2: PatchHandler<String>

    lateinit var patch: Patch

    @BeforeEach
    fun setUp() {
        handler = CompositePatchHandler(listOf(handler1, handler2))
        patch = Patch(Patch.Operation.ADD, "path")
    }

    @Test
    fun tryApply_Success_Late() {
        whenever(handler1.tryApply(patch, "target")).thenReturn(null)
        whenever(handler2.tryApply(patch, "target")).thenReturn("new_target")
        Assertions.assertEquals("new_target", handler.tryApply(patch, "target"))
    }

    @Test
    fun tryApply_Success_Early() {
        whenever(handler1.tryApply(patch, "target")).thenReturn("new_target")
        Assertions.assertEquals("new_target", handler.tryApply(patch, "target"))
        verifyZeroInteractions(handler2)
    }

    @Test
    fun tryApply_Failure() {
        whenever(handler1.tryApply(patch, "target")).thenReturn(null)
        whenever(handler2.tryApply(patch, "target")).thenReturn(null)
        Assertions.assertNull(handler.tryApply(patch, "target"))
    }
}