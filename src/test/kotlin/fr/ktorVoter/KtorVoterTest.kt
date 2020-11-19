package fr.ktorVoter

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.amshove.kluent.`should be`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertFailsWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class KtorVoterTest {
    @Test
    fun `test AuthorizationVoter GRANTED`() {
        withTestApplication({
            install(AuthorizationVoter) {
                voters = listOf { _, _, _ -> Vote.GRANTED }
            }
        }) {
            val call = handleRequest(HttpMethod.Post, "/user") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("""{"test":"plop"}""")
            }
            with(call) {
                call.can("plop") `should be` true
                call.assertCan("plop")
            }
        }
    }

    @Test
    fun `test AuthorizationVoter All GRANTED`() {
        withTestApplication({
            install(AuthorizationVoter) {
                voters = listOf(
                    { _, _, _ -> Vote.GRANTED }
                )
            }
        }) {
            val call = handleRequest(HttpMethod.Post, "/user") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("""{"test":"plop"}""")
            }
            with(call) {
                call.canAll("plop", listOf(object {}, object {})) `should be` true
                call.assertCanAll("plop", listOf(object {}, object {}))
            }
        }
    }

    @Test
    fun `test AuthorizationVoter DENIED`() {
        withTestApplication({
            install(AuthorizationVoter) {
                voters = listOf(
                    { _, _, _ -> Vote.DENIED }
                )
            }
        }) {
            pipeline
            val call = handleRequest(HttpMethod.Post, "/user") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("""{"test":"plop"}""")
            }
            with(call) {
                call.can("plop") `should be` false
                assertFailsWith<UnauthorizedException> {
                    call.assertCan("plop")
                }
            }
        }
    }

    @Test
    fun `test AuthorizationVoter All DENIED`() {
        withTestApplication({
            install(AuthorizationVoter) {
                voters = listOf(
                    { _, _, _ -> Vote.DENIED },
                    { _, _, _ -> Vote.GRANTED }
                )
            }
        }) {
            pipeline
            val call = handleRequest(HttpMethod.Post, "/user") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("""{"test":"plop"}""")
            }
            with(call) {
                call.canAll("plop", listOf(object {}, object {})) `should be` false
                assertFailsWith<UnauthorizedException> {
                    call.assertCanAll("plop", listOf(object {}, object {}))
                }
            }
        }
    }
}