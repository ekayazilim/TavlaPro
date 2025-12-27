package com.toplu.tavlauygulamasi.ui.theme

import androidx.compose.ui.graphics.Color
import com.toplu.tavlauygulamasi.R

data class GameTheme(
    val name: String,
    val boardRes: Int,
    val whiteCheckerColors: List<Color>,
    val blackCheckerColors: List<Color>,
    val highlightColor: Color
)

object GameThemes {
    val Classic = GameTheme(
        name = "Klasik Ahşap",
        boardRes = R.drawable.board_bg,
        whiteCheckerColors = listOf(Color.White, Color(0xFFE0E0E0)),
        blackCheckerColors = listOf(Color(0xFF424242), Color.Black),
        highlightColor = Color(0xFFD4AF37)
    )

    val Modern = GameTheme(
        name = "Modern Gece",
        boardRes = R.drawable.main_bg, // Reuse or add new
        whiteCheckerColors = listOf(Color(0xFF00BFA5), Color(0xFF00796B)),
        blackCheckerColors = listOf(Color(0xFF37474F), Color(0xFF263238)),
        highlightColor = Color(0xFF00E676)
    )

    val Luxury = GameTheme(
        name = "Lüks Mermer",
        boardRes = R.drawable.board_bg,
        whiteCheckerColors = listOf(Color(0xFFF5F5F5), Color(0xFFBDBDBD)),
        blackCheckerColors = listOf(Color(0xFF212121), Color(0xFF000000)),
        highlightColor = Color(0xFFFFD700)
    )

    val all = listOf(Classic, Modern, Luxury)
}
