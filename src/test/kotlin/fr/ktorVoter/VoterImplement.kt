package fr.ktorVoter

class Subject(
    val role: Role
) {
    enum class Role {
        ADMIN,
        USER,
    }
}

class VoterImplement <C, S> : Voter<C, S> {
    enum class Action : ActionI {
        CREATE,
        DELETE,
    }

    override fun supports(action: ActionI, context: C?, subject: S?): Boolean {
        return action in Action.values()
    }

    override fun vote(action: ActionI, context: C?, subject: S?): Vote {
        return if (subject is Subject) {
            if (action in Action.values()) {
                when (action) {
                    Action.CREATE -> {
                        isGranted { subject.role == Subject.Role.ADMIN }
                    }
                    Action.DELETE -> {
                        Vote.DENIED
                    }
                    else -> Vote.ABSTAIN
                }
            } else {
                Vote.ABSTAIN
            }
        } else {
            Vote.ABSTAIN
        }
    }
}