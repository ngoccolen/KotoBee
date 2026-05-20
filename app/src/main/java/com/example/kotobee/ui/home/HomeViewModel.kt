package com.example.kotobee.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotobee.data.model.Badge
import com.example.kotobee.data.model.GoalMilestone
import com.example.kotobee.data.model.LearningGoal
import com.example.kotobee.util.StudyActivityTracker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class UserProfile(
    val username: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val jlptLevel: String = "N5",
    val learnedVocab: Int = 0,
    val skillsProgress: Map<String, Float> = emptyMap(),
    val streak: Int = 0,
    val todayStudyPoints: Int = 0,
    val weeklyStudyPoints: Int = 0,
    val monthlyStudyPoints: Int = 0,
    val todayPointBreakdown: Map<String, Int> = emptyMap(),
    val activeDays: Int = 0,
    val role: String = "USER"
)

data class DailyTask(
    val id: String = "",
    val title: String = "",
    val current: Int = 0,
    val target: Int = 1,
    val dateKey: String = ""
)

class HomeViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile

    private val _dailyTasks = MutableStateFlow<List<DailyTask>>(emptyList())
    val dailyTasks: StateFlow<List<DailyTask>> = _dailyTasks

    // Learning Goal Path
    private val _currentGoal = MutableStateFlow<LearningGoal?>(null)
    val currentGoal: StateFlow<LearningGoal?> = _currentGoal

    private val _showCompletionDialog = MutableStateFlow(false)
    val showCompletionDialog: StateFlow<Boolean> = _showCompletionDialog

    private val _earnedBadge = MutableStateFlow<Badge?>(null)
    val earnedBadge: StateFlow<Badge?> = _earnedBadge

    private var currentUserDocId: String? = null

    fun loadUserData() {
        val currentUser = auth.currentUser ?: return
        val email = currentUser.email ?: return

        viewModelScope.launch {
            try {
                val snapshot = db.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .await()

                if (!snapshot.isEmpty) {
                    val doc = snapshot.documents[0]
                    currentUserDocId = doc.id
                    val baseProfile = doc.toHomeUserProfile(email)
                    _userProfile.value = loadStudySummary(doc.id, baseProfile)
                    loadDailyTasks()
                    loadCurrentGoal()
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to load user data: ${e.message}")
            }
        }
    }

    private suspend fun loadDailyTasks() {
        val docId = currentUserDocId ?: return
        try {
            val todayKey = StudyActivityTracker.todayDateKey()
            val tasksCollection = db.collection("users")
                .document(docId)
                .collection("daily_tasks")
            val tasksSnapshot = tasksCollection
                .get()
                .await()

            val tasks = mutableListOf<DailyTask>()
            tasksSnapshot.documents.forEach { doc ->
                val task = doc.toObject(DailyTask::class.java)
                    ?.copy(id = doc.id)
                    ?: return@forEach

                when {
                    task.dateKey.isBlank() -> {
                        doc.reference.update("dateKey", todayKey).await()
                        tasks += task.copy(dateKey = todayKey)
                    }
                    task.dateKey == todayKey -> tasks += task
                    else -> doc.reference.delete().await()
                }
            }

            _dailyTasks.value = tasks
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Failed to load daily tasks: ${e.message}")
        }
    }

    private suspend fun loadStudySummary(userDocId: String, baseProfile: UserProfile): UserProfile {
        val activitySnapshot = db.collection("users")
            .document(userDocId)
            .collection("study_activity")
            .get()
            .await()

        val activityByDate = activitySnapshot.documents
            .mapNotNull { document ->
                val date = document.getString("date") ?: document.id
                if (StudyActivityTracker.isValidDateKey(date)) date to document else null
            }
            .toMap()

        val valuesByDate = activityByDate.mapValues { (_, document) ->
            document.numberValue("value", "points")
        }

        val activeValues = valuesByDate.filterValues { it > 0 }
        val currentStreak = StudyActivityTracker.calculateCurrentStreak(activeValues.keys)
        val todayKey = StudyActivityTracker.todayDateKey()
        val todayPoints = valuesByDate[todayKey] ?: 0
        val weekKeys = StudyActivityTracker.currentWeekDateKeys()
        val monthKeys = StudyActivityTracker.currentMonthDateKeys()
        val weeklyPoints = valuesByDate
            .filterKeys { it in weekKeys }
            .values
            .sum()
        val monthlyPoints = valuesByDate
            .filterKeys { it in monthKeys }
            .values
            .sum()
        val todayBreakdown = activityByDate[todayKey]
            ?.sourcePointValues("sourcePoints")
            .orEmpty()
            .let { breakdown ->
                if (breakdown.isNotEmpty() || todayPoints == 0) breakdown else mapOf("study" to todayPoints)
            }

        if (currentStreak != baseProfile.streak) {
            runCatching {
                db.collection("users").document(userDocId).update("streak", currentStreak).await()
            }
        }

        return baseProfile.copy(
            streak = currentStreak,
            todayStudyPoints = todayPoints,
            weeklyStudyPoints = weeklyPoints,
            monthlyStudyPoints = monthlyPoints,
            todayPointBreakdown = todayBreakdown,
            activeDays = activeValues.size
        )
    }

    private suspend fun recordStudyActivity(points: Long, source: String) {
        val docId = currentUserDocId ?: return
        StudyActivityTracker.recordStudyActivity(
            userRef = db.collection("users").document(docId),
            points = points,
            source = source
        )
    }

    fun addNewDailyTask(title: String, target: Int) {
        val docId = currentUserDocId ?: return
        val taskId = UUID.randomUUID().toString()
        val newTask = DailyTask(
            id = taskId,
            title = title,
            current = 0,
            target = target.coerceAtLeast(1),
            dateKey = StudyActivityTracker.todayDateKey()
        )

        viewModelScope.launch {
            try {
                db.collection("users")
                    .document(docId)
                    .collection("daily_tasks")
                    .document(taskId)
                    .set(newTask)
                    .await()

                _dailyTasks.value = _dailyTasks.value + newTask
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to add daily task: ${e.message}")
            }
        }
    }

    fun incrementTaskProgress(task: DailyTask) {
        val docId = currentUserDocId ?: return
        if (task.current >= task.target) return

        val newCurrent = task.current + 1
        val todayKey = StudyActivityTracker.todayDateKey()

        viewModelScope.launch {
            try {
                db.collection("users")
                    .document(docId)
                    .collection("daily_tasks")
                    .document(task.id)
                    .update(
                        mapOf(
                            "current" to newCurrent,
                            "dateKey" to todayKey
                        )
                    )
                    .await()

                runCatching { recordStudyActivity(10L, "daily_task") }.onFailure { error ->
                    Log.e("HomeViewModel", "Failed to record study activity: ${error.message}")
                }

                _userProfile.value = loadStudySummary(docId, _userProfile.value)
                loadDailyTasks()
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to update daily task: ${e.message}")
            }
        }
    }

    // ====================================================
    // --- LEARNING GOAL PATH ---
    // ====================================================

    fun loadCurrentGoal() {
        val docId = currentUserDocId ?: return
        viewModelScope.launch {
            try {
                val goalsSnapshot = db.collection("users")
                    .document(docId)
                    .collection("goals")
                    .whereEqualTo("isCompleted", false)
                    .get()
                    .await()

                if (!goalsSnapshot.isEmpty) {
                    val doc = goalsSnapshot.documents[0]
                    _currentGoal.value = parseGoalFromDocument(doc)
                } else {
                    _currentGoal.value = null
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to load goal: ${e.message}")
            }
        }
    }

    private fun parseGoalFromDocument(doc: DocumentSnapshot): LearningGoal {
        val milestonesRaw = doc.get("milestones") as? List<*> ?: emptyList<Any>()
        val milestones = milestonesRaw.mapNotNull { raw ->
            val map = raw as? Map<*, *> ?: return@mapNotNull null
            GoalMilestone(
                id = map["id"] as? String ?: "",
                title = map["title"] as? String ?: "",
                isCompleted = map["isCompleted"] as? Boolean ?: false,
                order = (map["order"] as? Number)?.toInt() ?: 0
            )
        }.sortedBy { it.order }

        return LearningGoal(
            id = doc.id,
            title = doc.getString("title") ?: "",
            milestones = milestones,
            isCompleted = doc.getBoolean("isCompleted") ?: false,
            createdAt = doc.getLong("createdAt") ?: 0L,
            completedAt = doc.getLong("completedAt")
        )
    }

    fun createNewGoal(title: String, milestoneTitles: List<String>) {
        val docId = currentUserDocId ?: return
        val goalId = UUID.randomUUID().toString()
        val milestones = milestoneTitles.mapIndexed { index, msTitle ->
            mapOf(
                "id" to UUID.randomUUID().toString(),
                "title" to msTitle,
                "isCompleted" to false,
                "order" to index
            )
        }
        val goalData = mapOf(
            "title" to title,
            "milestones" to milestones,
            "isCompleted" to false,
            "createdAt" to System.currentTimeMillis(),
            "completedAt" to null
        )

        viewModelScope.launch {
            try {
                db.collection("users")
                    .document(docId)
                    .collection("goals")
                    .document(goalId)
                    .set(goalData)
                    .await()

                loadCurrentGoal()
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to create goal: ${e.message}")
            }
        }
    }

    fun completeMilestone(milestoneId: String) {
        val docId = currentUserDocId ?: return
        val goal = _currentGoal.value ?: return
        val milestoneIndex = goal.milestones.indexOfFirst { it.id == milestoneId }
        if (milestoneIndex == -1) return
        if (goal.milestones[milestoneIndex].isCompleted) return

        viewModelScope.launch {
            try {
                val updatedMilestones = goal.milestones.map { ms ->
                    if (ms.id == milestoneId) ms.copy(isCompleted = true) else ms
                }
                val allCompleted = updatedMilestones.all { it.isCompleted }

                val milestonesData = updatedMilestones.map { ms ->
                    mapOf(
                        "id" to ms.id,
                        "title" to ms.title,
                        "isCompleted" to ms.isCompleted,
                        "order" to ms.order
                    )
                }

                val updates = mutableMapOf<String, Any?>(
                    "milestones" to milestonesData
                )
                if (allCompleted) {
                    updates["isCompleted"] = true
                    updates["completedAt"] = System.currentTimeMillis()
                }

                db.collection("users")
                    .document(docId)
                    .collection("goals")
                    .document(goal.id)
                    .update(updates)
                    .await()

                // Record study activity for milestone completion
                runCatching { recordStudyActivity(20L, "goal_milestone") }

                val updatedGoal = goal.copy(
                    milestones = updatedMilestones,
                    isCompleted = allCompleted,
                    completedAt = if (allCompleted) System.currentTimeMillis() else null
                )
                _currentGoal.value = updatedGoal

                if (allCompleted) {
                    awardBadge(updatedGoal)
                    _showCompletionDialog.value = true
                }

                _userProfile.value = loadStudySummary(docId, _userProfile.value)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to complete milestone: ${e.message}")
            }
        }
    }

    private suspend fun awardBadge(goal: LearningGoal) {
        val docId = currentUserDocId ?: return
        val badgeId = UUID.randomUUID().toString()

        // Count existing badges to determine badge icon
        val existingBadgesCount = try {
            db.collection("users")
                .document(docId)
                .collection("badges")
                .get()
                .await()
                .size()
        } catch (e: Exception) { 0 }

        val iconName = when {
            existingBadgesCount == 0 -> "first_goal"
            existingBadgesCount < 3 -> "rising_star"
            existingBadgesCount < 5 -> "champion"
            existingBadgesCount < 10 -> "master"
            else -> "legend"
        }

        val badgeName = when {
            existingBadgesCount == 0 -> "Người tiên phong"
            existingBadgesCount < 3 -> "Ngôi sao đang lên"
            existingBadgesCount < 5 -> "Nhà vô địch"
            existingBadgesCount < 10 -> "Bậc thầy"
            else -> "Huyền thoại"
        }

        val badge = Badge(
            id = badgeId,
            name = badgeName,
            iconName = iconName,
            earnedAt = System.currentTimeMillis(),
            goalId = goal.id,
            goalTitle = goal.title
        )

        val badgeData = mapOf(
            "name" to badge.name,
            "iconName" to badge.iconName,
            "earnedAt" to badge.earnedAt,
            "goalId" to badge.goalId,
            "goalTitle" to badge.goalTitle
        )

        try {
            db.collection("users")
                .document(docId)
                .collection("badges")
                .document(badgeId)
                .set(badgeData)
                .await()

            _earnedBadge.value = badge

            // Record study activity for badge
            runCatching { recordStudyActivity(50L, "badge_earned") }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Failed to award badge: ${e.message}")
        }
    }

    fun dismissCompletionDialog() {
        _showCompletionDialog.value = false
        _earnedBadge.value = null
        // Reload to get next goal or show empty state
        loadCurrentGoal()
    }

    fun deleteCurrentGoal() {
        val docId = currentUserDocId ?: return
        val goal = _currentGoal.value ?: return

        viewModelScope.launch {
            try {
                db.collection("users")
                    .document(docId)
                    .collection("goals")
                    .document(goal.id)
                    .delete()
                    .await()
                _currentGoal.value = null
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to delete goal: ${e.message}")
            }
        }
    }

    fun signOut(onSuccess: () -> Unit) {
        auth.signOut()
        onSuccess()
    }
}

private fun DocumentSnapshot.toHomeUserProfile(email: String): UserProfile {
    return UserProfile(
        username = getString("username") ?: getString("displayName") ?: "",
        email = email,
        avatarUrl = getString("avatar_url") ?: getString("avatarUrl") ?: "",
        jlptLevel = getString("jlpt_level") ?: getString("current_level") ?: "N5",
        learnedVocab = numberValue("learned_vocab", "mastered_vocab_count"),
        skillsProgress = mapValue("skills_progress"),
        streak = numberValue("streak"),
        role = getString("role") ?: "USER"
    )
}

private fun DocumentSnapshot.mapValue(field: String): Map<String, Float> {
    val raw = get(field) as? Map<*, *> ?: return emptyMap()
    return raw.mapNotNull { (key, value) ->
        val name = key as? String ?: return@mapNotNull null
        val progress = when (value) {
            is Number -> value.toFloat()
            is String -> value.toFloatOrNull()
            else -> null
        } ?: return@mapNotNull null
        name to progress
    }.toMap()
}

private fun DocumentSnapshot.numberValue(vararg fields: String): Int {
    fields.forEach { field ->
        when (val value = get(field)) {
            is Number -> return value.toInt()
            is String -> value.toIntOrNull()?.let { return it }
        }
    }
    return 0
}

private fun DocumentSnapshot.sourcePointValues(field: String): Map<String, Int> {
    val raw = get(field) as? Map<*, *> ?: return emptyMap()
    return raw.mapNotNull { (key, value) ->
        val source = key as? String ?: return@mapNotNull null
        val points = when (value) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull()
            else -> null
        } ?: return@mapNotNull null
        source to points
    }.toMap()
}
