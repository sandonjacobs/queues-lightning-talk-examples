package dev.sandonjacobs.kafka.example1.processor

import kotlin.test.Test

class CohortFileProcessEventProcessorTest {

    val processoor = CohortFileProcessEventProcessor()

    @Test
    fun `loads file to CohortEntry list`() {
        val result = processoor.loadCohortEntriesFromFile("/example1/cohort_entries.json")
        assert(result != null)
        assert(result!!.size == 3)
    }
}