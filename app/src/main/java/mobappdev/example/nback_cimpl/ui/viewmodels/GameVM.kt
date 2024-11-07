package mobappdev.example.nback_cimpl.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.GameApplication
import mobappdev.example.nback_cimpl.NBackHelper
import mobappdev.example.nback_cimpl.data.UserPreferencesRepository

/**
 * This is the GameViewModel.
 *
 * It is good practice to first make an interface, which acts as the blueprint
 * for your implementation. With this interface we can create fake versions
 * of the viewmodel, which we can use to test other parts of our app that depend on the VM.
 *
 * Our viewmodel itself has functions to start a game, to specify a gametype,
 * and to check if we are having a match
 *
 * Date: 25-08-2023
 * Version: Version 1.0
 * Author: Yeetivity
 *
 */


interface GameViewModel {
    val gameState: StateFlow<GameState>
    val score: StateFlow<Int>
    val highscore: StateFlow<Int>
    val nBack: Int
    val gridSize: Int
    val eventInterval: Long

    fun setGameType(gameType: GameType)
    fun startGame()
    fun endGame()

    fun checkMatch()
}

class GameVM(
    private val userPreferencesRepository: UserPreferencesRepository
): GameViewModel, ViewModel() {
    private val _gameState = MutableStateFlow(GameState())
    override val gameState: StateFlow<GameState>
        get() = _gameState.asStateFlow()

    private val _score = MutableStateFlow(0)
    override val score: StateFlow<Int>
        get() = _score

    private val _highscore = MutableStateFlow(0)
    override val highscore: StateFlow<Int>
        get() = _highscore

    // nBack is currently hardcoded
    override val nBack: Int = 2
    override val gridSize: Int = 5

    private var job: Job? = null  // coroutine job for the game event
    override val eventInterval: Long = 2000L  // 2000 ms (2s)

    private val nBackHelper = NBackHelper()  // Helper that generate the event array
    private var events = emptyArray<Int>()  // Array with all events

    private var currentEventIndex = -1
    private var guessed = false

    // Scoring settings
    private val correctMatchPoints = 1
    private val incorrectMatchPenalty = 1
    private val minScore = 0  // Minimum score allowed

    override fun setGameType(gameType: GameType) {
        // update the gametype in the gamestate
        _gameState.value = _gameState.value.copy(gameType = gameType)
    }

    override fun startGame() {
        job?.cancel()  // Cancel any existing game loop
        currentEventIndex = 0

        // Get the events from our C-model (returns IntArray, so we need to convert to Array<Int>)
        events = nBackHelper.generateNBackString(10, gridSize*gridSize, 30, nBack).toList().toTypedArray()  // Todo Higher Grade: currently the size etc. are hardcoded, make these based on user input
        Log.d("GameVM", "The following sequence was generated: ${events.contentToString()}")

        job = viewModelScope.launch {
            when (gameState.value.gameType) {
                GameType.Audio -> runAudioGame()
                GameType.AudioVisual -> runAudioVisualGame()
                GameType.Visual -> runVisualGame(events)
            }

            // Update high score if the current score is higher
            if (_score.value > _highscore.value) {
                _highscore.value = _score.value
            }
        }
    }

    override fun endGame() {
        job?.cancel()
        currentEventIndex = 0
        events = emptyArray<Int>()
        _gameState.value = _gameState.value.copy(eventValue = -1, index = _gameState.value.index+1)
    }

    override fun checkMatch() {
        if (currentEventIndex >= nBack && currentEventIndex < events.size) {
            val currentEvent = events[currentEventIndex]
            val previousEvent = events[currentEventIndex - nBack]

            if (currentEvent == previousEvent && !guessed) {
                // Add points for correct match
                _score.value += correctMatchPoints
            } else if (currentEvent != previousEvent && !guessed) {
                // It's an incorrect match
                _score.value -= incorrectMatchPenalty
            }
            guessed = true

            // Ensure score doesn't fall below minimum
            if (_score.value < minScore) {
                _score.value = minScore
            }
        }

    }
    private fun runAudioGame() {
        // Todo: Make work for Basic grade
    }

    private suspend fun runVisualGame(events: Array<Int>){
        for (value in events) {
            guessed = false
            currentEventIndex++
            _gameState.value = _gameState.value.copy(eventValue = value, index = _gameState.value.index+1)
            Log.d("EVENT", "" + _gameState.value.eventValue)
            delay(eventInterval)
        }
        endGame()
    }

    private fun runAudioVisualGame(){
        // Todo: Make work for Higher grade
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GameApplication)
                GameVM(application.userPreferencesRespository)
            }
        }
    }

    init {
        // Code that runs during creation of the vm
        viewModelScope.launch {
            userPreferencesRepository.highscore.collect {
                _highscore.value = it
            }
        }
    }
}

// Class with the different game types
enum class GameType{
    Audio,
    Visual,
    AudioVisual
}

data class GameState(
    // You can use this state to push values from the VM to your UI.
    val gameType: GameType = GameType.Visual,  // Type of the game
    val eventValue: Int = -1,  // The value of the array string
    val index: Int = 0
)

class FakeVM: GameViewModel{
    private val _gameState = MutableStateFlow(GameState())
    override val gameState: StateFlow<GameState>
        get() = _gameState.asStateFlow()
    override val score: StateFlow<Int>
        get() = MutableStateFlow(2).asStateFlow()
    override val highscore: StateFlow<Int>
        get() = MutableStateFlow(42).asStateFlow()
    override val nBack: Int
        get() = 2
    override val gridSize: Int
        get() = 5
    override val eventInterval: Long
        get() = 2000L

    override fun setGameType(gameType: GameType) {
        // update the gametype in the gamestate
        _gameState.value = _gameState.value.copy(gameType = gameType)
    }

    override fun startGame() {
    }

    override fun endGame() {
    }

    override fun checkMatch() {
    }
}