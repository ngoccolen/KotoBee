package com.example.kotobee.ui.lessons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ProfileState(
    val username: String = "",
    val email: String = "",
    val jlptLevel: String = "N5",
    val learnedVocab: Int = 0,
    val streak: Int = 0,
    val rankInfo: String = "Top 5%",
    val role: String = "USER"
)

data class ActivityDay(val day: String, val value: Int)

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState

    private val _activityData = MutableStateFlow<List<ActivityDay>>(emptyList())
    val activityData: StateFlow<List<ActivityDay>> = _activityData

    init {
        loadActivityData()
    }

    fun loadUserProfile() {
        val email = auth.currentUser?.email ?: return
        viewModelScope.launch {
            try {
                val snapshot = db.collection("users").whereEqualTo("email", email).get().await()
                if (!snapshot.isEmpty) {
                    val doc = snapshot.documents[0]
                    _profileState.value = ProfileState(
                        username = doc.getString("username") ?: "",
                        email = email,
                        jlptLevel = doc.getString("jlpt_level") ?: "N5",
                        learnedVocab = doc.getLong("learned_vocab")?.toInt() ?: 0,
                        streak = doc.getLong("streak")?.toInt() ?: 0
                    )
                }
            } catch (e: Exception) {
            }
        }
    }

    private fun loadActivityData() {
        _activityData.value = listOf(
            ActivityDay("TH2", 30),
            ActivityDay("TH3", 60),
            ActivityDay("TH4", 90),
            ActivityDay("TH5", 50),
            ActivityDay("TH6", 75),
            ActivityDay("TH7", 20),
            ActivityDay("CN", 40)
        )
    }

    fun signOut(onSuccess: () -> Unit) {
        auth.signOut()
        onSuccess()
    }
}