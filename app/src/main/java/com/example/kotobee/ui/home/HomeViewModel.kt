package com.example.kotobee.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun signOut(onSuccess: () -> Unit) {
        auth.signOut()
        onSuccess()
    }
}

private fun DocumentSnapshot.toHomeUserProfile(email: String): UserProfile {
    return UserProfile(
        username = getString("username") ?: getString("displayName") ?: "",
        email = email,
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
