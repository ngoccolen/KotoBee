package com.example.kotobee.data.repository

import com.example.kotobee.model.StudyLeaderboardEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StudyLeaderboardBuilderTest {

    @Test
    fun legacyActivityEntryCanPopulateTopLeaderboard() {
        val leaderboards = buildStudyLeaderboards(
            candidates = listOf(entry("legacy", points = 42)),
            currentUserId = null,
            limit = 10
        )

        assertEquals(listOf("legacy"), leaderboards.entries.map { it.userId })
        assertEquals(42, leaderboards.entries.first().totalPoints)
    }

    @Test
    fun currentUserOutsideTopIsExposedAsPinnedEntry() {
        val leaderboards = buildStudyLeaderboards(
            candidates = listOf(
                entry("top-1", points = 100),
                entry("top-2", points = 80),
                entry("me", points = 20, isCurrentUser = true)
            ),
            currentUserId = "me",
            limit = 2
        )

        assertEquals(listOf("top-1", "top-2"), leaderboards.entries.map { it.userId })
        assertEquals("me", leaderboards.currentUserEntry?.userId)
        assertTrue(leaderboards.currentUserEntry?.isCurrentUser == true)
    }

    @Test
    fun zeroPointCurrentUserDoesNotHideOtherScores() {
        val leaderboards = buildStudyLeaderboards(
            candidates = listOf(
                entry("other", points = 30),
                entry("me", points = 0, isCurrentUser = true)
            ),
            currentUserId = "me",
            limit = 10
        )

        assertEquals(listOf("other"), leaderboards.entries.map { it.userId })
        assertNull(leaderboards.currentUserEntry)
    }

    @Test
    fun duplicateUsersAreMergedWithHighestPointValue() {
        val leaderboards = buildStudyLeaderboards(
            candidates = listOf(
                entry("me", points = 10),
                entry("me", points = 45, isCurrentUser = true),
                entry("other", points = 30)
            ),
            currentUserId = "me",
            limit = 10
        )

        assertEquals(listOf("me", "other"), leaderboards.entries.map { it.userId })
        assertEquals(45, leaderboards.entries.first().totalPoints)
        assertTrue(leaderboards.entries.first().isCurrentUser)
        assertNull(leaderboards.currentUserEntry)
    }

    private fun entry(
        userId: String,
        points: Int,
        isCurrentUser: Boolean = false
    ): StudyLeaderboardEntry {
        return StudyLeaderboardEntry(
            userId = userId,
            username = userId,
            totalPoints = points,
            isCurrentUser = isCurrentUser
        )
    }
}
