package mobappdev.example.nback_cimpl.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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

@Composable
fun GameScreen(vm: GameViewModel, onNavigateBack: () -> Unit) {
    val configuration = LocalConfiguration.current
    val smallestDimension = minOf(configuration.screenWidthDp.dp, configuration.screenHeightDp.dp)
    val isPortrait = smallestDimension == configuration.screenWidthDp.dp

    if (isPortrait) {
        PortraitMode(vm, configuration, onNavigateBack)
    } else {
        LandscapeMode(vm, configuration, onNavigateBack)
    }
}

@Composable
fun ScoreAndControls(
    score: Int, highScore: Int, vm: GameViewModel, onNavigateBack: () -> Unit,
) {
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
    when (gameType) {
        GameType.Audio -> AudioButton(vm, modifier)
        GameType.Visual -> PositionButton(vm, modifier)
        GameType.AudioVisual -> {
            Row(modifier = modifier) {
                PositionButton(vm, Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                AudioButton(vm, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun PortraitMode(vm: GameViewModel, configuration: Configuration, onNavigateBack: () -> Unit) {
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
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
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
}

@Composable
fun LandscapeMode(vm: GameViewModel, configuration: Configuration, onNavigateBack: () -> Unit) {
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
                    ) {
                        Text(text = "$index", color = MaterialTheme.colorScheme.inversePrimary)
                    }
                    index++
                }
            }
        }
    }
}


@Composable
fun AudioButton(vm: GameViewModel, modifier: Modifier) {
    Button(onClick = { vm.checkMatch(GameType.Audio) }, modifier = modifier, shape = RoundedCornerShape(6.dp)) {
        Text("Sound", style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun PositionButton(vm: GameViewModel, modifier: Modifier) {
    Button(onClick = { vm.checkMatch(GameType.Visual) }, modifier = modifier, shape = RoundedCornerShape(6.dp)) {
        Text("Position", style = MaterialTheme.typography.titleLarge)
    }
}

@Preview
@Composable
fun GameScreenPreview() {
    val navController = rememberNavController()
    Surface() {
        GameScreen(vm = FakeVM(), onNavigateBack = { navController.navigate("home") })
    }
}
