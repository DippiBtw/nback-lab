package mobappdev.example.nback_cimpl.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
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
    // Get screen dimensions and calculate the smallest dimension to base grid size on
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val smallestDimension = minOf(screenWidth, screenHeight)

    // Load Portrait or Landscape mode
    if (smallestDimension == screenWidth)
        PortraitMode(vm, configuration, onNavigateBack)
    else
        LandscapeMode(vm, configuration, onNavigateBack)
}

@Composable
fun PortraitMode(vm: GameViewModel, configuration: Configuration, onNavigateBack: () -> Unit) {
    val score = vm.score.collectAsState().value
    val highScore = vm.highscore.collectAsState().value
    val eventValue = vm.gameState.collectAsState().value.eventValue
    val gameType = vm.gameState.collectAsState().value.gameType

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top section with back button, score, and high score
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back button on the left
            Button(
                onClick = onNavigateBack,
                modifier = Modifier
                    .width((configuration.screenWidthDp * 0.35).dp)
                    .height(configuration.screenHeightDp.times(0.07).dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "Back",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            // Score and high score on the right
            Column(
                horizontalAlignment = Alignment.End // Aligns text to the right
            ) {
                Text(
                    text = "Best: $highScore",
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Score: $score",
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        GridBox(configuration.screenWidthDp, Modifier.fillMaxWidth(), vm.gridSize, eventValue)

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
fun LandscapeMode(vm: GameViewModel, configuration: Configuration, onNavigateBack: () -> Unit) {
    val score = vm.score.collectAsState().value
    val highScore = vm.highscore.collectAsState().value
    val eventValue = vm.gameState.collectAsState().value.eventValue
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
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            // Back button on the left
            Button(
                onClick = onNavigateBack,
                modifier = Modifier
                    .width((configuration.screenHeightDp * 0.35).dp)
                    .height(configuration.screenWidthDp.times(0.07).dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "Back",
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            // Score and high score on the bottom
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Best: $highScore",
                    style = MaterialTheme.typography.displayMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Score: $score",
                    style = MaterialTheme.typography.displayMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        GridBox(configuration.screenHeightDp, Modifier, vm.gridSize, eventValue)

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
fun GridBox(smallestDimension: Int, modifier: Modifier, gridSize: Int, eventValue: Int) {
    val boxColor = MaterialTheme.colorScheme.primary
    val highlightColor = MaterialTheme.colorScheme.inversePrimary
    val size = (smallestDimension * 0.80).toInt()
    val boxSize = size / gridSize
    var index = 1

    // Define animation parameters
    val fadeOutDuration = 1000 // Duration of the fade-out effect in milliseconds
    val animationDelay = 100L  // Delay between animations to reduce overlapping issues

    // Track animatables and force each to reset when `eventValue` changes
    val cellIntensityMap = remember { mutableStateMapOf<Int, Animatable<Float, *>>() }

    // Trigger a new animation on every `eventValue` change
    LaunchedEffect(eventValue) {
        // Reset or create a new Animatable for the current `eventValue`
        val animatable = cellIntensityMap[eventValue] ?: Animatable(1f).also {
            cellIntensityMap[eventValue] = it
        }

        // Ensure the animatable starts at full intensity
        animatable.snapTo(1f)

        // Apply a delay to prevent overlapping if animations queue up
        kotlinx.coroutines.delay(animationDelay)

        // Start the fade-out animation
        animatable.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = fadeOutDuration, easing = LinearEasing)
        )
    }

    // Display grid of boxes with animated colors based on intensity
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
                    // Get the current intensity for each box; default to 0f if no animation is active
                    val intensity = cellIntensityMap[index]?.value ?: 0f
                    val animatedColor = lerp(boxColor, highlightColor, intensity)

                    Box(
                        modifier = Modifier
                            .size(boxSize.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(animatedColor)
                    )
                    index++
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