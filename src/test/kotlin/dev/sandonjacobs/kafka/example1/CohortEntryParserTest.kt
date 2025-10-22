package dev.sandonjacobs.kafka.example1

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class CohortEntryParserTest {

    @Test
    fun `should parse cohort ADD entry`() {
        val entry = """
          {
              "memberId": "member123",
              "email": "member123@kotlin.lang",
              "memberName": "Member OneTwoThree",
              "action": "ADD"
          }
        """.trimIndent()

        val result = Json.decodeFromString<CohortEntry>(entry)
        assertEquals("member123", result.memberId)
        assertEquals("member123@kotlin.lang", result.email)
        assertEquals("Member OneTwoThree", result.memberName)
        assertEquals("ADD", result.action)
    }

    @Test
    fun `should parse cohort DELETE entry`() {
        val entry = """
          {
              "memberId": "member123",
              "email": "member123@kotlin.lang",
              "memberName": "Member OneTwoThree",
              "action": "DELETE"
          }
        """.trimIndent()

        val result = Json.decodeFromString<CohortEntry>(entry)
        assertEquals("member123", result.memberId)
        assertEquals("member123@kotlin.lang", result.email)
        assertEquals("Member OneTwoThree", result.memberName)
        assertEquals("DELETE", result.action)
    }

    @Test
    fun `should parse cohort UPDATE entry`() {
        val entry = """
          {
              "memberId": "member123",
              "email": "member123@kotlin.lang",
              "memberName": "Member OneTwoThree",
              "action": "UPDATE"
          }
        """.trimIndent()

        val result = Json.decodeFromString<CohortEntry>(entry)
        assertEquals("member123", result.memberId)
        assertEquals("member123@kotlin.lang", result.email)
        assertEquals("Member OneTwoThree", result.memberName)
        assertEquals("UPDATE", result.action)
    }




}