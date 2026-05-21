package com.example.kotobee.data.repository

import com.example.kotobee.model.StudyLeaderboardEntry
import com.example.kotobee.model.StudyLeaderboards

internal fun buildStudyLeaderboards(
    candidates: List<StudyLeaderboardEntry>,
    currentUserId: String?,
    limit: Int
): StudyLeaderboards {
    val safeLimit = limit.coerceAtLeast(1)
    val topEntries = candidates
        .filter { it.userId.isNotBlank() && it.totalPoints > 0 }
        .groupBy { it.userId }
        .values
        .mapNotNull { duplicateEntries ->
            val bestEntry = duplicateEntries.maxWithOrNull(
                compareBy<StudyLeaderboardEntry> { it.totalPoints }
                    .thenBy { if (it.isCurrentUser) 1 else 0 }
            ) ?: return@mapNotNull null

            bestEntry.copy(isCurrentUser = duplicateEntries.any { it.isCurrentUser })
        }
        .sortedWith(
            compareByDescending<StudyLeaderboardEntry> { it.totalPoints }
                .thenBy { it.username.lowercase() }
        )

    val visibleEntries = topEntries.take(safeLimit)
    val currentEntry = currentUserId
        ?.takeIf { it.isNotBlank() }
        ?.let { userId ->
            topEntries.firstOrNull { entry ->
                entry.userId == userId && visibleEntries.none { it.userId == userId }
            }
        }

    return StudyLeaderboards(
        entries = visibleEntries,
        currentUserEntry = currentEntry
    )
}
