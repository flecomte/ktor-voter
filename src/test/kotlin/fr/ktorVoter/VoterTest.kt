package fr.ktorVoter

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertFailsWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class VoterTest {
    private val voters = listOf(VoterImplement<Any, Any>())

    @Test
    fun `test Voter GRANTED`() {
        assertTrue(voters.can(
            action = VoterImplement.Action.CREATE,
            subject = Subject(role = Subject.Role.ADMIN)
        ))
    }

    @Test
    fun `test Voter DENIED`() {
        assertFalse(voters.can(
            action = VoterImplement.Action.CREATE,
            subject = Subject(role = Subject.Role.USER)
        ))
    }

    @Test
    fun `test Voter DENIED if all ABSTAIN`() {
        assertFalse(voters.can(VoterImplement.Action.CREATE))
    }

    @Test
    fun `test No Voter`() {
        assertFailsWith<NoVoterException> {
            voters.can(FakeAction.FAKE)
        }
    }

    enum class FakeAction : ActionI {
        FAKE
    }
}