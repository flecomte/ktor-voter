package fr.ktorVoter

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.*
import io.ktor.util.pipeline.*

typealias KtorVoter = Voter<ApplicationCall>
/** Variable to store all available voters */
private val votersAttributeKey = AttributeKey<List<KtorVoter>>("voters")

/** Extensions */
fun ApplicationCall.assertCan(action: Any, subject: Any? = null) {
    if (!can(action, subject)) {
        throw UnauthorizedException(action)
    }
}
fun ApplicationCall.assertCanAll(action: Any, subject: List<Any>) {
    assertCan(action, subject)
}

fun ApplicationCall.can(action: Any, subject: Any? = null): Boolean =
    attributes[votersAttributeKey].can(action, this, subject)

fun ApplicationCall.canAll(action: Any, subjects: List<Any>): Boolean =
    subjects.all { can(action, it) }

fun PipelineContext<Unit, ApplicationCall>.assertCan(action: Any, subject: Any? = null) =
    context.assertCan(action, subject)

fun PipelineContext<Unit, ApplicationCall>.assertCanAll(action: Any, subject: List<Any>) =
    context.assertCanAll(action, subject)

fun PipelineContext<Unit, ApplicationCall>.can(action: Any, subject: Any? = null) =
    context.can(action, subject)

fun PipelineContext<Unit, ApplicationCall>.canAll(action: Any, subject: List<Any>) =
    context.canAll(action, subject)

/** Configuration class for ktor */
class AuthorizationVoter {
    /** Configuration for [AuthorizationVoter] feature. */
    class Configuration {
        var voters = listOf<KtorVoter>()
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

class UnauthorizedException(action: Any) : VoterException("""Unauthorized for action "$action"""")
class ForbiddenException(message: String? = null) : Throwable(message)