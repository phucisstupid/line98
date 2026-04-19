package com.line98.game.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.line98.game.core.BallColor
import com.line98.game.core.Cell
import com.line98.game.core.GameEngine
import com.line98.game.core.GameMode
import com.line98.game.core.GameState
import com.line98.game.core.Position
import com.line98.game.core.PowerUpType

@Composable
fun GameScreen(
    state: GameState,
    onCellTap: (Position) -> Unit,
    onPowerUp: (PowerUpType) -> Unit,
    onMenu: () -> Unit,
    onRestart: () -> Unit,
) {
    val boardEngine = remember { GameEngine() }
    val reachableTargets = remember(state.board, state.selected) {
        val selected = state.selected
        if (selected == null) {
            emptySet()
        } else {
            state.board.positions()
                .filter { position ->
                    position != selected &&
                        state.board[position].isEmpty &&
                        boardEngine.hasPath(state.board, selected, position)
                }
                .toSet()
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TopHud(
                state = state,
                modifier = Modifier.fillMaxWidth(),
            )

            if (state.activePowerUp != null) {
                Text(
                    text = "${state.activePowerUp.displayName()} active",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }

            state.message?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }

            BoardSurface(
                state = state,
                reachableTargets = reachableTargets,
                modifier = Modifier.fillMaxWidth(),
                onCellTap = onCellTap,
            )

            NextPreview(
                colors = state.nextBalls,
                modifier = Modifier.fillMaxWidth(),
            )

            if (state.mode == GameMode.PowerUp) {
                PowerUpBar(
                    state = state,
                    onPowerUp = onPowerUp,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (state.isGameOver) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 2.dp,
                    shadowElevation = 0.dp,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Game over",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = "Score ${state.score}  Best ${state.highScore}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            ) {
                OutlinedButton(
                    onClick = onMenu,
                    modifier = Modifier.width(120.dp),
                ) {
                    Text("Menu")
                }
                Button(
                    onClick = onRestart,
                    modifier = Modifier.width(120.dp),
                ) {
                    Text("Restart")
                }
            }
        }
    }
}

@Composable
private fun TopHud(
    state: GameState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = state.mode.displayName(),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = "Score ${state.score}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "Best ${state.highScore}",
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun BoardSurface(
    state: GameState,
    reachableTargets: Set<Position>,
    modifier: Modifier = Modifier,
    onCellTap: (Position) -> Unit,
) {
    val selectionColor = MaterialTheme.colorScheme.secondary

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .semantics { contentDescription = "Line 98 board" },
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(state.board, state.selected, state.activePowerUp) {
                    detectTapGestures { tap ->
                        val cellSize = size.width / Position.Size.toFloat()
                        val col = (tap.x / cellSize).toInt().coerceIn(0, Position.Size - 1)
                        val row = (tap.y / cellSize).toInt().coerceIn(0, Position.Size - 1)
                        onCellTap(Position(row, col))
                    }
                },
        ) {
            val cellSize = size.minDimension / Position.Size
            val inset = cellSize * 0.08f
            val ballRadius = cellSize * 0.31f
            drawRoundRect(
                color = Color(0xFF111317),
                topLeft = Offset.Zero,
                size = Size(size.width, size.height),
                cornerRadius = CornerRadius(cellSize * 0.18f, cellSize * 0.18f),
            )

            for (row in 0 until Position.Size) {
                for (col in 0 until Position.Size) {
                    val position = Position(row, col)
                    val left = col * cellSize
                    val top = row * cellSize
                    val cell = state.board[position]
                    val isSelected = state.selected == position
                    val isReachable = position in reachableTargets

                    drawRoundRect(
                        color = when {
                            isSelected -> Color(0xFF2F3742)
                            isReachable -> Color(0xFF203D33)
                            else -> Color(0xFF1A1D22)
                        },
                        topLeft = Offset(left + inset, top + inset),
                        size = Size(cellSize - inset * 2f, cellSize - inset * 2f),
                        cornerRadius = CornerRadius(cellSize * 0.08f, cellSize * 0.08f),
                    )

                    if (isReachable) {
                        drawRoundRect(
                            color = Color(0xFF35C2A1).copy(alpha = 0.18f),
                            topLeft = Offset(left + inset, top + inset),
                            size = Size(cellSize - inset * 2f, cellSize - inset * 2f),
                            cornerRadius = CornerRadius(cellSize * 0.08f, cellSize * 0.08f),
                        )
                    }

                    if (cell is Cell.Occupied) {
                        val center = Offset(left + cellSize / 2f, top + cellSize / 2f)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    cell.color.toComposeColor().copy(alpha = 0.98f),
                                    cell.color.toComposeColor().darker(0.18f),
                                ),
                            ),
                            radius = ballRadius,
                            center = center,
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.25f),
                            radius = ballRadius * 0.32f,
                            center = Offset(center.x - ballRadius * 0.2f, center.y - ballRadius * 0.22f),
                        )
                        if (isSelected) {
                            drawCircle(
                                color = selectionColor,
                                radius = ballRadius * 1.28f,
                                center = center,
                                style = Stroke(width = cellSize * 0.05f),
                            )
                        }
                    } else if (isSelected) {
                        drawCircle(
                            color = selectionColor,
                            radius = ballRadius * 1.1f,
                            center = Offset(left + cellSize / 2f, top + cellSize / 2f),
                            style = Stroke(width = cellSize * 0.04f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NextPreview(
    colors: List<BallColor>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Next",
            style = MaterialTheme.typography.titleSmall,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            colors.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = color.toComposeColor(),
                            shape = CircleShape,
                        ),
                )
            }
        }
    }
}

@Composable
private fun PowerUpBar(
    state: GameState,
    onPowerUp: (PowerUpType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Power-ups",
            style = MaterialTheme.typography.titleSmall,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PowerUpButton(
                label = "Bomb",
                count = state.charges.bomb,
                active = state.activePowerUp == PowerUpType.Bomb,
                enabled = state.charges.bomb > 0 && !state.isGameOver,
                modifier = Modifier.width(80.dp),
                onClick = { onPowerUp(PowerUpType.Bomb) },
            )
            PowerUpButton(
                label = "Color",
                count = state.charges.colorChanger,
                active = state.activePowerUp == PowerUpType.ColorChanger,
                enabled = state.charges.colorChanger > 0 && !state.isGameOver,
                modifier = Modifier.width(80.dp),
                onClick = { onPowerUp(PowerUpType.ColorChanger) },
            )
            PowerUpButton(
                label = "Row",
                count = state.charges.rowColumnClear,
                active = state.activePowerUp == PowerUpType.RowClear,
                enabled = state.charges.rowColumnClear > 0 && !state.isGameOver,
                modifier = Modifier.width(80.dp),
                onClick = { onPowerUp(PowerUpType.RowClear) },
            )
            PowerUpButton(
                label = "Col",
                count = state.charges.rowColumnClear,
                active = state.activePowerUp == PowerUpType.ColumnClear,
                enabled = state.charges.rowColumnClear > 0 && !state.isGameOver,
                modifier = Modifier.width(80.dp),
                onClick = { onPowerUp(PowerUpType.ColumnClear) },
            )
        }
    }
}

@Composable
private fun PowerUpButton(
    label: String,
    count: Int,
    active: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val colors = if (active) {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f),
            disabledContentColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.6f),
        )
    } else {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        )
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 52.dp),
        colors = colors,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun BallColor.toComposeColor(): Color =
    when (this) {
        BallColor.Red -> Color(0xFFE84A5F)
        BallColor.Green -> Color(0xFF35C2A1)
        BallColor.Blue -> Color(0xFF5D8CFF)
        BallColor.Yellow -> Color(0xFFFFCE47)
        BallColor.Purple -> Color(0xFFB86CFF)
        BallColor.Cyan -> Color(0xFF45D8FF)
    }

private fun Color.darker(amount: Float): Color =
    Color(red * (1f - amount), green * (1f - amount), blue * (1f - amount), alpha)

private fun GameMode.displayName(): String =
    when (this) {
        GameMode.Classic -> "Classic"
        GameMode.PowerUp -> "Power-up"
    }

private fun PowerUpType.displayName(): String =
    when (this) {
        PowerUpType.Bomb -> "Bomb"
        PowerUpType.ColorChanger -> "Color changer"
        PowerUpType.RowClear -> "Row clear"
        PowerUpType.ColumnClear -> "Column clear"
    }
