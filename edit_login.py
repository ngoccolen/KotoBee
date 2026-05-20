import os

file_path = 'app/src/main/java/com/example/kotobee/ui/auth/login/LoginScreen.kt'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Replace 1: Add import
content = content.replace('import androidx.compose.foundation.shape.RoundedCornerShape',
                          'import androidx.compose.foundation.shape.RoundedCornerShape\nimport androidx.compose.foundation.border')

# Replace 2: Background gradient
content = content.replace('colors = listOf(Color.White, Color.White) // Nền trắng thuần',
                          'colors = listOf(Color(0xFFFFEBEE), Color(0xFFFFEBEE)) // Nền đỏ nhạt')

# Replace 3: Card border and offset
content = content.replace('border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE53935)),\n                    modifier = Modifier.fillMaxWidth()',
                          'border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFB71C1C)),\n                    modifier = Modifier.fillMaxWidth().offset(y = (-20).dp)')

# Replace 4: Google button
old_google = '''                            if (authState == AuthState.Loading && loginType == "google") {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = primaryAccentColor, strokeWidth = 2.dp)
                            } else {
                                Text(
                                    text = "Đăng nhập bằng Google",'''

new_google = '''                            if (authState == AuthState.Loading && loginType == "google") {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = primaryAccentColor, strokeWidth = 2.dp)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_google),
                                        contentDescription = "Google Icon",
                                        modifier = Modifier.size(24.dp),
                                        tint = Color.Unspecified
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Đăng nhập bằng Google",'''

content = content.replace(old_google, new_google)

# Adding the closing brace for Row
old_google_end = '''                                    text = "Đăng nhập bằng Google",
                                    fontSize = 16.sp,
                                    color = Color(0xFF555555),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }'''

new_google_end = '''                                    text = "Đăng nhập bằng Google",
                                    fontSize = 16.sp,
                                    color = Color(0xFF555555),
                                    fontWeight = FontWeight.Medium
                                )
                                }
                            }
                        }'''
content = content.replace(old_google_end, new_google_end)


# Replace 5: Forgot Password Dialog
old_dialog = '''    if (showForgotDialog) {
        androidx.compose.material3.AlertDialog(
            // ... (Phần Dialog giữ nguyên như cũ của bạn)
            onDismissRequest = { showForgotDialog = false },'''

new_dialog = '''    if (showForgotDialog) {
        androidx.compose.material3.AlertDialog(
            // ... (Phần Dialog giữ nguyên như cũ của bạn)
            onDismissRequest = { showForgotDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.border(1.5.dp, Color(0xFFB71C1C), RoundedCornerShape(28.dp)),'''

content = content.replace(old_dialog, new_dialog)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Done replacing")
