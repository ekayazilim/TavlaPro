package com.toplu.tavlauygulamasi.ui.board

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toplu.tavlauygulamasi.R
import com.toplu.tavlauygulamasi.core.models.*
import kotlinx.coroutines.delay

@Composable
fun BoardScreen(viewModel: GameViewModel, onBack: () -> Unit = {}) {
    val gameState by viewModel.gameState.collectAsState()
    val validMoves by viewModel.validMoves.collectAsState()
    val selectedPoint by viewModel.selectedPoint.collectAsState()
    val highlightedPoints by viewModel.highlightedPoints.collectAsState()
    val isRolling by viewModel.isRolling.collectAsState()
    val rollingDice by viewModel.rollingDice.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val currentTheme by viewModel.currentTheme.collectAsState()
    val aiHint by viewModel.aiHint.collectAsState()
    val lastMove by viewModel.lastMove.collectAsState()

    // Animation state for the moving piece
    var animatingMove by remember { mutableStateOf<Move?>(null) }
    var animatingPlayer by remember { mutableStateOf<GameColor?>(null) }
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(lastMove) {
        lastMove?.let { move ->
            animatingMove = move
            animatingPlayer = gameState.currentPlayer
            animProgress.snapTo(0f)
            animProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            )
            animatingMove = null
            animatingPlayer = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF2D1E16), Color(0xFF1B120E))
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = Color.White)
            }

            Button(
                onClick = { viewModel.getHint() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color(0xFFD4AF37)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.5f))
            ) {
                Text("İPUCU", fontSize = 10.sp)
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (gameState.isGameOver) "OYUN BİTTİ" else "TAVLA PRO",
                    color = Color(0xFFD4AF37),
                    style = MaterialTheme.typography.titleMedium,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "SKOR: ${gameState.matchScore[GameColor.WHITE]} - ${gameState.matchScore[GameColor.BLACK]}",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (gameState.currentPlayer == GameColor.WHITE) "SIRA: BEYAZ" else "SIRA: SİYAH",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.width(48.dp))
        }

        // The Board Layout
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            TavlaBoardComponent(
                gameState = gameState,
                selectedPoint = selectedPoint,
                highlightedPoints = highlightedPoints,
                aiHint = aiHint,
                animatingMove = animatingMove,
                animatingPlayer = animatingPlayer,
                animProgress = animProgress.value,
                theme = currentTheme,
                onPointClick = { viewModel.onPointClick(it) }
            )

            if (isRolling) {
                AnimatedDiceOverlay(rollingValues = rollingDice)
            }
        }

        // Dice and Controls
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            color = Color.Black.copy(alpha = 0.3f),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                gameState.dice.forEach { die ->
                    DieView(value = die.value, isUsed = die.isUsed)
                }

                Spacer(modifier = Modifier.width(16.dp))

                if (canUndo) {
                    Button(
                        onClick = { viewModel.onUndo() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("GERİ AL")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Button(
                    onClick = { viewModel.onRollDice() },
                    enabled = !gameState.isGameOver && (gameState.dice.isEmpty() || gameState.dice.all { it.isUsed }),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD4AF37),
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("ZAR AT")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun TavlaBoardComponent(
    gameState: GameState,
    selectedPoint: Int?,
    highlightedPoints: Set<Int>,
    aiHint: Move?,
    animatingMove: Move?,
    animatingPlayer: GameColor?,
    animProgress: Float,
    theme: com.toplu.tavlauygulamasi.ui.theme.GameTheme,
    onPointClick: (Int) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF3E2723))
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()
        val barWidth = width * 0.08f
        val pointWidth = (width - barWidth) / 12

        Image(
            painter = painterResource(id = theme.boardRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Canvas(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val clickedPoint = calculateClickedPoint(offset, width, height, pointWidth, barWidth)
                    if (clickedPoint != -1) {
                        onPointClick(clickedPoint)
                    }
                }
            }
        ) {
            // Draw Checkers
            gameState.board.forEach { (index, pointState) ->
                drawCheckersOnPoint(index, pointState, width, height, pointWidth, barWidth, theme)
            }

            // Draw Bar Checkers
            drawBarCheckers(gameState.bar, width, height, barWidth, theme)

            // Highlighting
            selectedPoint?.let {
                highlightPoint(it, width, height, pointWidth, barWidth, theme.highlightColor.copy(alpha = 0.3f))
            }
            highlightedPoints.forEach {
                highlightPoint(it, width, height, pointWidth, barWidth, Color.Green.copy(alpha = 0.3f))
            }

            aiHint?.let { hint ->
                highlightPoint(hint.from, width, height, pointWidth, barWidth, Color.Cyan.copy(alpha = 0.4f))
                highlightPoint(hint.to, width, height, pointWidth, barWidth, Color.Cyan.copy(alpha = 0.4f))
            }

            // Draw Animated Checker
            animatingMove?.let { move ->
                animatingPlayer?.let { color ->
                    val startPos = calculateCheckerPos(move.from, 0, width, height, pointWidth, barWidth)
                    val endPos = calculateCheckerPos(move.to, 0, width, height, pointWidth, barWidth)
                    
                    val currentX = startPos.x + (endPos.x - startPos.x) * animProgress
                    val currentY = startPos.y + (endPos.y - startPos.y) * animProgress
                    
                    val colors = if (color == GameColor.WHITE) theme.whiteCheckerColors else theme.blackCheckerColors
                    drawChecker(currentX, currentY, pointWidth * 0.4f, colors)
                }
            }
        }
    }
}

fun calculateCheckerPos(index: Int, countIdx: Int, width: Float, height: Float, pointWidth: Float, barWidth: Float): Offset {
    if (index == 0) {
        val barCenterX = width / 2
        val radius = barWidth * 0.4f
        return Offset(barCenterX, height / 2 - radius - countIdx * (radius * 2))
    }
    if (index == 25) {
        return Offset(width - pointWidth / 2, height / 2)
    }
    
    val xOffset = calculatePointX(index, width, pointWidth, barWidth)
    val isTop = index in 13..24
    val centerX = xOffset + pointWidth / 2
    val radius = pointWidth * 0.4f
    val y = if (isTop) radius + countIdx * (radius * 1.8f) else height - radius - countIdx * (radius * 1.8f)
    return Offset(centerX, y)
}

private fun calculateClickedPoint(offset: Offset, width: Float, height: Float, pointWidth: Float, barWidth: Float): Int {
    val x = offset.x
    val y = offset.y
    val barLeft = 6 * pointWidth
    val barRight = barLeft + barWidth

    if (x in barLeft..barRight) return 0

    val isTop = y < height / 2
    return if (isTop) {
        if (x < barLeft) (x / pointWidth).toInt() + 13
        else ((x - barWidth) / pointWidth).toInt() + 13
    } else {
        if (x < barLeft) 12 - (x / pointWidth).toInt()
        else 12 - ((x - barWidth) / pointWidth).toInt()
    }
}

fun DrawScope.drawCheckersOnPoint(index: Int, pointState: PointState, width: Float, height: Float, pointWidth: Float, barWidth: Float, theme: com.toplu.tavlauygulamasi.ui.theme.GameTheme) {
    if (pointState.count == 0) return
    val xOffset = calculatePointX(index, width, pointWidth, barWidth)
    val isTop = index in 13..24
    val centerX = xOffset + pointWidth / 2
    val radius = pointWidth * 0.4f

    for (i in 0 until pointState.count) {
        val y = if (isTop) radius + i * (radius * 1.8f) else height - radius - i * (radius * 1.8f)
        val colors = if (pointState.color == GameColor.WHITE) theme.whiteCheckerColors else theme.blackCheckerColors
        drawChecker(centerX, y, radius, colors)
    }
}

fun DrawScope.drawBarCheckers(bar: Map<GameColor, Int>, width: Float, height: Float, barWidth: Float, theme: com.toplu.tavlauygulamasi.ui.theme.GameTheme) {
    val barLeft = (width - barWidth) / 2
    val centerX = barLeft + barWidth / 2
    val radius = barWidth * 0.4f

    val whiteCount = bar[GameColor.WHITE] ?: 0
    for (i in 0 until whiteCount) {
        drawChecker(centerX, height / 2 - radius - i * (radius * 2), radius, theme.whiteCheckerColors)
    }

    val blackCount = bar[GameColor.BLACK] ?: 0
    for (i in 0 until blackCount) {
        drawChecker(centerX, height / 2 + radius + i * (radius * 2), radius, theme.blackCheckerColors)
    }
}

fun DrawScope.drawChecker(x: Float, y: Float, radius: Float, colors: List<Color>) {
    val gradient = Brush.radialGradient(
        colors = colors,
        center = Offset(x - radius * 0.3f, y - radius * 0.3f),
        radius = radius * 1.5f
    )
    
    drawCircle(gradient, radius, Offset(x, y))
    drawCircle(Color.Gray.copy(alpha = 0.5f), radius, Offset(x, y), style = androidx.compose.ui.graphics.drawscope.Stroke(2f))
    
    drawCircle(
        color = Color.White.copy(alpha = 0.1f),
        radius = radius * 0.8f,
        center = Offset(x, y),
        style = androidx.compose.ui.graphics.drawscope.Stroke(1f)
    )
}

fun DrawScope.highlightPoint(index: Int, width: Float, height: Float, pointWidth: Float, barWidth: Float, color: Color) {
    if (index == 0) {
        drawRect(color, Offset((width - barWidth) / 2, 0f), Size(barWidth, height))
        return
    }
    if (index == 25) return

    val xOffset = calculatePointX(index, width, pointWidth, barWidth)
    val isTop = index in 13..24
    if (isTop) {
        drawRect(color, Offset(xOffset, 0f), Size(pointWidth, height * 0.4f))
    } else {
        drawRect(color, Offset(xOffset, height * 0.6f), Size(pointWidth, height * 0.4f))
    }
}

fun calculatePointX(index: Int, width: Float, pointWidth: Float, barWidth: Float): Float {
    return when (index) {
        in 1..6 -> width - (index * pointWidth)
        in 7..12 -> width - (index * pointWidth) - barWidth
        in 13..18 -> (index - 13) * pointWidth
        in 19..24 -> (index - 13) * pointWidth + barWidth
        else -> 0f
    }
}

@Composable
fun AnimatedDiceOverlay(rollingValues: List<Int>) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.2f)),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        rollingValues.forEach { value ->
            DieView(
                value = value,
                isUsed = false,
                modifier = Modifier
                    .padding(16.dp)
                    .graphicsLayer(
                        rotationZ = rotation + (value * 90f),
                        scaleX = scale,
                        scaleY = scale
                    )
            )
        }
    }
}

@Composable
fun DieView(value: Int, isUsed: Boolean, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.size(50.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUsed) Color.Gray else Color.White
        )
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.padding(8.dp).fillMaxSize()) {
                drawDiceDots(value, size.width)
            }
        }
    }
}

fun DrawScope.drawDiceDots(value: Int, size: Float) {
    val dotColor = Color.Black
    val dotRadius = size * 0.1f
    val spacing = size * 0.25f
    val center = size / 2

    when (value) {
        1 -> drawCircle(dotColor, dotRadius, Offset(center, center))
        2 -> {
            drawCircle(dotColor, dotRadius, Offset(center - spacing, center - spacing))
            drawCircle(dotColor, dotRadius, Offset(center + spacing, center + spacing))
        }
        3 -> {
            drawCircle(dotColor, dotRadius, Offset(center - spacing, center - spacing))
            drawCircle(dotColor, dotRadius, Offset(center, center))
            drawCircle(dotColor, dotRadius, Offset(center + spacing, center + spacing))
        }
        4 -> {
            drawCircle(dotColor, dotRadius, Offset(center - spacing, center - spacing))
            drawCircle(dotColor, dotRadius, Offset(center + spacing, center - spacing))
            drawCircle(dotColor, dotRadius, Offset(center - spacing, center + spacing))
            drawCircle(dotColor, dotRadius, Offset(center + spacing, center + spacing))
        }
        5 -> {
            drawCircle(dotColor, dotRadius, Offset(center - spacing, center - spacing))
            drawCircle(dotColor, dotRadius, Offset(center + spacing, center - spacing))
            drawCircle(dotColor, dotRadius, Offset(center, center))
            drawCircle(dotColor, dotRadius, Offset(center - spacing, center + spacing))
            drawCircle(dotColor, dotRadius, Offset(center + spacing, center + spacing))
        }
        6 -> {
            drawCircle(dotColor, dotRadius, Offset(center - spacing, center - spacing))
            drawCircle(dotColor, dotRadius, Offset(center + spacing, center - spacing))
            drawCircle(dotColor, dotRadius, Offset(center - spacing, center))
            drawCircle(dotColor, dotRadius, Offset(center + spacing, center))
            drawCircle(dotColor, dotRadius, Offset(center - spacing, center + spacing))
            drawCircle(dotColor, dotRadius, Offset(center + spacing, center + spacing))
        }
    }
}
