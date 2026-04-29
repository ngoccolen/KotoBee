package com.example.kotobee.ui.vocab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotobee.data.model.Deck
import com.example.kotobee.data.model.VocabItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class VocabManagerViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _decks = MutableStateFlow<List<Deck>>(emptyList())
    val decks: StateFlow<List<Deck>> = _decks.asStateFlow()

    private val _vocabs = MutableStateFlow<List<VocabItem>>(emptyList())
    val vocabs: StateFlow<List<VocabItem>> = _vocabs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadDecks() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = db.collection("decks")
                    .whereEqualTo("userId", userId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()
                _decks.value = snapshot.toObjects(Deck::class.java)
            } catch (e: Exception) {
                _decks.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createDeck(name: String, description: String) {
        val userId = auth.currentUser?.uid ?: return
        val deckId = UUID.randomUUID().toString()
        val newDeck = Deck(
            id = deckId,
            name = name,
            description = description,
            userId = userId,
            createdAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            try {
                db.collection("decks").document(deckId).set(newDeck).await()
                val currentList = _decks.value.toMutableList()
                currentList.add(0, newDeck)
                _decks.value = currentList
            } catch (e: Exception) {
            }
        }
    }

    fun loadVocabs(deckId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = db.collection("decks").document(deckId)
                    .collection("vocabs")
                    .get()
                    .await()
                _vocabs.value = snapshot.toObjects(VocabItem::class.java)
            } catch (e: Exception) {
                _vocabs.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addVocab(deckId: String, kanji: String, kana: String, meaning: String, example: String, exampleMeaning: String) {
        val vocabId = UUID.randomUUID().toString()
        val newVocab = VocabItem(vocabId, deckId, kanji, kana, meaning, example, exampleMeaning)

        viewModelScope.launch {
            try {
                db.collection("decks").document(deckId)
                    .collection("vocabs").document(vocabId).set(newVocab).await()
                val currentList = _vocabs.value.toMutableList()
                currentList.add(newVocab)
                _vocabs.value = currentList
            } catch (e: Exception) {
            }
        }
    }
}