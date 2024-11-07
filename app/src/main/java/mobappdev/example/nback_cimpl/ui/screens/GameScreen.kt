package mobappdev.example.nback_cimpl.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import mobappdev.example.nback_cimpl.ui.viewmodels.FakeVM
import mobappdev.example.nback_cimpl.ui.viewmodels.GameType
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel
import mobappdev.example.nback_cimpl.ui.viewmodels.GuessFeedback

@Composable
fun GameScreen(vm: GameViewModel, onNavigateBack: () -> Unit) {
    val configuration = LocalConfiguration.current
    val smallestDimension = minOf(configuration.screenWidthDp.dp, configuration.screenHeightDp.dp)
    val isPortrait = smallestDimension == configuration.screenWidthDp.dp

    val isGameOver =
        vm.visualState.collectAsState().value.finished || vm.audioState.collectAsState().value.finished

    if (isPortrait) {
        PortraitMode(vm, configuration, onNavigateBack, isGameOver)
    } else {
        LandscapeMode(vm, configuration, onNavigateBack, isGameOver)
    }
}

@Composable
fun ScoreAndControls(
    score: Int, highScore: Int, vm: GameViewModel, onNavigateBack: () -> Unit,
) {
    val round = vm.currentEventIndex.collectAsState().value + 1

    // Back Button
    BackButton(onNavigateBack = onNavigateBack, vm = vm)

    // Score Display
    Column(horizontalAlignment = Alignment.End) {
        Text(
            "Best: $highScore",
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            "Score: $score",
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            "Round: $round",
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun BackButton(onNavigateBack: () -> Unit, vm: GameViewModel) {
    Button(
        onClick = {
            vm.endGame()
            onNavigateBack()
        },
        modifier = Modifier
            .width(150.dp)
            .height(48.dp),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text("Back", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun GameControlButtons(gameType: GameType, vm: GameViewModel, modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    val smallestDimension = minOf(configuration.screenWidthDp.dp, configuration.screenHeightDp.dp)
    val isPortrait = smallestDimension == configuration.screenWidthDp.dp

    when (gameType) {
        GameType.Audio -> AudioButton(vm, modifier)
        GameType.Visual -> PositionButton(vm, modifier)
        GameType.AudioVisual -> {
            if (isPortrait)
                Row(modifier = modifier) {
                    PositionButton(vm, Modifier.weight(1f).fillMaxHeight())
                    Spacer(modifier = Modifier.width(8.dp))
                    AudioButton(vm, Modifier.weight(1f).fillMaxHeight())
                }
            else {
                Column(modifier = modifier) {
                    PositionButton(vm, Modifier.weight(1f))
                    Spacer(modifier = Modifier.height(8.dp))
                    AudioButton(vm, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun PortraitMode(
    vm: GameViewModel,
    configuration: Configuration,
    onNavigateBack: () -> Unit,
    isGameOver: Boolean
) {
    val score = vm.score.collectAsState().value
    val highScore = vm.highscore.collectAsState().value
    val gameType = vm.gameType.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScoreAndControls(score, highScore, vm, onNavigateBack)
        }

        if (gameType != GameType.Audio)
            GridBox(
                smallestDimension = configuration.screenWidthDp,
                vm = vm,
                modifier = Modifier.fillMaxWidth()
            )

        GameControlButtons(
            gameType,
            vm,
            Modifier
                .fillMaxWidth()
                .height(configuration.screenHeightDp.times(0.18).dp)
        )
    }
    // Show Game Over Box if finished
    if (isGameOver) {
        GameOverBox(vm, onNavigateBack)
    }
}

@Composable
fun LandscapeMode(
    vm: GameViewModel,
    configuration: Configuration,
    onNavigateBack: () -> Unit,
    isGameOver: Boolean
) {
    val score = vm.score.collectAsState().value
    val highScore = vm.highscore.collectAsState().value
    val gameType = vm.gameType.collectAsState().value

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            ScoreAndControls(score, highScore, vm, onNavigateBack)
        }

        if (gameType != GameType.Audio)
            GridBox(smallestDimension = configuration.screenHeightDp, vm = vm)

        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GameControlButtons(
                gameType,
                vm,
                Modifier
                    .fillMaxHeight()
                    .width(configuration.screenWidthDp.times(0.18).dp)
            )
        }
    }
    // Show Game Over Box if finished
    if (isGameOver) {
        GameOverBox(vm, onNavigateBack)
    }
}

@Composable
fun GameOverBox(vm: GameViewModel, onNavigateBack: () -> Unit) {
    val score = vm.score.collectAsState().value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Game Over", style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                "Score: $score", style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    vm.endGame()
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Home")
            }
        }
    }
}

@Composable
fun GridBox(smallestDimension: Int, modifier: Modifier = Modifier, vm: GameViewModel) {
    val boxColor = MaterialTheme.colorScheme.primary
    val highlightColor = Color.Red
    val size = (smallestDimension * 0.80).toInt()
    val gridSize = vm.gridSize.collectAsState().value
    val boxSize = size / gridSize
    val eventInterval = vm.eventInterval.collectAsState().value

    val gameState = vm.visualState.collectAsState().value
    val highlightMap = remember { mutableStateMapOf<Int, MutableState<Boolean>>() }

    // Initialize highlight state for each cell
    LaunchedEffect(Unit) {
        for (i in 1..gridSize * gridSize) {
            highlightMap.getOrPut(i) { mutableStateOf(false) }
        }
    }

    // Trigger the LaunchedEffect
    LaunchedEffect(gameState.index) {
        highlightMap.values.forEach { it.value = false }
        kotlinx.coroutines.delay(300L)
        highlightMap[gameState.eventValue]?.value = true
        kotlinx.coroutines.delay(eventInterval)
        highlightMap[gameState.eventValue]?.value = false
    }

    // Layout for the grid
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        var index = 1
        repeat(gridSize) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(gridSize) {
                    val cellColor =
                        if (highlightMap[index]?.value == true) highlightColor else boxColor
                    Box(
                        modifier = Modifier
                            .size(boxSize.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(cellColor)
                    )
                    index++
                }
            }
        }
    }
}


@Composable
fun AudioButton(vm: GameViewModel, modifier: Modifier) {
    val color = when (vm.audioFeedback.collectAsState().value) {
        GuessFeedback.None -> MaterialTheme.colorScheme.primary
        GuessFeedback.Correct -> Color.hsv(120f, 1f, 0.6f)
        GuessFeedback.Incorrect -> Color.hsv(0f, 1f, 0.6f)
    }
    Button(
        onClick = { vm.checkMatch(GameType.Audio) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text("Sound", style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun PositionButton(vm: GameViewModel, modifier: Modifier) {
    val color = when (vm.visualFeedback.collectAsState().value) {
        GuessFeedback.None -> MaterialTheme.colorScheme.primary
        GuessFeedback.Correct -> Color.hsv(120f, 1f, 0.6f)
        GuessFeedback.Incorrect -> Color.hsv(0f, 1f, 0.6f)
    }
    Button(
        onClick = { vm.checkMatch(GameType.Visual) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text("Position", style = MaterialTheme.typography.titleLarge)
    }
}

@Preview
@Composable
fun GameScreenPreview() {
    val navController = rememberNavController()
    Surface {
        GameScreen(vm = FakeVM(), onNavigateBack = { navController.navigate("home") })
    }
}
