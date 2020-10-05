package fr.ktorVoter

import org.amshove.kluent.`should be`
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class VoteTest {

    @Test
    fun isGranted() {
        assertEquals(Vote.toVote { true }, Vote.GRANTED)
    }

    @Test
    fun isDenied() {
        assertEquals(Vote.toVote { false }, Vote.DENIED)
    }

    @Test
    fun isAbstain() {
        assertEquals(Vote.toVote { null }, Vote.ABSTAIN)
    }

    @Test
    fun toBoolNull() {
        Vote.ABSTAIN.toBool() `should be` null
    }

    @Test
    fun toBoolTrue() {
        Vote.GRANTED.toBool() `should be` true
    }

    @Test
    fun toBoolFalse() {
        Vote.DENIED.toBool() `should be` false
    }
}