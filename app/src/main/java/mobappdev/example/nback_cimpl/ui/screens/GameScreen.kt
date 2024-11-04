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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import mobappdev.example.nback_cimpl.ui.viewmodels.FakeVM
import mobappdev.example.nback_cimpl.ui.viewmodels.GameType
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment

@Composable
fun GameScreen(vm: GameViewModel, onNavigateBack: () -> Unit) {
    vm.setGameType(GameType.AudioVisual)

    // Get screen dimensions and calculate the smallest dimension to base grid size on
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val smallestDimension = minOf(screenWidth, screenHeight)


    // Game controls based on game type
    if (smallestDimension == screenWidth)
        PortraitMode(vm, configuration)
    else
        LandscapeMode(vm, configuration)
}

@Composable
fun PortraitMode(vm: GameViewModel, configuration: Configuration) {
    val score = vm.score.collectAsState().value
    val highScore = vm.highscore.collectAsState().value
    val gameType = vm.gameState.collectAsState().value.gameType

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top section with score and high score
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(6.dp),
                text = "High Score: $highScore",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Score: $score",
                style = MaterialTheme.typography.titleLarge
            )
        }

        GridBox(configuration.screenWidthDp, Modifier.fillMaxWidth())

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (gameType) {
                GameType.Audio -> {
                    AudioButton(
                        vm,
                        Modifier
                            .fillMaxWidth()
                            .height(configuration.screenHeightDp.times(0.18).dp)
                    )
                }

                GameType.Visual -> {
                    PositionButton(
                        vm,
                        Modifier
                            .fillMaxWidth()
                            .height(configuration.screenHeightDp.times(0.18).dp)
                    )
                }

                GameType.AudioVisual -> {
                    PositionButton(
                        vm,
                        Modifier
                            .weight(1f)
                            .height(configuration.screenHeightDp.times(0.18).dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AudioButton(
                        vm,
                        Modifier
                            .weight(1f)
                            .height(configuration.screenHeightDp.times(0.18).dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LandscapeMode(vm: GameViewModel, configuration: Configuration) {
    val score = vm.score.collectAsState().value
    val highScore = vm.highscore.collectAsState().value
    val gameType = vm.gameState.collectAsState().value.gameType

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Score and high score section
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = Modifier.padding(6.dp),
                text = "High Score: $highScore",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Score: $score",
                style = MaterialTheme.typography.titleLarge
            )
        }

        GridBox(configuration.screenHeightDp, Modifier)

        // Game control buttons
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(0.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (gameType) {
                GameType.Audio -> {
                    AudioButton(
                        vm,
                        Modifier
                            .fillMaxHeight()
                            .width(configuration.screenWidthDp.times(0.18).dp)
                    )
                }

                GameType.Visual -> {
                    PositionButton(
                        vm,
                        Modifier
                            .fillMaxHeight()
                            .width(configuration.screenWidthDp.times(0.18).dp)
                    )
                }

                GameType.AudioVisual -> {
                    PositionButton(
                        vm,
                        Modifier
                            .weight(1f)
                            .width(configuration.screenWidthDp.times(0.18).dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AudioButton(
                        vm,
                        Modifier
                            .weight(1f)
                            .width(configuration.screenWidthDp.times(0.18).dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GridBox(smallestDimension: Int, modifier: Modifier) {
    val gridSize = 3
    val boxColor = MaterialTheme.colorScheme.primary
    val size = (smallestDimension * 0.80).toInt()
    val boxSize = size / gridSize

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(gridSize) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(gridSize) {
                    Box(
                        modifier = Modifier
                            .size(boxSize.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(boxColor)
                    )
                }
            }
        }
    }
}

@Composable
fun AudioButton(vm: GameViewModel, modifier: Modifier) {
    Button(
        onClick = { vm.checkMatch() },
        modifier = modifier,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            "Sound",
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
fun PositionButton(vm: GameViewModel, modifier: Modifier) {
    Button(
        onClick = { vm.checkMatch() },
        modifier = modifier,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            "Position",
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Preview
@Composable
fun GameScreenPreview() {
    val navController = rememberNavController()

    // Since I am injecting a VM into my homescreen that depends on Application context, the preview doesn't work.
    Surface() {
        GameScreen(FakeVM(), onNavigateBack = { navController.navigate("home") })
    }
}