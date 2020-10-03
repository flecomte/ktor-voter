package fr.ktorVoter

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertFailsWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CheckClassTest {
    private val voters = listOf(VoterImplement<Any, Any>())

    @Test
    fun `test checkClass extension fail`() {
        assertFailsWith<WrongClassException> {
            VoterImplement<Any, Any>()
                .checkClass(String::class, Subject(Subject.Role.USER))
        }
    }

    @Test
    fun `test checkClass extension fail on null`() {
        assertFailsWith<WrongClassException> {
            VoterImplement<Any, Any>()
                .checkClass(String::class, null)
        }
    }

    @Test
    fun `test checkClass extension`() {
        VoterImplement<Any, Any>()
            .checkClass(Subject::class, Subject(Subject.Role.USER))
    }
}
