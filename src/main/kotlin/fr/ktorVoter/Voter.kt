package fr.ktorVoter

import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.util.AttributeKey
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.pipeline.PipelineContext

interface ActionI

typealias Voter = (Any, Any?, Any?) -> Vote

/** Check if in the list of voter, you can make the action for one subject */
fun List<Voter>.can(action: Any, context: Any? = null, subject: Any? = null): Boolean =
    ifEmpty { throw NoVoterException() }
        /* For each voter, get the vote of all voter */
        .map {
            it(action, context, subject)
        }
        /* If all Abstain, throw Exception */
        .apply { if (all { it == Vote.ABSTAIN }) throw AllVoterAbstainException(action) }
        /* If no one DENIED and if there is at least one GRANTED, grant access */
        .run { none { it == Vote.DENIED } and any { it == Vote.GRANTED } }

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


/** Variable to store all available voters */
private val votersAttributeKey = AttributeKey<List<Voter>>("voters")

/** Extensions */
fun ApplicationCall.assertCan(action: ActionI, subject: Any) {
    if (!can(action, subject)) {
        throw UnauthorizedException(action)
    }
}

fun ApplicationCall.can(action: ActionI, subject: Any): Boolean =
    attributes[votersAttributeKey].can(action, this, subject)

fun PipelineContext<Unit, ApplicationCall>.assertCan(action: ActionI, subject: Any) =
    context.assertCan(action, subject)

fun PipelineContext<Unit, ApplicationCall>.can(action: ActionI, subject: Any) =
    context.can(action, subject)

/** Configuration class for ktor */
class AuthorizationVoter {
    /** Configuration for [AuthorizationVoter] feature. */
    class Configuration {
        var voters = listOf<Voter>()
    }

    /** Object for installing feature */
    companion object Feature :
        ApplicationFeature<ApplicationCallPipeline, Configuration, AuthorizationVoter> {

        override val key =
            AttributeKey<AuthorizationVoter>("fr.ktorVoter.Voter")

        @KtorExperimentalAPI
        override fun install(
            pipeline: ApplicationCallPipeline,
            configure: Configuration.() -> Unit
        ): AuthorizationVoter {
            val configuration = Configuration().apply(configure)

            pipeline.intercept(ApplicationCallPipeline.Features) {
                context.attributes.put(votersAttributeKey, configuration.voters)

                try {
                    proceed()
                } catch (e: VoterException) {
                    context.respond(HttpStatusCode.Forbidden)
                }
            }

            return AuthorizationVoter()
        }
    }
}

abstract class VoterException(message: String) : Throwable(message)
class NoVoterException() : VoterException("No voter found")
class AllVoterAbstainException(action: Any?) : VoterException("""All voter abstain for the action "$action"""")
class UnauthorizedException(action: ActionI) : VoterException("""Unauthorized for action "$action"""")
class ForbiddenException(message: String? = null) : Throwable(message)

