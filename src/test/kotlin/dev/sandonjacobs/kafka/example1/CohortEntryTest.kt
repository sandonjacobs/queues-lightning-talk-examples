package dev.sandonjacobs.kafka.example1

import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class CohortEntryTest {

    private lateinit var baseCohortEntry: CohortEntry
    private lateinit var fileProcessCommand: CohortFileProcessCommand

    @BeforeTest
    fun init() {
        baseCohortEntry = CohortEntry(
            memberId = "member123",
            email = "member123@kotlin.lang",
            memberName = "Member OneTwoThree",
            action = "ADD"
        )

        @OptIn(ExperimentalTime::class)
        fileProcessCommand = CohortFileProcessCommand(
            customerId = "customerA",
            cohortId = "cohortX",
            updatedTs = Clock.System.now(),
            fileLocation = "/path/to/file.csv"
        )
    }

    @Test
    fun `'ADD' command created`() {
        val cohortEntry = baseCohortEntry.copy(action = "ADD")
        validateCommand(cohortEntry, fileProcessCommand,
            AddMemberToCohortCommand::class.java)
    }

    @Test
    fun `'UPDATE' command created`() {
        val cohortEntry = baseCohortEntry.copy(action = "UPDATE")
        validateCommand(cohortEntry, fileProcessCommand,
            UpdateMemberInCohortCommand::class.java)
    }

    @Test
    fun `'DELETE' command created`() {
        val cohortEntry = baseCohortEntry.copy(action = "DELETE")
        validateCommand(cohortEntry, fileProcessCommand,
            RemoveMemberFromCohortCommand::class.java)
    }

    private fun<T: MemberToCohortCommand> validateCommand(cohortEntry: CohortEntry,
                                fileProcessCommand: CohortFileProcessCommand,
                                expectedCommandType: Class<T>) {

        val result = cohortEntry.cohortEntryToCohortCommand(fileProcessCommand)
        assertEquals(expectedCommandType, result::class.java)
        assertEquals(cohortEntry.memberName, result.memberName)
        assertEquals(cohortEntry.email, result.email)
        assertEquals(cohortEntry.memberName, result.memberName)
        assertEquals(fileProcessCommand.cohortId, result.cohortId)
        assertEquals(fileProcessCommand.customerId, result.customerId)
        assertEquals(fileProcessCommand.fileLocation, result.fileLocation)
    }
}