package com.example.xogame // change this to match your project's package name

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    XOGame()
                }
            }
        }
    }
}

// ---------- Game logic ----------

private val WIN_LINES = listOf(
    listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // rows
    listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // columns
    listOf(0, 4, 8), listOf(2, 4, 6)                   // diagonals
)

/** Returns the winning line (3 indices) or null if nobody has won yet. */
private fun findWinningLine(board: List<String>): List<Int>? =
    WIN_LINES.firstOrNull { (a, b, c) ->
        board[a].isNotEmpty() && board[a] == board[b] && board[a] == board[c]
    }

// ---------- UI ----------

private val XColor = Color(0xFF1E88E5)
private val OColor = Color(0xFFE53935)

@Composable
fun XOGame() {
    var board by remember { mutableStateOf(List(9) { "" }) }
    var xTurn by remember { mutableStateOf(true) }
    var xScore by remember { mutableStateOf(0) }
    var oScore by remember { mutableStateOf(0) }
    var draws by remember { mutableStateOf(0) }

    val winLine = findWinningLine(board)
    val winner = winLine?.let { board[it[0]] }
    val isDraw = winner == null && board.all { it.isNotEmpty() }
    val gameOver = winner != null || isDraw

    fun resetBoard() {
        board = List(9) { "" }
        xTurn = true
    }

    fun onCellClick(index: Int) {
        if (gameOver || board[index].isNotEmpty()) return
        val mark = if (xTurn) "X" else "O"
        val newBoard = board.toMutableList().also { it[index] = mark }
        board = newBoard

        val line = findWinningLine(newBoard)
        when {
            line != null -> if (mark == "X") xScore++ else oScore++
            newBoard.all { it.isNotEmpty() } -> draws++
            else -> xTurn = !xTurn
        }
    }

    val status = when {
        winner != null -> "$winner wins! 🎉"
        isDraw -> "It's a draw!"
        else -> "${if (xTurn) "X" else "O"}'s turn"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("XO Game", fontSize = 34.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        // Scoreboard
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ScoreItem("X", xScore, XColor)
            ScoreItem("Draws", draws, Color.Gray)
            ScoreItem("O", oScore, OColor)
        }

        Spacer(Modifier.height(24.dp))
        Text(status, fontSize = 24.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(24.dp))

        // 3x3 board
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (row in 0..2) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (col in 0..2) {
                        val index = row * 3 + col
                        Cell(
                            value = board[index],
                            highlighted = winLine?.contains(index) == true,
                            modifier = Modifier.weight(1f),
                            onClick = { onCellClick(index) }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { resetBoard() }) { Text("New round") }
            Button(onClick = {
                resetBoard()
                xScore = 0; oScore = 0; draws = 0
            }) { Text("Reset scores") }
        }
    }
}

@Composable
private fun ScoreItem(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 16.sp, color = color, fontWeight = FontWeight.SemiBold)
        Text(value.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Cell(
    value: String,
    highlighted: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(if (highlighted) Color(0xFFFFF59D) else Color(0xFFEEEEEE))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value,
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            color = if (value == "X") XColor else OColor
        )
    }
}
