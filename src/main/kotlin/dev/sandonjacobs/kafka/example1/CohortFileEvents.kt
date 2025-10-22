package dev.sandonjacobs.kafka.example1

import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class CohortUpdatedEvent @OptIn(ExperimentalTime::class) constructor(
    val customerId: String,
    val cohortId: String,
    val updatedTs: Instant,
    val fileLocations: List<String>
)

@Serializable
data class CohortFileProcessCommand @OptIn(ExperimentalTime::class) constructor(
    val customerId: String,
    val cohortId: String,
    val updatedTs: Instant,
    val fileLocation: String
)

@Serializable
data class CohortEntry(val memberId: String, val email: String, val memberName: String, val action: String) {

    @OptIn(ExperimentalTime::class)
    fun cohortEntryToCohortCommand(fileProcessCommand: CohortFileProcessCommand): MemberToCohortCommand {
        return when (action) {
            CohortMemberActionType.ADD.action -> AddMemberToCohortCommand(
                customerId = fileProcessCommand.customerId,
                cohortId = fileProcessCommand.cohortId,
                memberId = memberId,
                email = email,
                fileLocation = fileProcessCommand.fileLocation,
                memberName = memberName,
                transactionTs = fileProcessCommand.updatedTs
            )
            CohortMemberActionType.UPDATE.action -> UpdateMemberInCohortCommand(
                customerId = fileProcessCommand.customerId,
                cohortId = fileProcessCommand.cohortId,
                memberId = memberId,
                email = email,
                fileLocation = fileProcessCommand.fileLocation,
                memberName = memberName,
                transactionTs = fileProcessCommand.updatedTs
            )
            CohortMemberActionType.DELETE.action -> RemoveMemberFromCohortCommand(
                customerId = fileProcessCommand.customerId,
                cohortId = fileProcessCommand.cohortId,
                memberId = memberId,
                email = email,
                fileLocation = fileProcessCommand.fileLocation,
                memberName = memberName,
                transactionTs = fileProcessCommand.updatedTs
            )
            else -> throw IllegalArgumentException("Unknown action type: ${action}")
        }
    }
}

