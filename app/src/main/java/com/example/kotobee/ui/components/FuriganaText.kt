package com.example.kotobee.ui.components

import android.graphics.Color
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun FuriganaText(htmlContent: String, modifier: Modifier = Modifier) {
    // Định nghĩa CSS để WebView nhìn y hệt như Native Text của Compose
    val textColor = "#2C3E50" // Màu chữ tối
    val furiganaColor = "#E65100" // Màu cam cho Furigana dễ nhìn (bạn có thể đổi)

    val styledHtml = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body {
                    font-family: sans-serif;
                    font-size: 18px; /* Cỡ chữ chính */
                    line-height: 2.2; /* Tăng khoảng cách dòng để không bị đè Furigana */
                    color: $textColor;
                    background-color: transparent; /* Nền trong suốt */
                    padding: 0;
                    margin: 0;
                    word-wrap: break-word;
                }
                ruby {
                    ruby-align: center;
                }
                rt {
                    font-size: 11px; /* Cỡ chữ của Furigana */
                    color: $furiganaColor;
                    font-weight: bold;
                }
                /* NHK thỉnh thoảng có các thẻ a hoặc span thừa, ta reset lại */
                a { color: $textColor; text-decoration: none; }
                .dicWin { text-decoration: none; cursor: default; }
            </style>
        </head>
        <body>
            $htmlContent
        </body>
        </html>
    """.trimIndent()

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                // Làm trong suốt nền của WebView để tiệp màu với Surface của Compose
                setBackgroundColor(Color.TRANSPARENT)

                settings.apply {
                    javaScriptEnabled = false // Tắt JS để load cực nhanh và tiết kiệm pin
                    defaultTextEncodingName = "utf-8"
                }
            }
        },
        update = { webView ->
            // Load dữ liệu HTML đã được bọc CSS
            webView.loadDataWithBaseURL(null, styledHtml, "text/html", "utf-8", null)
        }
    )
}