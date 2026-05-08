package com.example.kotobee.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kotobee.data.database.AppDatabase
import com.example.kotobee.data.model.KanjiEntity
import com.example.kotobee.data.repository.KanjiRepository

// --- IMPORT ĐẦY ĐỦ CÁC MÀN HÌNH Ở ĐÂY ---
import com.example.kotobee.ui.auth.login.LoginScreen
import com.example.kotobee.ui.auth.register.RegisterScreen
import com.example.kotobee.ui.home.MainScreen

// --- IMPORT CHO VOCAB (FLASHCARD & QUIZ) ---
import com.example.kotobee.ui.lessons.vocab.DeckListScreen
import com.example.kotobee.ui.lessons.vocab.DeckDetailScreen
import com.example.kotobee.ui.lessons.vocab.AddVocabScreen
import com.example.kotobee.ui.lessons.vocab.VocabularyPracticeScreen
import com.example.kotobee.ui.lessons.vocab.VocabManagerViewModel
import com.example.kotobee.ui.lessons.vocab.QuizScreen

// --- IMPORT CHO READING ---
import com.example.kotobee.ui.lessons.reading.ReadingPracticeScreen
import com.example.kotobee.ui.lessons.reading.NewsListScreen
import com.example.kotobee.ui.lessons.reading.ReadingViewModel

// --- IMPORT CHO GRAMMAR ---
import com.example.kotobee.ui.lessons.grammar.GrammarPracticeScreen
import com.example.kotobee.ui.lessons.grammar.GrammarDashboardScreen
import com.example.kotobee.ui.lessons.grammar.GrammarDetailScreen
import com.example.kotobee.ui.lessons.grammar.GrammarListScreen
import com.example.kotobee.ui.lessons.grammar.GrammarViewModel

// --- IMPORT CHO LISTENING ---
import com.example.kotobee.ui.lessons.listening.ListeningListScreen
import com.example.kotobee.ui.lessons.listening.ListeningPracticeScreen
import com.example.kotobee.ui.lessons.listening.ListeningViewModel
import com.example.kotobee.ui.lessons.writing.AppViewModelFactory
import com.example.kotobee.ui.lessons.writing.KanjiListScreen

// --- IMPORT CHO KANJI WRITING ---
import com.example.kotobee.ui.lessons.writing.KanjiPracticeViewModel
import com.example.kotobee.ui.lessons.writing.PracticeScreen

import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val currentUser = FirebaseAuth.getInstance().currentUser

    val startDestination = if (currentUser != null) "home" else "login"

    // TẠO VIEWMODEL DÙNG CHUNG CHO LUỒNG TỪ VỰNG Ở ĐÂY
    val vocabViewModel: VocabManagerViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("home") { MainScreen(navController) }

        // ====================================================
        // --- ROUTE CHỐNG CRASH CHO CÁC MỤC CHƯA PHÁT TRIỂN ---
        // ====================================================
        composable("lessons_list") {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Màn hình này đang được phát triển!")
            }
        }

        // ====================================================
        // --- LUỒNG FLASHCARD TỪ VỰNG ---
        // ====================================================
        composable("deck_list") {
            DeckListScreen(
                viewModel = vocabViewModel,
                onDeckClick = { deckId ->
                    navController.navigate("deck_detail/$deckId")
                }
            )
        }

        composable(
            route = "deck_detail/{deckId}",
            arguments = listOf(navArgument("deckId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId") ?: ""
            DeckDetailScreen(
                deckId = deckId,
                viewModel = vocabViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddVocab = { id -> navController.navigate("add_vocab/$id") },
                onNavigateToPractice = { id -> navController.navigate("vocab_practice/$id") },
                onNavigateToQuiz = { id -> navController.navigate("quiz/$id") }
            )
        }

        composable(
            route = "add_vocab/{deckId}",
            arguments = listOf(navArgument("deckId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId") ?: ""
            AddVocabScreen(
                deckId = deckId,
                viewModel = vocabViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "vocab_practice/{deckId}",
            arguments = listOf(navArgument("deckId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId") ?: ""
            VocabularyPracticeScreen(
                deckId = deckId,
                viewModel = vocabViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "quiz/{deckId}",
            arguments = listOf(navArgument("deckId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId") ?: ""
            QuizScreen(
                deckId = deckId,
                viewModel = vocabViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ====================================================
        // --- LUỒNG NGHE HỘI THOẠI NHK (LISTENING) ---
        // ====================================================
        composable("listening_list") {
            ListeningListScreen(
                onLessonClick = { lessonId ->
                    navController.navigate("listening_practice/$lessonId")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "listening_practice/{lessonId}",
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
            val listeningViewModel: ListeningViewModel = viewModel()

            LaunchedEffect(lessonId) {
                listeningViewModel.loadLessonData(lessonId)
            }

            ListeningPracticeScreen(
                viewModel = listeningViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // ====================================================
        // --- LUỒNG ĐỌC BÁO NHK (READING) ---
        // ====================================================
        composable("news_list") {
            val readingViewModel: ReadingViewModel = viewModel()
            NewsListScreen(
                viewModel = readingViewModel,
                onArticleClick = { newsId ->
                    navController.navigate("reading_detail/$newsId")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "reading_detail/{newsId}",
            arguments = listOf(navArgument("newsId") { type = NavType.StringType })
        ) { backStackEntry ->
            val newsId = backStackEntry.arguments?.getString("newsId") ?: ""
            val readingViewModel: ReadingViewModel = viewModel()

            ReadingPracticeScreen(
                newsId = newsId,
                viewModel = readingViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // ====================================================
        // --- LUỒNG NGỮ PHÁP ---
        // ====================================================
        composable("grammar_dashboard") {
            GrammarDashboardScreen(navController)
        }

        composable(
            route = "grammar_list/{level}",
            arguments = listOf(navArgument("level") { type = NavType.StringType })
        ) { backStackEntry ->
            val level = backStackEntry.arguments?.getString("level") ?: "N4"
            val grammarViewModel: GrammarViewModel = viewModel()
            GrammarListScreen(navController, grammarViewModel, level)
        }

        composable("grammar_practice") {
            GrammarPracticeScreen()
        }

        composable(
            route = "grammar_detail/{grammarId}",
            arguments = listOf(navArgument("grammarId") { type = NavType.StringType })
        ) { backStackEntry ->
            val grammarId = backStackEntry.arguments?.getString("grammarId") ?: ""
            val grammarViewModel: GrammarViewModel = viewModel()

            LaunchedEffect(grammarId) {
                grammarViewModel.loadGrammarDetail(grammarId)
            }
            GrammarDetailScreen(navController, grammarViewModel)
        }

        // ====================================================
        // --- LUỒNG HÁN TỰ (KANJI WRITING) ---
        // ====================================================
        composable("kanji_list") {
            // Đã thay tham số false cuối cùng thành chuỗi "" để fix lỗi String? expected
            val mockList = listOf(
                KanjiEntity(
                    character = "一",
                    meaning = "MỘT",
                    onyomi = "イチ",
                    kunyomi = "ひと",
                    strokeCount = 1,
                    radical = "一",
                    jlptLevel = 5,
                    ""
                ),
                KanjiEntity(
                    character = "二",
                    meaning = "HAI",
                    onyomi = "ニ",
                    kunyomi = "ふた",
                    strokeCount = 2,
                    radical = "二",
                    jlptLevel = 5,
                    ""
                ),
                KanjiEntity(
                    character = "日",
                    meaning = "NHẬT",
                    onyomi = "ニチ",
                    kunyomi = "ひ",
                    strokeCount = 4,
                    radical = "日",
                    jlptLevel = 5,
                    ""
                ),
                KanjiEntity(
                    character = "本",
                    meaning = "BẢN",
                    onyomi = "ホン",
                    kunyomi = "もと",
                    strokeCount = 5,
                    radical = "木",
                    jlptLevel = 5,
                    ""
                )
            )

            KanjiListScreen(
                kanjiList = mockList,
                onKanjiClick = { clickedCharacter ->
                    navController.navigate("kanji_practice/$clickedCharacter")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "kanji_practice/{character}",
            arguments = listOf(navArgument("character") { type = NavType.StringType })
        ) { backStackEntry ->
            val character = backStackEntry.arguments?.getString("character") ?: "一"
            val context = LocalContext.current

            val repository = remember { KanjiRepository(context) }
            val factory = remember { AppViewModelFactory(repository) }
            val kanjiViewModel: KanjiPracticeViewModel = viewModel(factory = factory)

            LaunchedEffect(character) {
                kanjiViewModel.loadKanjiData(character)
            }

            val state by kanjiViewModel.state.collectAsState()

            PracticeScreen(
                state = state,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}