package mobappdev.example.nback_cimpl.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import mobappdev.example.nback_cimpl.R
import mobappdev.example.nback_cimpl.ui.viewmodels.FakeVM
import mobappdev.example.nback_cimpl.ui.viewmodels.GameType
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel

/**
 * This is the Home screen composable
 *
 * Currently this screen shows the saved highscore
 * It also contains a button which can be used to show that the C-integration works
 * Furthermore it contains two buttons that you can use to start a game
 *
 * Date: 25-08-2023
 * Version: Version 1.0
 * Author: Yeetivity
 *
 */

@Composable
fun HomeScreen(
    vm: GameViewModel,
    onNavigateToGame: () -> Unit
) {
    val highscore by vm.highscore.collectAsState()  // Highscore is its own StateFlow
    val snackBarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(32.dp),
                text = "High-Score = $highscore",
                style = MaterialTheme.typography.headlineLarge
            )

            // Add sliders section in the middle
            SlidersSection(vm)

            Text(
                modifier = Modifier.padding(16.dp),
                text = "Start Game".uppercase(),
                style = MaterialTheme.typography.displaySmall
            )

            // Game type buttons
            GameTypeButtons(vm = vm, onNavigateToGame = onNavigateToGame)
        }
    }
}

@Composable
fun GameTypeButtons(vm: GameViewModel, onNavigateToGame: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GameTypeButton(
            icon = R.drawable.sound_on,
            gameType = GameType.Audio,
            vm = vm,
            onNavigateToGame = onNavigateToGame
        )
        GameTypeButton(
            icon = R.drawable.brain,
            gameType = GameType.AudioVisual,
            vm = vm,
            onNavigateToGame = onNavigateToGame
        )
        GameTypeButton(
            icon = R.drawable.visual,
            gameType = GameType.Visual,
            vm = vm,
            onNavigateToGame = onNavigateToGame
        )
    }
}

@Composable
fun GameTypeButton(
    icon: Int,
    gameType: GameType,
    vm: GameViewModel,
    onNavigateToGame: () -> Unit
) {
    Button(
        onClick = {
            vm.setGameType(gameType)
            vm.startGame()
            onNavigateToGame()
        }, shape = RoundedCornerShape(6.dp),
        modifier = Modifier.padding(4.dp)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = "Game Type",
            modifier = Modifier
                .height(48.dp)
                .aspectRatio(3f / 2f),
        )
    }
}


@Composable
fun SlidersSection(vm: GameViewModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = 32.dp)
    ) {
        SliderWithLabel(
            label = "N-Back Level",
            value = vm.nBack.collectAsState().value.toFloat(),
            range = 1f..6f,
            steps = 4,
            updateFunc = { value ->
                vm.saveNBackLevel(value.toInt())  // Update using VM function
            }
        )
        SliderWithLabel(
            label = "Grid Size",
            value = vm.gridSize.collectAsState().value.toFloat(),
            range = 3f..6f,
            steps = 2,
            updateFunc = { value ->
                vm.saveGridSize(value.toInt())  // Update using VM function
            }
        )
        SliderWithLabel(
            label = "Number of Events",
            value = vm.numberOfEvents.collectAsState().value.toFloat(),
            range = 10f..40f,
            steps = 28,
            updateFunc = { value ->
                vm.saveNumEvents(value.toInt())  // Update using VM function
            }
        )
        SliderWithLabel(
            label = "Interval of Events",
            value = vm.eventInterval.collectAsState().value.toFloat(),
            range = 1000f..5000f,
            steps = 3,
            updateFunc = { value ->
                vm.saveEventInterval(value.toLong())  // Update using VM function
            }
        )
    }
}


@Composable
fun SliderWithLabel(label: String, value: Float, range: ClosedFloatingPointRange<Float>, steps: Int = 1, updateFunc: (Float) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "$label: ${value.toInt()}", style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = value,
            onValueChange = { newValue -> updateFunc(newValue) },
            valueRange = range,
            steps = steps,
            modifier = Modifier.fillMaxWidth()
        )
    }
}


@Preview
@Composable
fun HomeScreenPreview() {
    val navController = rememberNavController()

    // Since I am injecting a VM into my homescreen that depends on Application context, the preview doesn't work.
    Surface() {
        HomeScreen(FakeVM(), onNavigateToGame = { navController.navigate("game") })
    }
}