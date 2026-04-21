package com.line98.game.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.line98.game.core.BallColor
import com.line98.game.core.Cell
import com.line98.game.core.GameEngine
import com.line98.game.core.GameMode
import com.line98.game.core.GameState
import com.line98.game.core.Position
import com.line98.game.ui.displayName
import com.line98.game.core.PowerUpType

private val BoardSurfaceColor = Color(0xFF111317)
private val GridCellColor = Color(0xFF1A1D22)
private val GridSelectedColor = Color(0xFF2F3742)
private val GridReachableColor = Color(0xFF203D33)
private val GridBallHighlight = Color.White.copy(alpha = 0.25f)
private val MaxBoardContentWidth = 432.dp

@Composable
fun GameScreen(
    state: GameState,
    onCellTap: (Position) -> Unit,
    onPowerUp: (PowerUpType) -> Unit,
    onMenu: () -> Unit,
    onRestart: () -> Unit,
) {
    val boardEngine = remember { GameEngine() }
    val scrollState = rememberScrollState()
    val blockedPath = state.message == "Blocked path"
    val reachableTargets = remember(state.board, state.selected) {
        val selected = state.selected ?: return@remember emptySet()
        state.board.positions()
            .filter { position ->
                position != selected &&
                    state.board[position].isEmpty &&
                    boardEngine.hasPath(state.board, selected, position)
            }
            .toSet()
    }

    Scaffold { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            val contentWidth = (maxWidth - 24.dp).coerceAtMost(MaxBoardContentWidth)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TopHud(
                    state = state,
                    modifier = Modifier.width(contentWidth),
                )

                if (state.activePowerUp != null) {
                    Text(
                        text = "${state.activePowerUp.displayName()} active",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.width(contentWidth),
                        textAlign = TextAlign.Center,
                    )
                }

                state.message?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.width(contentWidth),
                        textAlign = TextAlign.Center,
                    )
                }

                BoardSurface(
                    state = state,
                    reachableTargets = reachableTargets,
                    blockedPath = blockedPath,
                    modifier = Modifier.size(contentWidth),
                    onCellTap = onCellTap,
                )

                NextPreview(
                    colors = state.nextBalls,
                    modifier = Modifier.width(contentWidth),
                )

                if (state.mode == GameMode.PowerUp) {
                    PowerUpBar(
                        state = state,
                        onPowerUp = onPowerUp,
                        modifier = Modifier.width(contentWidth),
                    )
                }

                if (state.isGameOver) {
                    Surface(
                        modifier = Modifier.width(contentWidth),
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
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.width(contentWidth),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onMenu,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Menu")
                    }
                    Button(
                        onClick = onRestart,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Restart")
                    }
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
    blockedPath: Boolean,
    modifier: Modifier = Modifier,
    onCellTap: (Position) -> Unit,
) {
    val selectionColor =
        if (blockedPath) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
    val boardModifier =
        if (blockedPath) {
            modifier.border(2.dp, MaterialTheme.colorScheme.error, MaterialTheme.shapes.medium)
        } else {
            modifier
        }

    Surface(
        modifier = boardModifier,
        color = BoardSurfaceColor,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            for (row in 0 until Position.Size) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    for (col in 0 until Position.Size) {
                        val position = Position(row, col)
                        val cell = state.board[position]
                        val isSelected = state.selected == position
                        val isReachable = position in reachableTargets
                        val description = cellDescription(
                            position = position,
                            cell = cell,
                            selected = isSelected,
                            reachable = isReachable,
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .semantics {
                                    role = Role.Button
                                    contentDescription = description
                                    onClick(label = "Activate cell") {
                                        onCellTap(position)
                                        true
                                    }
                                }
                                .clickable(
                                    role = Role.Button,
                                    onClick = { onCellTap(position) },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val inset = size.minDimension * 0.08f
                                val ballRadius = size.minDimension * 0.31f
                                drawRoundRect(
                                    color = when {
                                        isSelected -> GridSelectedColor
                                        isReachable -> GridReachableColor
                                        else -> GridCellColor
                                    },
                                    topLeft = Offset(inset, inset),
                                    size = Size(size.width - inset * 2f, size.height - inset * 2f),
                                    cornerRadius = CornerRadius(size.minDimension * 0.08f, size.minDimension * 0.08f),
                                )

                                if (isReachable) {
                                    drawRoundRect(
                                        color = Color(0xFF35C2A1).copy(alpha = 0.18f),
                                        topLeft = Offset(inset, inset),
                                        size = Size(size.width - inset * 2f, size.height - inset * 2f),
                                        cornerRadius = CornerRadius(size.minDimension * 0.08f, size.minDimension * 0.08f),
                                    )
                                }

                                if (cell is Cell.Occupied) {
                                    val center = Offset(size.width / 2f, size.height / 2f)
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
                                        color = GridBallHighlight,
                                        radius = ballRadius * 0.32f,
                                        center = Offset(center.x - ballRadius * 0.2f, center.y - ballRadius * 0.22f),
                                    )
                                    if (isSelected) {
                                        drawCircle(
                                            color = selectionColor,
                                            radius = ballRadius * 1.28f,
                                            center = center,
                                            style = Stroke(width = size.minDimension * 0.05f),
                                        )
                                    }
                                } else if (isSelected) {
                                    drawCircle(
                                        color = selectionColor,
                                        radius = ballRadius * 1.1f,
                                        center = Offset(size.width / 2f, size.height / 2f),
                                        style = Stroke(width = size.minDimension * 0.04f),
                                    )
                                }
                            }
                        }
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
                modifier = Modifier.weight(1f),
                onClick = { onPowerUp(PowerUpType.Bomb) },
            )
            PowerUpButton(
                label = "Color",
                count = state.charges.colorChanger,
                active = state.activePowerUp == PowerUpType.ColorChanger,
                enabled = state.charges.colorChanger > 0 && !state.isGameOver,
                modifier = Modifier.weight(1f),
                onClick = { onPowerUp(PowerUpType.ColorChanger) },
            )
            PowerUpButton(
                label = "Row",
                count = state.charges.rowColumnClear,
                active = state.activePowerUp == PowerUpType.RowClear,
                enabled = state.charges.rowColumnClear > 0 && !state.isGameOver,
                modifier = Modifier.weight(1f),
                onClick = { onPowerUp(PowerUpType.RowClear) },
            )
            PowerUpButton(
                label = "Col",
                count = state.charges.rowColumnClear,
                active = state.activePowerUp == PowerUpType.ColumnClear,
                enabled = state.charges.rowColumnClear > 0 && !state.isGameOver,
                modifier = Modifier.weight(1f),
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

private fun cellDescription(
    position: Position,
    cell: Cell,
    selected: Boolean,
    reachable: Boolean,
): String {
    val base = "Row ${position.row + 1}, column ${position.col + 1}"
    val occupancy = when (cell) {
        Cell.Empty -> "empty"
        is Cell.Occupied -> "${cell.color.name.lowercase()} ball"
    }
    val selection = if (selected) ", selected" else ""
    val target = when {
        reachable -> ", valid target"
        cell.isEmpty -> ", unavailable"
        else -> ""
    }
    return "$base, $occupancy$selection$target"
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

