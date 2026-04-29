package com.example.kotobee.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kotobee.ui.admin.AddGrammarScreen
import com.example.kotobee.ui.auth.login.LoginScreen
import com.example.kotobee.ui.auth.register.RegisterScreen
import com.example.kotobee.ui.home.MainScreen
import com.example.kotobee.ui.lessons.HandwritingPracticeScreen
import com.example.kotobee.ui.vocab.DeckListScreen
import com.example.kotobee.ui.vocab.VocabularyPracticeScreen
import com.example.kotobee.ui.lessons.ListeningPracticeScreen
import com.example.kotobee.ui.lessons.reading.ReadingPracticeScreen
import com.example.kotobee.ui.lessons.grammar.GrammarPracticeScreen
import com.example.kotobee.ui.lessons.grammar.GrammarDashboardScreen
import com.example.kotobee.ui.lessons.grammar.GrammarDetailScreen
import com.example.kotobee.ui.lessons.grammar.GrammarListScreen
import com.example.kotobee.ui.lessons.grammar.GrammarViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val currentUser = FirebaseAuth.getInstance().currentUser

    val startDestination = if (currentUser != null) "home" else "login"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("home") { MainScreen(navController) }
        composable("deck_list") {
            DeckListScreen(onDeckClick = { deckId ->
                navController.navigate("vocab_practice/$deckId")
            })
        }
        composable(
            route = "vocab_practice/{deckId}",
            arguments = listOf(navArgument("deckId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId") ?: ""
            VocabularyPracticeScreen(
                deckId = deckId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("listening_practice") { ListeningPracticeScreen() }
        composable("reading_practice") { ReadingPracticeScreen() }
        composable("writing_practice") { HandwritingPracticeScreen() }

        // --- CHÈN THÊM ROUTE NGỮ PHÁP Ở ĐÂY ---
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

            // Gọi load dữ liệu khi vào trang
            LaunchedEffect(grammarId) {
                grammarViewModel.loadGrammarDetail(grammarId)
            }
            GrammarDetailScreen(navController, grammarViewModel)
        }

        composable("admin_dashboard") {
            AddGrammarScreen() // Khi nào code trang Admin thì gọi hàm ở đây
        }
    }
}