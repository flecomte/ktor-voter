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

/** Interface to implement voter */
interface Voter<C, S> {
    fun supports(action: ActionI, context: C?, subject: S? = null): Boolean
    fun vote(action: ActionI, context: C?, subject: S? = null): Vote
    fun isGranted(lambda: () -> Boolean?): Vote =
        Vote.isGranted(lambda)
}

/** Check if in the list of voter, you can make the action for one subject */
fun <C> List<Voter<C, Any>>.can(action: ActionI, context: C? = null, subject: Any? = null): Boolean = subject
    /* Convert subject as list */
    .let { if (subject !is List<*>) listOf(subject) else subject }
    /* For each voter, get the vote of all supported voter */
    .flatMap { sub ->
        filter { it.supports(action, context, sub) }
            .ifEmpty { throw NoVoterException(action) }
            .map { it.vote(action, context, sub) }
    }
    /* Check if no one DENIED and if there is at least one GRANTED */
    .run {
        none { it == Vote.DENIED } and
        any { it == Vote.GRANTED }
    }

/** Responses of voters */
enum class Vote {
    GRANTED,
    ABSTAIN,
    DENIED;

    /** Helper to convert true/false/null to GRANTED/DENIED/ABSTAIN */
    companion object {
        fun isGranted(lambda: () -> Boolean?): Vote = when (lambda()) {
            true -> GRANTED
            false -> DENIED
            null -> ABSTAIN
        }
    }
}

/** Variable to store all available voters */
private val votersAttributeKey = AttributeKey<List<Voter<ApplicationCall, Any>>>("voters")

/** Extensions */
fun ApplicationCall.assertCan(action: ActionI, subject: Any? = null) {
    if (!can(action, subject)) {
        throw UnauthorizedException(action)
    }
}

fun ApplicationCall.can(action: ActionI, subject: Any? = null): Boolean =
    attributes[votersAttributeKey].can(action, this, subject)

fun PipelineContext<Unit, ApplicationCall>.assertCan(action: ActionI, subject: Any? = null) =
    context.assertCan(action, subject)

fun PipelineContext<Unit, ApplicationCall>.can(action: ActionI, subject: Any? = null) =
    context.can(action, subject)

/** Configuration class for ktor */
class AuthorizationVoter {
    /** Configuration for [AuthorizationVoter] feature. */
    class Configuration {
        val voters = listOf<Voter<ApplicationCall, Any>>()
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
class NoVoterException(action: ActionI) : VoterException("No voter found for action '$action'")
class UnauthorizedException(action: ActionI) : VoterException("Unauthorized for action '$action'")
class ForbiddenException(message: String? = null) : Throwable(message)
