package dev.sandonjacobs.kafka.example1.model

import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

enum class CohortMemberActionType(val action: String) {
    ADD("ADD"),
    DELETE("DELETE"),
    UPDATE("UPDATE")
}

@Serializable
data class CohortMemberKey(val customerId: String, val cohortId: String, val memberId: String)

@Serializable
sealed class MemberToCohortCommand(val actionType: CohortMemberActionType) {
    abstract val customerId: String
    abstract val cohortId: String
    abstract val memberId: String
    abstract val email: String
    abstract val fileLocation: String
    abstract val memberName: String
    @OptIn(ExperimentalTime::class)
    abstract val transactionTs: Instant
}

@Serializable
data class AddMemberToCohortCommand @OptIn(ExperimentalTime::class) constructor(
    override val customerId: String,
    override val cohortId: String,
    override val memberId: String,
    override val email: String,
    override val fileLocation: String,
    override val memberName: String,
    override val transactionTs: Instant
): MemberToCohortCommand(CohortMemberActionType.ADD)

@Serializable
data class UpdateMemberInCohortCommand @OptIn(ExperimentalTime::class) constructor(
    override val customerId: String,
    override val cohortId: String,
    override val memberId: String,
    override val email: String,
    override val fileLocation: String,
    override val memberName: String,
    override val transactionTs: Instant
): MemberToCohortCommand(CohortMemberActionType.UPDATE)

@Serializable
data class RemoveMemberFromCohortCommand @OptIn(ExperimentalTime::class) constructor(
    override val customerId: String,
    override val cohortId: String,
    override val memberId: String,
    override val email: String,
    override val fileLocation: String,
    override val memberName: String,
    override val transactionTs: Instant
): MemberToCohortCommand(CohortMemberActionType.DELETE)


