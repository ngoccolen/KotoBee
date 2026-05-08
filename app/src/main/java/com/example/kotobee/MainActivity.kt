package com.example.kotobee

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.kotobee.ui.auth.register.RegisterScreen
import com.example.kotobee.ui.lessons.vocab.VocabularyPracticeScreen
import com.example.kotobee.ui.navigation.NavGraph
import com.example.kotobee.ui.theme.KotoBeeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KotoBeeTheme {
                NavGraph()
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    KotoBeeTheme {
        NavGraph()
    }
}
