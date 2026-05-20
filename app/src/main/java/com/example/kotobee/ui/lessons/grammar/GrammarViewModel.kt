package com.example.kotobee.ui.lessons.grammar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotobee.data.model.Grammar
import com.example.kotobee.data.model.GrammarProgress
import com.example.kotobee.data.model.GrammarQuestion
import com.example.kotobee.data.model.GrammarQuizSaveResult
import com.example.kotobee.data.repository.GrammarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class GrammarLevelProgress(
    val level: String,
    val title: String,
    val description: String,
    val lessons: Int,
    val completed: Int
) {
    val progress: Float
        get() = if (lessons == 0) 0f else completed.toFloat() / lessons.toFloat()
}

data class GrammarLessonAccess(
    val grammar: Grammar,
    val unlocked: Boolean,
    val completed: Boolean,
    val previousTitle: String? = null,
    val bestScore: Int = 0
)

data class GrammarQuizSaveState(
    val isSaving: Boolean = false,
    val result: GrammarQuizSaveResult? = null,
    val errorMessage: String? = null
)

class GrammarViewModel : ViewModel() {
    private val repository = GrammarRepository()

    private val _allLessons = MutableStateFlow<List<Grammar>>(emptyList())
    val allLessons: StateFlow<List<Grammar>> = _allLessons

    private val _lessons = MutableStateFlow<List<Grammar>>(emptyList())
    val lessons: StateFlow<List<Grammar>> = _lessons

    private val _currentGrammar = MutableStateFlow<Grammar?>(null)
    val currentGrammar: StateFlow<Grammar?> = _currentGrammar

    private val _questions = MutableStateFlow<List<GrammarQuestion>>(emptyList())
    val questions: StateFlow<List<GrammarQuestion>> = _questions

    private val _progressMap = MutableStateFlow<Map<String, GrammarProgress>>(emptyMap())
    val progressMap: StateFlow<Map<String, GrammarProgress>> = _progressMap

    private val _lessonAccess = MutableStateFlow<List<GrammarLessonAccess>>(emptyList())
    val lessonAccess: StateFlow<List<GrammarLessonAccess>> = _lessonAccess

    private val _currentLessonAccess = MutableStateFlow<GrammarLessonAccess?>(null)
    val currentLessonAccess: StateFlow<GrammarLessonAccess?> = _currentLessonAccess

    private val _quizSaveState = MutableStateFlow(GrammarQuizSaveState())
    val quizSaveState: StateFlow<GrammarQuizSaveState> = _quizSaveState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loadOverview() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            runCatching {
                repository.getAllGrammarLessons() to repository.getGrammarProgress()
            }.onSuccess { (lessons, progress) ->
                _allLessons.value = lessons
                _progressMap.value = progress
            }.onFailure { error ->
                _allLessons.value = emptyList()
                _progressMap.value = emptyMap()
                _errorMessage.value = error.message
            }
            _isLoading.value = false
        }
    }

    fun loadLessonsByLevel(level: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            runCatching {
                repository.getGrammarLessonsByLevel(level) to repository.getGrammarProgress()
            }.onSuccess { (lessons, progress) ->
                _lessons.value = lessons
                _progressMap.value = progress
                _lessonAccess.value = buildGrammarLessonAccess(lessons, progress)
            }.onFailure { error ->
                _lessons.value = emptyList()
                _lessonAccess.value = emptyList()
                _errorMessage.value = error.message
            }
            _isLoading.value = false
        }
    }

    fun loadGrammarDetail(grammarId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _currentGrammar.value = null
            _currentLessonAccess.value = null
            _questions.value = emptyList()
            _quizSaveState.value = GrammarQuizSaveState()

            val lesson = runCatching { repository.getGrammarById(grammarId) }
                .getOrElse { error ->
                    _errorMessage.value = error.message
                    null
                }

            _currentGrammar.value = lesson
            _questions.value = runCatching { repository.getQuestionsForLesson(grammarId) }
                .getOrElse { emptyList() }

            lesson?.let { grammar ->
                val levelLessons = runCatching { repository.getGrammarLessonsByLevel(grammar.level) }
                    .getOrElse { emptyList() }
                val progress = runCatching { repository.getGrammarProgress() }
                    .getOrElse { emptyMap() }
                _progressMap.value = progress
                _lessonAccess.value = buildGrammarLessonAccess(levelLessons, progress)
                _currentLessonAccess.value = _lessonAccess.value.firstOrNull { it.grammar.id == grammar.id }
            }

            if (lesson == null && _errorMessage.value.isNullOrBlank()) {
                _errorMessage.value = "Không tìm thấy bài học ngữ pháp này."
            }
            _isLoading.value = false
        }
    }

    fun saveGrammarQuizResult(correctCount: Int, totalQuestions: Int) {
        val grammar = _currentGrammar.value ?: return
        if (totalQuestions <= 0) return

        viewModelScope.launch {
            val score = ((correctCount.toFloat() / totalQuestions.toFloat()) * 100).toInt()
            _quizSaveState.value = GrammarQuizSaveState(isSaving = true)

            runCatching {
                repository.saveGrammarQuizResult(
                    grammar = grammar,
                    score = score,
                    correctCount = correctCount,
                    totalQuestions = totalQuestions
                )
            }.onSuccess { result ->
                _quizSaveState.value = GrammarQuizSaveState(result = result)
                val progress = runCatching { repository.getGrammarProgress() }.getOrElse { _progressMap.value }
                _progressMap.value = progress
                _lessonAccess.value = buildGrammarLessonAccess(_lessonAccess.value.map { it.grammar }, progress)
                _currentLessonAccess.value = _lessonAccess.value.firstOrNull { it.grammar.id == grammar.id }
            }.onFailure { error ->
                _quizSaveState.value = GrammarQuizSaveState(
                    errorMessage = error.message ?: "Không thể lưu kết quả quiz."
                )
            }
        }
    }

    fun resetQuizSaveState() {
        _quizSaveState.value = GrammarQuizSaveState()
    }

    fun buildLevelProgress(): List<GrammarLevelProgress> {
        val source = _allLessons.value
        val progress = _progressMap.value
        return levels.map { level ->
            val levelLessons = source.filter { it.level.equals(level, ignoreCase = true) }
            GrammarLevelProgress(
                level = level,
                title = levelTitles[level].orEmpty(),
                description = levelDescriptions[level].orEmpty(),
                lessons = levelLessons.size,
                completed = levelLessons.count { progress[it.id]?.completed == true }
            )
        }
    }

    private val levels = listOf("N5", "N4", "N3", "N2", "N1")

    private val levelTitles = mapOf(
        "N5" to "Nền tảng",
        "N4" to "Mở rộng giao tiếp",
        "N3" to "Trung cấp",
        "N2" to "Sắc thái học thuật",
        "N1" to "Diễn đạt nâng cao"
    )

    private val levelDescriptions = mapOf(
        "N5" to "Câu cơ bản, mong muốn, yêu cầu và trạng thái.",
        "N4" to "Nghĩa vụ, xin phép, kinh nghiệm và thay đổi thói quen.",
        "N3" to "Mục đích, quyết định, thói quen và sắc thái tần suất.",
        "N2" to "Lập luận, tương phản, phạm vi và căn cứ suy luận.",
        "N1" to "Văn phong trang trọng, tin tức và diễn đạt giàu sắc thái."
    )
}

fun buildGrammarLessonAccess(
    lessons: List<Grammar>,
    progress: Map<String, GrammarProgress>
): List<GrammarLessonAccess> {
    return lessons.mapIndexed { index, grammar ->
        val currentProgress = progress[grammar.id]
        val previousLesson = lessons.getOrNull(index - 1)
        val unlocked = index == 0 || previousLesson?.let { progress[it.id]?.completed == true } == true

        GrammarLessonAccess(
            grammar = grammar,
            unlocked = unlocked,
            completed = currentProgress?.completed == true,
            previousTitle = previousLesson?.title,
            bestScore = currentProgress?.bestScore ?: 0
        )
    }
}
