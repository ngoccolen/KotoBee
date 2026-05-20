package com.example.kotobee.ui.profile

import android.net.Uri
import androidx.compose.material3.Badge
import com.example.kotobee.data.model.Badge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.kotobee.data.service.CloudinaryService
import com.example.kotobee.ui.auth.AuthState
import com.example.kotobee.util.StudyActivityTracker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class ProfileState(
    val username: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val jlptLevel: String = "N5",
    val learnedVocab: Int = 0,
    val streak: Int = 0,
    val rankInfo: String = "Chưa xếp hạng",
    val role: String = "USER",
    val totalStudyPoints: Int = 0,
    val activeDays: Int = 0,
    val completedTasks: Int = 0,
    val totalTasks: Int = 0,
    val lastActivityLabel: String = "Chưa có hoạt động"
)

data class ActivityDay(
    val day: String,
    val value: Int,
    val dateKey: String = "",
    val isToday: Boolean = false
)

data class RecentActivity(
    val title: String,
    val subtitle: String,
    val meta: String,
    val type: String
)

class ProfileViewModel(
    private val cloudinaryService: CloudinaryService
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState

    private val _activityData = MutableStateFlow<List<ActivityDay>>(emptyList())
    val activityData: StateFlow<List<ActivityDay>> = _activityData

    private val _recentActivities = MutableStateFlow<List<RecentActivity>>(emptyList())
    val recentActivities: StateFlow<List<RecentActivity>> = _recentActivities

    private val _updateState = MutableStateFlow<AuthState>(AuthState.Idle)
    val updateState: StateFlow<AuthState> = _updateState

    private val _badges = MutableStateFlow<List<Badge>>(emptyList())
    val badges: StateFlow<List<Badge>> = _badges

    init {
        _activityData.value = buildActivityWindow()
        _recentActivities.value = listOf(
            RecentActivity(
                title = "Chưa có hoạt động",
                subtitle = "Học flashcard hoặc làm nhiệm vụ để ghi nhận tiến độ.",
                meta = "",
                type = "empty"
            )
        )
    }

    fun loadUserProfile() {
        val email = auth.currentUser?.email ?: return
        viewModelScope.launch {
            try {
                val snapshot = db.collection("users").whereEqualTo("email", email).get().await()
                if (!snapshot.isEmpty) {
                    val doc = snapshot.documents[0]
                    val baseState = ProfileState(
                        username = doc.getString("username") ?: "",
                        email = email,
                        avatarUrl = doc.getString("avatar_url") ?: "",
                        jlptLevel = doc.getString("jlpt_level") ?: "N5",
                        learnedVocab = doc.numberValue("learned_vocab"),
                        streak = doc.numberValue("streak"),
                        role = doc.getString("role") ?: "USER",
                        rankInfo = doc.getString("rank_info") ?: "Chưa xếp hạng"
                    )
                    _profileState.value = baseState
                    loadLearningStats(doc.id, doc.reference, baseState)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateProfile(newUsername: String, newJlptLevel: String, newAvatarUri: Uri?) {
        val email = auth.currentUser?.email ?: return

        viewModelScope.launch {
            _updateState.value = AuthState.Loading
            try {
                var downloadUrl = _profileState.value.avatarUrl

                if (newAvatarUri != null) {
                    downloadUrl = cloudinaryService.uploadImage(newAvatarUri)
                        ?: throw IllegalStateException("Không tải ảnh lên được, vui lòng thử lại")
                }

                val snapshot = db.collection("users").whereEqualTo("email", email).get().await()
                if (!snapshot.isEmpty) {
                    val docRef = snapshot.documents[0].reference
                    val updates = mapOf(
                        "username" to newUsername,
                        "jlpt_level" to newJlptLevel,
                        "avatar_url" to downloadUrl,
                        "avatarUrl" to downloadUrl,
                        "avatar_updated_at" to System.currentTimeMillis()
                    )
                    docRef.update(updates).await()

                    auth.currentUser?.updateProfile(
                        UserProfileChangeRequest.Builder()
                            .setDisplayName(newUsername)
                            .setPhotoUri(downloadUrl.takeIf { it.isNotBlank() }?.let(Uri::parse))
                            .build()
                    )?.await()

                    _profileState.value = _profileState.value.copy(
                        username = newUsername,
                        jlptLevel = newJlptLevel,
                        avatarUrl = downloadUrl
                    )
                    _updateState.value = AuthState.Success()
                }
            } catch (e: Exception) {
                _updateState.value = AuthState.Error(e.message ?: "Lỗi khi cập nhật hồ sơ")
            }
        }
    }

    fun resetUpdateState() {
        _updateState.value = AuthState.Idle
    }

    private suspend fun loadLearningStats(
        userDocId: String,
        userRef: DocumentReference,
        baseState: ProfileState
    ) {
        try {
            val activitySnapshot = db.collection("users")
                .document(userDocId)
                .collection("study_activity")
                .get()
                .await()

            val tasksSnapshot = db.collection("users")
                .document(userDocId)
                .collection("daily_tasks")
                .get()
                .await()

            val badgesSnapshot = db.collection("users")
                .document(userDocId)
                .collection("badges")
                .get()
                .await()

            val badgesList = badgesSnapshot.documents.mapNotNull { badgeDoc ->
                Badge(
                    id = badgeDoc.id,
                    name = badgeDoc.getString("name") ?: "",
                    iconName = badgeDoc.getString("iconName") ?: badgeDoc.getString("icon_name") ?: "",
                    earnedAt = badgeDoc.getLong("earnedAt") ?: badgeDoc.getLong("earned_at") ?: 0L,
                    goalId = badgeDoc.getString("goalId") ?: badgeDoc.getString("goal_id") ?: "",
                    goalTitle = badgeDoc.getString("goalTitle") ?: badgeDoc.getString("goal_title") ?: ""
                )
            }.sortedByDescending { it.earnedAt }

            _badges.value = badgesList

            val valuesByDate = activitySnapshot.documents
                .associate { document ->
                    val date = document.getString("date") ?: document.id
                    val value = document.numberValue("value", "points")
                    date to value
                }
                .filterKeys(StudyActivityTracker::isValidDateKey)

            val activeValues = valuesByDate.filterValues { it > 0 }
            val calculatedStreak = StudyActivityTracker.calculateCurrentStreak(activeValues.keys)
            val todayKey = StudyActivityTracker.todayDateKey()
            val todayTasks = tasksSnapshot.documents.filter { task ->
                val dateKey = task.getString("dateKey").orEmpty()
                dateKey.isBlank() || dateKey == todayKey
            }
            val totalTasks = todayTasks.size
            val completedTasks = todayTasks.count { task ->
                val current = task.numberValue("current")
                val target = task.numberValue("target").coerceAtLeast(1)
                current >= target
            }

            _activityData.value = buildActivityWindow(valuesByDate)
            _recentActivities.value = buildRecentActivities(
                valuesByDate = activeValues,
                streak = calculatedStreak,
                completedTasks = completedTasks,
                totalTasks = totalTasks
            )
            _profileState.value = baseState.copy(
                streak = calculatedStreak,
                totalStudyPoints = activeValues.values.sum(),
                activeDays = activeValues.size,
                completedTasks = completedTasks,
                totalTasks = totalTasks,
                lastActivityLabel = activeValues.keys.maxOrNull()?.let(::formatRelativeDate) ?: "Chưa có hoạt động"
            )

            if (calculatedStreak != baseState.streak) {
                runCatching { userRef.update("streak", calculatedStreak).await() }
            }
        } catch (e: Exception) {
            _activityData.value = buildActivityWindow()
            _recentActivities.value = listOf(
                RecentActivity(
                    title = "Chưa tải được hoạt động",
                    subtitle = "Kiểm tra kết nối rồi thử lại.",
                    meta = "",
                    type = "empty"
                )
            )
        }
    }

    private fun buildRecentActivities(
        valuesByDate: Map<String, Int>,
        streak: Int,
        completedTasks: Int,
        totalTasks: Int
    ): List<RecentActivity> {
        val activities = mutableListOf<RecentActivity>()

        if (streak > 0) {
            activities += RecentActivity(
                title = "Streak hiện tại",
                subtitle = "$streak ngày liên tiếp",
                meta = "Giữ nhịp học mỗi ngày",
                type = "streak"
            )
        }

        if (totalTasks > 0) {
            activities += RecentActivity(
                title = "Nhiệm vụ hôm nay",
                subtitle = "$completedTasks/$totalTasks hoàn thành",
                meta = if (completedTasks >= totalTasks) "Đã xong" else "Đang học",
                type = "task"
            )
        }

        valuesByDate.keys
            .sortedDescending()
            .take(3)
            .forEach { dateKey ->
                activities += RecentActivity(
                    title = "Học tập",
                    subtitle = "+${valuesByDate[dateKey] ?: 0} điểm",
                    meta = formatRelativeDate(dateKey),
                    type = "study"
                )
            }

        return activities.ifEmpty {
            listOf(
                RecentActivity(
                    title = "Chưa có hoạt động",
                    subtitle = "Làm nhiệm vụ hoặc học flashcard để bắt đầu streak.",
                    meta = "",
                    type = "empty"
                )
            )
        }.take(5)
    }

    private fun buildActivityWindow(valuesByDate: Map<String, Int> = emptyMap()): List<ActivityDay> =
        buildCurrentWeekActivity(valuesByDate)

    private fun formatRelativeDate(dateKey: String): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = runCatching { formatter.parse(dateKey) }.getOrNull() ?: return dateKey
        val target = Calendar.getInstance().apply {
            time = date
            clearTime()
        }
        val today = Calendar.getInstance().apply { clearTime() }
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            clearTime()
        }
        val labelFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN"))

        return when {
            target.timeInMillis == today.timeInMillis -> "Hôm nay"
            target.timeInMillis == yesterday.timeInMillis -> "Hôm qua"
            else -> labelFormatter.format(date)
        }
    }

    private fun isValidDateKey(value: String): Boolean {
        return runCatching {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(value)
        }.getOrNull() != null
    }

    fun signOut(onSuccess: () -> Unit) {
        auth.signOut()
        onSuccess()
    }

    class Factory(
        private val cloudinaryService: CloudinaryService
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(cloudinaryService) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

fun buildCurrentWeekActivity(
    valuesByDate: Map<String, Int> = emptyMap(),
    today: Calendar = Calendar.getInstance()
): List<ActivityDay> {
    val keyFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val normalizedToday = (today.clone() as Calendar).apply { clearTime() }
    val weekStart = (normalizedToday.clone() as Calendar).apply {
        val daysFromMonday = (get(Calendar.DAY_OF_WEEK) + 5) % 7
        add(Calendar.DAY_OF_YEAR, -daysFromMonday)
    }

    return WEEKDAY_LABELS.mapIndexed { index, label ->
        val day = (weekStart.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, index)
        }
        val dateKey = keyFormatter.format(day.time)
        val value = if (day.after(normalizedToday)) {
            0
        } else {
            valuesByDate[dateKey]?.coerceAtLeast(0) ?: 0
        }

        ActivityDay(
            day = label,
            value = value,
            dateKey = dateKey,
            isToday = day.timeInMillis == normalizedToday.timeInMillis
        )
    }
}

private val WEEKDAY_LABELS = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

private fun Calendar.clearTime() {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
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
