package com.example.kotobee.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kotobee.R
import com.example.kotobee.ui.auth.AuthState

private data class LevelChoice(
    val level: String,
    val title: String,
    val description: String
)

@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = viewModel()
) {
    val onboardingState by viewModel.onboardingState.collectAsState()
    val levels = listOf(
        LevelChoice("N5", "Mới bắt đầu", "Biết hiragana/katakana hoặc đang học nền tảng."),
        LevelChoice("N4", "Sơ cấp vững hơn", "Có thể đọc câu ngắn và muốn mở rộng ngữ pháp."),
        LevelChoice("N3", "Trung cấp", "Đã học khá nhiều nhưng cần luyện đều hơn."),
        LevelChoice("N2", "Trung cao cấp", "Ưu tiên đọc, nghe và ôn thi nghiêm túc."),
        LevelChoice("N1", "Nâng cao", "Muốn duy trì phản xạ và vốn từ chuyên sâu.")
    )
    val goals = listOf("Ôn JLPT", "Giao tiếp", "Đọc hiểu", "Du học/Công việc")
    val skills = listOf("Từ vựng", "Ngữ pháp", "Đọc hiểu", "Hán tự")

    var selectedLevel by remember { mutableStateOf("N5") }
    var selectedGoal by remember { mutableStateOf(goals.first()) }
    val selectedSkills = remember { mutableStateListOf("Từ vựng", "Hán tự") }

    LaunchedEffect(onboardingState) {
        if (onboardingState is AuthState.Success) {
            navController.navigate("home") {
                popUpTo("onboarding") { inclusive = true }
            }
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFF8FAFC))))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "KotoBee",
            modifier = Modifier.size(150.dp)
        )

        Text(
            text = "KotoBee cần biết bạn đang ở đâu",
            color = Color(0xFF333333),
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Khảo sát ngắn này chỉ chạy cho tài khoản mới để gợi ý nội dung học phù hợp.",
            color = Color(0xFF757575),
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier.padding(top = 6.dp, bottom = 18.dp)
        )

        SurveySection(title = "Trình độ hiện tại") {
            levels.forEach { choice ->
                LevelChoiceCard(
                    choice = choice,
                    selected = selectedLevel == choice.level,
                    onClick = { selectedLevel = choice.level }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        SurveySection(title = "Mục tiêu chính") {
            TwoColumnChips(
                options = goals,
                selected = selectedGoal,
                onSelected = { selectedGoal = it }
            )
        }

        SurveySection(title = "Kỹ năng muốn ưu tiên") {
            TwoColumnChips(
                options = skills,
                selectedValues = selectedSkills,
                onToggle = { skill ->
                    if (selectedSkills.contains(skill)) {
                        if (selectedSkills.size > 1) selectedSkills.remove(skill)
                    } else {
                        selectedSkills.add(skill)
                    }
                }
            )
        }

        if (onboardingState is AuthState.Error) {
            Text(
                text = (onboardingState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 10.dp)
            )
        }

        Button(
            onClick = {
                viewModel.saveInitialSurvey(
                    selectedLevel = selectedLevel,
                    learningGoal = selectedGoal,
                    focusSkills = selectedSkills.toList()
                )
            },
            enabled = onboardingState != AuthState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
            contentPadding = PaddingValues()
        ) {
            if (onboardingState == AuthState.Loading) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
            } else {
                Text("Bắt đầu học", color = Color.White, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun SurveySection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, Color(0xFFE53935)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = Color(0xFF333333), fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun LevelChoiceCard(
    choice: LevelChoice,
    selected: Boolean,
    onClick: () -> Unit
) {
    val accent = if (selected) Color(0xFFE53935) else Color(0xFFE5E7EB)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, if (selected) Color(0xFFE53935) else Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = if (selected) Color(0xFFE53935) else Color.White,
            border = BorderStroke(1.dp, accent),
            modifier = Modifier.size(42.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (selected) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFF757575), modifier = Modifier.size(20.dp))
                }
            }
        }
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("${choice.level} · ${choice.title}", color = Color(0xFF333333), fontWeight = FontWeight.Bold)
            Text(choice.description, color = Color(0xFF757575), fontSize = 12.sp, lineHeight = 17.sp)
        }
    }
}

@Composable
private fun TwoColumnChips(
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.chunked(2).forEach { rowOptions ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                rowOptions.forEach { option ->
                    SelectableChip(
                        label = option,
                        selected = selected == option,
                        onClick = { onSelected(option) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowOptions.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TwoColumnChips(
    options: List<String>,
    selectedValues: List<String>,
    onToggle: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.chunked(2).forEach { rowOptions ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                rowOptions.forEach { option ->
                    SelectableChip(
                        label = option,
                        selected = selectedValues.contains(option),
                        onClick = { onToggle(option) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowOptions.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SelectableChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, maxLines = 1) },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color.White,
            selectedLabelColor = Color(0xFFE53935)
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = Color(0xFFE53935),
            selectedBorderColor = Color(0xFFE53935)
        )
    )
}
