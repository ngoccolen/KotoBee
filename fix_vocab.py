import re

# 1. Update VocabScreen.kt
with open(r'd:\Documents\KotoBee\app\src\main\java\com\example\kotobee\ui\lessons\vocab\VocabScreen.kt', 'r', encoding='utf-8') as f:
    code = f.read()

bottom_bar_old = """@Composable
fun SpacedRepetitionBottomBar(onReview: (String) -> Unit) {
    Surface(color = AppBackground, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RepetitionButton(
                text = "Chưa thuộc",
                containerColor = Color.White,
                contentColor = Color(0xFFEF4444),
                modifier = Modifier.weight(1f)
            ) { onReview("Chưa thuộc") }

            RepetitionButton(
                text = "Nhớ",
                containerColor = Color(0xFFFFF7ED),
                contentColor = Color(0xFFF97316),
                modifier = Modifier.weight(1f)
            ) { onReview("Nhớ") }

            RepetitionButton(
                text = "Rất dễ",
                containerColor = Color(0xFFF0FDF4),
                contentColor = Color(0xFF22C55E),
                modifier = Modifier.weight(1f)
            ) { onReview("Rất dễ") }
        }
    }
}"""

bottom_bar_new = """@Composable
fun SpacedRepetitionBottomBar(onReview: (String) -> Unit) {
    Surface(color = AppBackground, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RepetitionButton(
                text = "Chưa thuộc",
                containerColor = Color.White,
                contentColor = Color(0xFFEF4444),
                modifier = Modifier.weight(1f)
            ) { onReview("Chưa thuộc") }

            RepetitionButton(
                text = "Đã thuộc",
                containerColor = Color.White,
                contentColor = Color(0xFF22C55E),
                modifier = Modifier.weight(1f)
            ) { onReview("Đã thuộc") }
        }
    }
}"""

code = code.replace(bottom_bar_old, bottom_bar_new)

with open(r'd:\Documents\KotoBee\app\src\main\java\com\example\kotobee\ui\lessons\vocab\VocabScreen.kt', 'w', encoding='utf-8') as f:
    f.write(code)

# 2. Update VocabManagerViewModel.kt
with open(r'd:\Documents\KotoBee\app\src\main\java\com\example\kotobee\ui\lessons\vocab\VocabManagerViewModel.kt', 'r', encoding='utf-8') as f:
    code = f.read()

review_logic_old = """        when (difficulty) {
            "Chưa thuộc" -> {
                newLevel = 0
                nextReview = now + 60 * 1000L
            }
            "Nhớ" -> {
                newLevel += 1
                val intervalDays = when (newLevel) {
                    1 -> 1
                    2 -> 3
                    3 -> 7
                    4 -> 14
                    else -> 30
                }
                nextReview = now + intervalDays * 24 * 60 * 60 * 1000L
            }
            "Rất dễ" -> {
                newLevel += 2
                val intervalDays = when (newLevel) {
                    1 -> 2
                    2 -> 5
                    3 -> 12
                    4 -> 25
                    else -> 45
                }
                nextReview = now + intervalDays * 24 * 60 * 60 * 1000L
            }
        }"""

review_logic_new = """        when (difficulty) {
            "Chưa thuộc" -> {
                newLevel = 0
                nextReview = now + 60 * 1000L
            }
            "Đã thuộc" -> {
                newLevel = 3
                // Đẩy lịch ôn tập sang rất xa, coi như đã thuộc hoàn toàn (ko dùng SRS nữa)
                nextReview = now + 365L * 24 * 60 * 60 * 1000L
            }
        }"""
code = code.replace(review_logic_old, review_logic_new)

with open(r'd:\Documents\KotoBee\app\src\main\java\com\example\kotobee\ui\lessons\vocab\VocabManagerViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(code)

# 3. Update QuizScreen.kt
with open(r'd:\Documents\KotoBee\app\src\main\java\com\example\kotobee\ui\lessons\vocab\QuizScreen.kt', 'r', encoding='utf-8') as f:
    code = f.read()

quiz_btn_old = """                    Button(
                        onClick = { if (selectedAnswer == null) selectedAnswer = option },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).heightIn(min = 60.dp)
                            .border(1.dp, if (selectedAnswer != null && isCorrect) TextGreen else Color.Transparent, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = TextDark)
                    ) {
                        Text(
                            option,
                            fontSize = 16.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }"""

quiz_btn_new = """                    val borderColor = when {
                        selectedAnswer != null && isCorrect -> TextGreen
                        selectedAnswer != null && isSelected && !isCorrect -> TextRed
                        else -> Color(0xFFE5E7EB) // Gray border
                    }

                    Button(
                        onClick = { if (selectedAnswer == null) selectedAnswer = option },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).heightIn(min = 60.dp)
                            .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = TextDark)
                    ) {
                        Text(
                            option,
                            fontSize = 16.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }"""
code = code.replace(quiz_btn_old, quiz_btn_new)

with open(r'd:\Documents\KotoBee\app\src\main\java\com\example\kotobee\ui\lessons\vocab\QuizScreen.kt', 'w', encoding='utf-8') as f:
    f.write(code)

print("Done")
