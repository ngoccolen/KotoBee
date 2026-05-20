package com.example.kotobee.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.kotobee.data.service.CloudinaryService
import com.example.kotobee.di.AppContainer

// --- IMPORT ĐẦY ĐỦ CÁC MÀN HÌNH Ở ĐÂY ---
import com.example.kotobee.ui.auth.login.LoginScreen
import com.example.kotobee.ui.auth.register.RegisterScreen
import com.example.kotobee.ui.community.CreatePostScreen
import com.example.kotobee.ui.community.CreatePostViewModel
import com.example.kotobee.ui.home.MainScreen

// --- IMPORT CHO VOCAB (FLASHCARD & QUIZ) ---
import com.example.kotobee.ui.lessons.vocab.DeckListScreen
import com.example.kotobee.ui.lessons.vocab.DeckDetailScreen
import com.example.kotobee.ui.lessons.vocab.AddVocabScreen
import com.example.kotobee.ui.lessons.vocab.VocabularyPracticeScreen
import com.example.kotobee.ui.lessons.vocab.VocabManagerViewModel
import com.example.kotobee.ui.lessons.vocab.QuizScreen
import com.example.kotobee.ui.lessons.vocab.MatchingGameScreen

// --- IMPORT CHO READING ---
import com.example.kotobee.ui.lessons.reading.ReadingPracticeScreen
import com.example.kotobee.ui.lessons.reading.NewsListScreen
import com.example.kotobee.ui.lessons.reading.ReadingViewModel

// --- IMPORT CHO GRAMMAR ---
// --- IMPORT CHO GRAMMAR ---
import com.example.kotobee.ui.lessons.grammar.GrammarPracticeScreen
import com.example.kotobee.ui.lessons.grammar.GrammarDashboardScreen
import com.example.kotobee.ui.lessons.grammar.GrammarDetailScreen
import com.example.kotobee.ui.lessons.grammar.GrammarListScreen
import com.example.kotobee.ui.lessons.grammar.GrammarViewModel

// --- IMPORT CHO LISTENING ---
import com.example.kotobee.ui.lessons.writing.AppViewModelFactory
import com.example.kotobee.ui.lessons.writing.KanjiListScreen

// --- IMPORT CHO SHADOWING ---
import com.example.kotobee.ui.lessons.shadowing.ShadowingViewModel
import com.example.kotobee.ui.lessons.shadowing.ShadowingListScreen
import com.example.kotobee.ui.lessons.shadowing.ShadowingPracticeScreen
import com.example.kotobee.ui.lessons.speaking.SpeakingChatScreen
import com.example.kotobee.ui.lessons.speaking.SpeakingHubScreen
import com.example.kotobee.ui.lessons.speaking.SpeakingPairRoomScreen
import com.example.kotobee.ui.lessons.speaking.SpeakingPairViewModel
import com.example.kotobee.ui.lessons.speaking.SpeakingTopicListScreen
import com.example.kotobee.ui.lessons.speaking.SpeakingViewModel

// --- IMPORT CHO KANJI WRITING ---
import com.example.kotobee.ui.lessons.writing.KanjiPracticeViewModel
import com.example.kotobee.ui.lessons.writing.PracticeScreen

import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.layout.systemBarsPadding

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val currentUser = FirebaseAuth.getInstance().currentUser

    val startDestination = if (currentUser != null) "home" else "login"

    // TẠO VIEWMODEL DÙNG CHUNG CHO LUỒNG TỪ VỰNG Ở ĐÂY
    val vocabViewModel: VocabManagerViewModel = viewModel()

    Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable("login") { LoginScreen(navController) }
            composable("register") { RegisterScreen(navController) }
            composable("home") { MainScreen(navController) }

            // Vocab flow
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
                    onNavigateToAddVocab = { selectedDeckId ->
                        navController.navigate("add_vocab/$selectedDeckId")
                    },
                    onNavigateToPractice = { selectedDeckId ->
                        navController.navigate("vocab_practice/$selectedDeckId")
                    },
                    onNavigateToQuiz = { selectedDeckId ->
                        navController.navigate("vocab_quiz/$selectedDeckId")
                    },
                    onNavigateToMatch = { selectedDeckId ->
                        navController.navigate("vocab_match/$selectedDeckId")
                    }
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
                route = "vocab_quiz/{deckId}",
                arguments = listOf(navArgument("deckId") { type = NavType.StringType })
            ) { backStackEntry ->
                val deckId = backStackEntry.arguments?.getString("deckId") ?: ""

                QuizScreen(
                    deckId = deckId,
                    viewModel = vocabViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "vocab_match/{deckId}",
                arguments = listOf(navArgument("deckId") { type = NavType.StringType })
            ) { backStackEntry ->
                val deckId = backStackEntry.arguments?.getString("deckId") ?: ""

                MatchingGameScreen(
                    deckId = deckId,
                    viewModel = vocabViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("create_post") {
                val context = LocalContext.current

                val appContainer = remember {
                    AppContainer(context)
                }

                val createPostViewModel: CreatePostViewModel = viewModel(
                    factory = CreatePostViewModel.Factory(
                        repository = appContainer.communityRepository,
                        cloudinaryService = appContainer.cloudinaryService
                    )
                )

                CreatePostScreen(
                    viewModel = createPostViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // ====================================================
            // --- ROUTE CHỐNG CRASH CHO CÁC MỤC CHƯA PHÁT TRIỂN ---
            // ====================================================
            composable("lessons_list") {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Màn hình này đang được phát triển!")
                }
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
                GrammarPracticeScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = "grammar_practice/{grammarId}",
                arguments = listOf(navArgument("grammarId") { type = NavType.StringType })
            ) { backStackEntry ->
                val grammarId = backStackEntry.arguments?.getString("grammarId") ?: "n5_te_kudasai"
                val grammarViewModel: GrammarViewModel = viewModel()
                GrammarPracticeScreen(
                    grammarId = grammarId,
                    viewModel = grammarViewModel,
                    onBackClick = { navController.popBackStack() }
                )
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
                val context = LocalContext.current
                val repository = remember { KanjiRepository(context) }
                var kanjiList by remember { mutableStateOf<List<KanjiEntity>>(emptyList()) }
                var isLoading by remember { mutableStateOf(true) }
                var errorMessage by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    isLoading = true
                    errorMessage = null
                    runCatching {
                        repository.getKanjiList()
                    }.onSuccess { data ->
                        kanjiList = data
                    }.onFailure { error ->
                        kanjiList = emptyList()
                        errorMessage = error.message
                    }
                    isLoading = false
                }

                KanjiListScreen(
                    kanjiList = kanjiList,
                    isLoading = isLoading,
                    errorMessage = errorMessage,
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

            // ====================================================
            // --- LUỒNG LUYỆN NÓI (SHADOWING) ---
            // ====================================================
            composable("speaking_hub") {
                SpeakingHubScreen(
                    onShadowingClick = { navController.navigate("shadowing_list") },
                    onConversationClick = { navController.navigate("speaking_topics") },
                    onPairConversationClick = { navController.navigate("speaking_pair_rooms") },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("shadowing_list") {
                val context = LocalContext.current
                val appContainer = remember { AppContainer(context.applicationContext) }
                val factory = remember { ShadowingViewModel.Factory(appContainer.shadowingRepository) }
                val viewModel: ShadowingViewModel = viewModel(factory = factory)

                ShadowingListScreen(
                    viewModel = viewModel,
                    onLessonClick = { lessonId ->
                        navController.navigate("shadowing_practice/$lessonId")
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = "shadowing_practice/{lessonId}",
                arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
            ) { backStackEntry ->
                val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
                val context = LocalContext.current
                val appContainer = remember { AppContainer(context.applicationContext) }
                val factory = remember { ShadowingViewModel.Factory(appContainer.shadowingRepository) }
                val viewModel: ShadowingViewModel = viewModel(factory = factory)

                ShadowingPracticeScreen(
                    lessonId = lessonId,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("speaking_topics") {
                val context = LocalContext.current
                val appContainer = remember { AppContainer(context.applicationContext) }
                val factory = remember { SpeakingViewModel.Factory(appContainer.speakingConversationRepository) }
                val viewModel: SpeakingViewModel = viewModel(factory = factory)

                SpeakingTopicListScreen(
                    viewModel = viewModel,
                    onTopicClick = { topicId ->
                        navController.navigate("speaking_chat/${Uri.encode(topicId)}")
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = "speaking_chat/{topicId}",
                arguments = listOf(navArgument("topicId") { type = NavType.StringType })
            ) { backStackEntry ->
                val topicId = backStackEntry.arguments?.getString("topicId") ?: ""
                val context = LocalContext.current
                val appContainer = remember { AppContainer(context.applicationContext) }
                val factory = remember { SpeakingViewModel.Factory(appContainer.speakingConversationRepository) }
                val viewModel: SpeakingViewModel = viewModel(factory = factory)

                SpeakingChatScreen(
                    topicId = topicId,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("speaking_pair_rooms") {
                val context = LocalContext.current
                val appContainer = remember { AppContainer(context.applicationContext) }
                val factory = remember { SpeakingPairViewModel.Factory(appContainer.speakingPairRepository) }
                val viewModel: SpeakingPairViewModel = viewModel(factory = factory)

                SpeakingPairRoomScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
