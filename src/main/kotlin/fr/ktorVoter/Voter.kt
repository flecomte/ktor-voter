package fr.ktorVoter

interface ActionI

typealias Voter<C> = (Any, C, Any?) -> Vote

/** Check if in the list of voter, you can make the action for one subject */
fun <C> List<Voter<C>>.can(action: Any, context: C, subject: Any? = null): Boolean =
    ifEmpty { throw NoVoterException() }
        /* For each voter, get the vote of all voter */
        .map {
            it(action, context, subject)
        }
        /* If all Abstain, throw Exception */
        .apply { if (all { it == Vote.ABSTAIN }) throw AllVoterAbstainException(action) }
        /* If no one DENIED and if there is at least one GRANTED, grant access */
        .run { none { it == Vote.DENIED } and any { it == Vote.GRANTED } }

fun <C> List<Voter<C>>.canAll(action: Any, context: C, subjects: List<Any>): Boolean =
    subjects.all { can(action, context, it) }

/** Responses of voters */
enum class Vote {
    GRANTED,
    ABSTAIN,
    DENIED;

    /** Helper to convert true/false/null to GRANTED/DENIED/ABSTAIN */
    companion object {
        fun toVote(lambda: () -> Boolean?): Vote = when (lambda()) {
            true -> GRANTED
            false -> DENIED
            null -> ABSTAIN
        }
    }

    fun toBool(): Boolean? = when (this) {
        GRANTED -> true
        DENIED -> false
        ABSTAIN -> null
    }
}

abstract class VoterException(message: String) : Throwable(message)
class NoVoterException() : VoterException("No voter found")
class AllVoterAbstainException(action: Any?) : VoterException("""All voter abstain for the action "$action"""")
