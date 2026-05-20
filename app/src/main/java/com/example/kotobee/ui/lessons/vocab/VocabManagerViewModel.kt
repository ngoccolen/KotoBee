package com.example.kotobee.ui.lessons.vocab

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotobee.data.model.Deck
import com.example.kotobee.data.model.VocabItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class QuizQuestion(
    val vocab: VocabItem,
    val options: List<String>,
    val correctAnswer: String
)

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
                val snapshot = db.collection("decks").whereEqualTo("userId", userId).get().await()
                val loadedDecks = snapshot.toObjects(Deck::class.java).sortedByDescending { it.createdAt }
                _decks.value = loadedDecks
            } catch (e: Exception) {
                Log.e("FirebaseError", "Lỗi tải Decks: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createDeck(name: String, description: String) {
        val userId = auth.currentUser?.uid ?: return
        val deckId = UUID.randomUUID().toString()
        val newDeck = Deck(id = deckId, name = name, description = description, userId = userId, createdAt = System.currentTimeMillis())

        viewModelScope.launch {
            try {
                db.collection("decks").document(deckId).set(newDeck).await()
                val currentList = _decks.value.toMutableList()
                currentList.add(0, newDeck)
                _decks.value = currentList
            } catch (e: Exception) {
                Log.e("FirebaseError", "Lỗi tạo Deck: ${e.message}")
            }
        }
    }

    fun updateDeck(deckId: String, newName: String, newDescription: String) {
        viewModelScope.launch {
            try {
                db.collection("decks").document(deckId)
                    .update(mapOf("name" to newName, "description" to newDescription)).await()

                val currentList = _decks.value.toMutableList()
                val index = currentList.indexOfFirst { it.id == deckId }
                if (index != -1) {
                    currentList[index] = currentList[index].copy(name = newName, description = newDescription)
                    _decks.value = currentList
                }
            } catch (e: Exception) {
                Log.e("FirebaseError", "Lỗi sửa Deck: ${e.message}")
            }
        }
    }

    fun deleteDeck(deckId: String) {
        viewModelScope.launch {
            try {
                db.collection("decks").document(deckId).delete().await()
                _decks.value = _decks.value.filter { it.id != deckId }
            } catch (e: Exception) {
                Log.e("FirebaseError", "Lỗi xóa Deck: ${e.message}")
            }
        }
    }

    fun shareDeckWithEmail(deckId: String, email: String) {
        viewModelScope.launch {
            try {
                val userSnapshot = db.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .await()

                if (!userSnapshot.isEmpty) {
                    val friendId = userSnapshot.documents.first().id
                    db.collection("decks").document(deckId)
                        .update("sharedWith", FieldValue.arrayUnion(friendId))
                        .await()

                    // Cập nhật State cục bộ
                    val currentList = _decks.value.toMutableList()
                    val index = currentList.indexOfFirst { it.id == deckId }
                    if (index != -1) {
                        val currentShared = currentList[index].sharedWith.toMutableList()
                        if (!currentShared.contains(friendId)) {
                            currentShared.add(friendId)
                            currentList[index] = currentList[index].copy(sharedWith = currentShared)
                            _decks.value = currentList
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("FirebaseError", "Lỗi chia sẻ Deck: ${e.message}")
            }
        }
    }

    fun loadVocabs(deckId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = db.collection("decks").document(deckId).collection("vocabs").get().await()
                val now = System.currentTimeMillis()
                val loadedVocabs = snapshot.toObjects(VocabItem::class.java).sortedWith(
                    compareBy<VocabItem> { it.nextReviewTime > now } // Đưa các từ đến hạn ôn lên đầu
                        .thenBy { it.nextReviewTime } // Ưu tiên các từ quá hạn sâu nhất
                )
                _vocabs.value = loadedVocabs
            } catch (e: Exception) {
                Log.e("FirebaseError", "Lỗi tải Vocabs: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addVocab(deckId: String, kanji: String, kana: String, meaning: String, example: String, exampleMeaning: String, onSuccess: () -> Unit) {
        val vocabId = UUID.randomUUID().toString()
        val newVocab = VocabItem(id = vocabId, deckId = deckId, kanji = kanji, kana = kana, meaning = meaning, example = example, exampleMeaning = exampleMeaning, level = 0, nextReviewTime = System.currentTimeMillis())

        viewModelScope.launch {
            _isLoading.value = true
            try {
                db.collection("decks").document(deckId).collection("vocabs").document(vocabId).set(newVocab).await()
                val currentList = _vocabs.value.toMutableList()
                currentList.add(0, newVocab)
                _vocabs.value = currentList
                onSuccess()
            } catch (e: Exception) {
                Log.e("FirebaseError", "Lỗi thêm Vocab: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateVocab(deckId: String, vocabId: String, newTerm: String, newDefinition: String) {
        viewModelScope.launch {
            try {
                db.collection("decks").document(deckId).collection("vocabs").document(vocabId)
                    .update(mapOf(
                        "kanji" to "",
                        "kana" to newTerm,
                        "meaning" to newDefinition
                    )).await()

                val currentList = _vocabs.value.toMutableList()
                val index = currentList.indexOfFirst { it.id == vocabId }
                if (index != -1) {
                    currentList[index] = currentList[index].copy(
                        kanji = "",
                        kana = newTerm,
                        meaning = newDefinition
                    )
                    _vocabs.value = currentList
                }
            } catch (e: Exception) {
                Log.e("FirebaseError", "Lỗi sửa từ vựng: ${e.message}")
            }
        }
    }

    fun deleteVocab(deckId: String, vocabId: String) {
        viewModelScope.launch {
            try {
                db.collection("decks").document(deckId).collection("vocabs").document(vocabId).delete().await()
                _vocabs.value = _vocabs.value.filter { it.id != vocabId }
            } catch (e: Exception) {
                Log.e("FirebaseError", "Lỗi xóa từ vựng: ${e.message}")
            }
        }
    }

    private suspend fun incrementMasteredCount() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = db.collection("users").document(userId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentCount = snapshot.getLong("mastered_vocab_count") ?: 0L
            transaction.update(userRef, "mastered_vocab_count", currentCount + 1)
        }.await()
    }

    fun reviewVocab(vocab: VocabItem, difficulty: String, onNextCard: () -> Unit) {
        val now = System.currentTimeMillis()
        var newLevel = vocab.level
        var nextReview = now

        when (difficulty) {
            "Chưa thuộc" -> {
                newLevel = 0
                nextReview = now + 60 * 1000L
            }
            "Đã thuộc" -> {
                newLevel = 3
                // Đẩy lịch ôn tập sang rất xa, coi như đã thuộc hoàn toàn (ko dùng SRS nữa)
                nextReview = now + 365L * 24 * 60 * 60 * 1000L
            }
        }

        val isNewlyMastered = newLevel >= 3 && vocab.level < 3

        viewModelScope.launch {
            try {
                // 1. Cập nhật trên Firebase
                db.collection("decks").document(vocab.deckId).collection("vocabs").document(vocab.id)
                    .update("level", newLevel, "nextReviewTime", nextReview).await()

                if (isNewlyMastered) { incrementMasteredCount() }

                // 2. Cập nhật State cục bộ để giao diện DeckDetailScreen thay đổi ngay lập tức
                val currentList = _vocabs.value.toMutableList()
                val index = currentList.indexOfFirst { it.id == vocab.id }
                if (index != -1) {
                    currentList[index] = currentList[index].copy(level = newLevel, nextReviewTime = nextReview)
                    _vocabs.value = currentList
                }

                onNextCard()
            } catch (e: Exception) {
                Log.e("VocabViewModel", "Lỗi ôn tập: ${e.message}")
            }
        }
    }

    fun generateQuiz(deckId: String): List<QuizQuestion> {
        val currentVocabs = _vocabs.value
        if (currentVocabs.size < 4) return emptyList()

        return currentVocabs.map { vocab ->
            val wrongOptions = currentVocabs.filter { it.id != vocab.id }.shuffled().take(3).map { it.meaning }
            val allOptions = (wrongOptions + vocab.meaning).shuffled()
            QuizQuestion(vocab = vocab, options = allOptions, correctAnswer = vocab.meaning)
        }.shuffled()
    }
}