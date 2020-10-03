package fr.ktorVoter

import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

class WrongClassException(
    expected: KClass<*>,
    current: KClass<*>?
) : VoterException("Can not define authorization with class $current. Need $expected")

fun Voter<*, *>.checkClass(
    expected: KClass<*>,
    subject: Any?
) {
    if (subject != null && !subject::class.isSubclassOf(expected)) {
        throw WrongClassException(expected, subject::class)
    } else if (subject == null) {
        throw WrongClassException(expected, null)
    }
}