
package com.example.kotobee.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class UserProfile(
    val username: String = "",
    val email: String = "",
    val jlpt_level: String = "N5",
    val learned_vocab: Int = 0,
    val streak: Int = 0,
    val role: String = "USER"
)

class HomeViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile

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
                    val data = snapshot.documents[0].toObject(UserProfile::class.java)
                    if (data != null) {
                        _userProfile.value = data
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    fun signOut(onSuccess: () -> Unit) {
        auth.signOut()
        onSuccess()
    }
}