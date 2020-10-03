package fr.ktorVoter

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class VoteTest {

    @Test
    fun isGranted() {
        assertEquals(Vote.isGranted { true }, Vote.GRANTED)
    }

    @Test
    fun isDenied() {
        assertEquals(Vote.isGranted { false }, Vote.DENIED)
    }

    @Test
    fun isAbstain() {
        assertEquals(Vote.isGranted { null }, Vote.ABSTAIN)
    }
}