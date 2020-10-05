package fr.ktorVoter

import fr.ktorVoter.VoterTest.TestVoterImplementation.ActionTest
import org.amshove.kluent.`should be`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertFailsWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class VoterTest {
    class TestVoterImplementation() : Voter<Any?> {
        enum class ActionTest : ActionI {
            CREATE,
            DELETE,
        }

        override fun invoke(action: Any, context: Any?, subject: Any?): Vote {
            return if (subject is Subject && action is ActionTest) {
                when (action) {
                    ActionTest.CREATE -> Vote.toVote { subject.role == Subject.Role.ADMIN }
                    ActionTest.DELETE -> Vote.DENIED
                }
            } else Vote.ABSTAIN
        }
    }

    class Subject(
        val role: Role
    ) {
        enum class Role {
            ADMIN,
            USER,
        }
    }


    @Test
    fun `test Voter`() {
        listOf<Voter<Any?>>(TestVoterImplementation()).can(
            action = ActionTest.CREATE,
            context = null,
            subject = Subject(role = Subject.Role.ADMIN)
        ) `should be` true
    }

    @Test
    fun `test Voter GRANTED`() {
        listOf<Voter<Any?>>(
            { _, _, _ -> Vote.GRANTED },
            { _, _, _ -> Vote.ABSTAIN }
        ).can(
            action = ActionTest.CREATE,
            context = null,
            subject = Subject(role = Subject.Role.USER)
        ) `should be` true
    }

    @Test
    fun `test Voter All GRANTED`() {
        listOf<Voter<Any?>>(
            { _, _, _ -> Vote.GRANTED },
            { _, _, _ -> Vote.ABSTAIN }
        ).canAll(
            action = ActionTest.CREATE,
            context = null,
            subjects = listOf(Subject(role = Subject.Role.USER))
        ) `should be` true
    }

    @Test
    fun `test Voter DENIED`() {
        listOf<Voter<Any?>>(
            { _, _, _ -> Vote.GRANTED },
            { _, _, _ -> Vote.ABSTAIN },
            { _, _, _ -> Vote.DENIED }
        ).can(
            action = ActionTest.CREATE,
            context = null,
            subject = Subject(role = Subject.Role.USER)
        ) `should be` false
    }

    @Test
    fun `test Voter All DENIED`() {
        listOf<Voter<Any?>>(
            { _, _, _ -> Vote.GRANTED },
            { _, _, _ -> Vote.ABSTAIN },
            { _, _, _ -> Vote.DENIED }
        ).canAll(
            action = ActionTest.CREATE,
            context = null,
            subjects = listOf(Subject(role = Subject.Role.USER))
        ) `should be` false
    }

    @Test
    fun `test No Voter`() {
        assertFailsWith<NoVoterException> {
            emptyList<Voter<Any?>>()
                .can(FakeAction.FAKE, null)
        }
    }

    @Test
    fun `test All Voter Abstain`() {
        assertFailsWith<AllVoterAbstainException> {
            listOf<Voter<Any?>> { _, _, _ -> Vote.ABSTAIN }
                .can(object {}, null)
        }
    }

    enum class FakeAction : ActionI {
        FAKE
    }
}