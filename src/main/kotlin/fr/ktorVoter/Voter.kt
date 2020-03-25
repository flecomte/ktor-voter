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

interface Voter {
    fun supports(action: ActionI, call: ApplicationCall, subject: Any? = null): Boolean
    fun vote(action: ActionI, call: ApplicationCall, subject: Any? = null): Vote
}

fun List<Voter>.can(action: ActionI, call: ApplicationCall, subject: Any? = null): Boolean {
    val listOfSubject: List<Any?> = if (subject !is List<*>) listOf(subject) else subject
    val votes: List<Vote> = listOfSubject.flatMap { subject ->
        this
            .filter { it.supports(action, call, subject) }
            .ifEmpty { throw NoVoterException(action) }
            .map { it.vote(action, call, subject) }
    }

    return votes.all { it in listOf(
        Vote.GRANTED,
        Vote.ABSTAIN
    ) } and votes.any { it == Vote.GRANTED }
}

enum class Vote {
    GRANTED,
    ABSTAIN,
    DENIED;

    companion object {
        fun isGranted(lambda: () -> Boolean): Vote {
            return if (lambda()) GRANTED else DENIED
        }
    }
}

private val votersAttributeKey = AttributeKey<List<Voter>>("voters")

fun ApplicationCall.assertCan(action: ActionI, subject: Any? = null, agreeIfNullOrEmpty: Boolean = true) {
    val isNullOrEmpty = (subject == null || (subject is Collection<*> && subject.isNullOrEmpty()))
    if (!can(action, subject) && !agreeIfNullOrEmpty && isNullOrEmpty) {
        throw UnauthorizedException(action)
    }
}

fun PipelineContext<Unit, ApplicationCall>.assertCan(action: ActionI, subject: Any? = null, agreeIfNullOrEmpty: Boolean = true) =
    context.assertCan(action, subject, agreeIfNullOrEmpty)

fun PipelineContext<Unit, ApplicationCall>.can(action: ActionI, subject: Any? = null) =
    context.can(action, subject)

fun ApplicationCall.can(action: ActionI, subject: Any? = null): Boolean {
    val voters = attributes[votersAttributeKey]

    return voters.can(action, this, subject)
}

class AuthorizationVoter {
    /**
     * Configuration for [AuthorizationVoter] feature.
     */
    class Configuration {
        var voters = mutableListOf<Voter>()
        fun voter(voter: Voter) = voters.add(voter)
    }

    /**
     * Object for installing feature
     */
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
