package mobappdev.example.nback_cimpl.ui.viewmodels

import android.speech.tts.TextToSpeech
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
import java.lang.Thread.State
import kotlin.math.min

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
    val gameType: StateFlow<GameType>
    val visualState: StateFlow<GameState>
    val audioState: StateFlow<GameState>
    val score: StateFlow<Int>
    val highscore: StateFlow<Int>
    val nBack: StateFlow<Int>
    val gridSize: StateFlow<Int>
    val eventInterval: StateFlow<Long>
    val numberOfEvents: StateFlow<Int>
    val currentEventIndex: StateFlow<Int>

    val visualGuess: StateFlow<Boolean>
    val audioGuess: StateFlow<Boolean>
    val visualFeedback: StateFlow<GuessFeedback>
    val audioFeedback: StateFlow<GuessFeedback>

    fun setGameType(gameType: GameType)
    fun startGame()
    fun endGame()

    fun checkMatch(type: GameType)

    fun saveNBackLevel(level: Int)
    fun saveGridSize(size: Int)
    fun saveNumEvents(events: Int)
    fun saveEventInterval(interval: Long)
}

class GameVM(
    private val application: GameApplication,
    private val userPreferencesRepository: UserPreferencesRepository
) : GameViewModel, ViewModel() {
    private val _gameType = MutableStateFlow(GameType.Visual)
    override val gameType: StateFlow<GameType>
        get() = _gameType.asStateFlow()


    private val _visualState = MutableStateFlow(GameState())
    override val visualState: StateFlow<GameState>
        get() = _visualState.asStateFlow()

    private val _audioState = MutableStateFlow(GameState())
    override val audioState: StateFlow<GameState>
        get() = _audioState.asStateFlow()

    private val _score = MutableStateFlow(0)
    override val score: StateFlow<Int>
        get() = _score

    private val _highscore = MutableStateFlow(0)
    override val highscore: StateFlow<Int>
        get() = _highscore

    private val _nBack = MutableStateFlow(1)
    override val nBack: StateFlow<Int>
        get() = _nBack

    private val _gridSize = MutableStateFlow(3)
    override val gridSize: StateFlow<Int>
        get() = _gridSize

    private val _eventInterval = MutableStateFlow(2000L)
    override val eventInterval: StateFlow<Long>
        get() = _eventInterval

    private val _numberOfEvents = MutableStateFlow(10)
    override val numberOfEvents: StateFlow<Int>
        get() = _numberOfEvents

    private var _textToSpeech: TextToSpeech? = null

    private var job: Job? = null  // coroutine job for the game event
    private val nBackHelper = NBackHelper()  // Helper that generate the event array

    private var audioEvents = emptyArray<Int>()  // Array with all events
    private var visualEvents = emptyArray<Int>()  // Array with all events

    private val _currentEventIndex = MutableStateFlow(10)
    override val currentEventIndex: StateFlow<Int>
        get() = _currentEventIndex

    private val _visualGuess = MutableStateFlow(false)
    override val visualGuess: StateFlow<Boolean>
        get() = _visualGuess
    private val _audioGuess = MutableStateFlow(false)
    override val audioGuess: StateFlow<Boolean>
        get() = _audioGuess

    private val _visualFeedback = MutableStateFlow(GuessFeedback.None)
    override val visualFeedback: StateFlow<GuessFeedback> get() = _visualFeedback.asStateFlow()
    private val _audioFeedback = MutableStateFlow(GuessFeedback.None)
    override val audioFeedback: StateFlow<GuessFeedback> get() = _audioFeedback.asStateFlow()

    private var isInitialized = false

    override fun setGameType(gameType: GameType) {
        _gameType.value = gameType
    }

    override fun startGame() {
        job?.cancel()  // Cancel any existing game loop
        _currentEventIndex.value = 0
        _score.value = 0

        _visualState.value.finished = false
        _audioState.value.finished = false

        // Get the events from our C-model (returns IntArray, so we need to convert to Array<Int>)
        if (gameType.value != GameType.Audio) {
            visualEvents = nBackHelper.generateNBackString(
                numberOfEvents.value,
                gridSize.value * gridSize.value,
                35,
                nBack.value
            ).toList()
                .toTypedArray()
            Log.d(
                "GameVM",
                "The following visual sequence was generated: ${visualEvents.contentToString()}"
            )
        }
        if (gameType.value != GameType.Visual) {
            audioEvents = nBackHelper.generateNBackString(
                numberOfEvents.value,
                min(gridSize.value * gridSize.value, 26),
                25,
                nBack.value
            ).toList()
                .toTypedArray()
            Log.d(
                "GameVM",
                "The following audio sequence was generated: ${audioEvents.contentToString()}"
            )
        }

        job = viewModelScope.launch {
            when (gameType.value) {
                GameType.Audio -> runAudioGame()
                GameType.AudioVisual -> runAudioVisualGame()
                GameType.Visual -> runVisualGame()
            }

            _visualState.value.finished = true
            _audioState.value.finished = true

            endGame()
            saveHighscore()
        }
    }

    override fun endGame() {
        job?.cancel()
        _currentEventIndex.value = 0

        audioEvents = emptyArray<Int>()
        visualEvents = emptyArray<Int>()

        _visualState.value =
            _visualState.value.copy(eventValue = -1, index = 0)
        _audioState.value =
            _audioState.value.copy(eventValue = -1, index = 0)

        _visualFeedback.value = GuessFeedback.None
        _audioFeedback.value = GuessFeedback.None
    }

    override fun checkMatch(type: GameType) {
        if (_currentEventIndex.value >= nBack.value &&
            _currentEventIndex.value < if (type == GameType.Visual) visualEvents.size else audioEvents.size
        ) {

            // Get relevant events
            val currentEvent =
                if (type == GameType.Visual) visualEvents[_currentEventIndex.value]
                else audioEvents[_currentEventIndex.value]
            val previousEvent =
                if (type == GameType.Visual) visualEvents[_currentEventIndex.value - nBack.value]
                else audioEvents[_currentEventIndex.value - nBack.value]

            // Give/Remove points based on correctness, user can only make one guess per round
            if (currentEvent == previousEvent && if (type == GameType.Visual) !_visualGuess.value else !_audioGuess.value) {
                _score.value += 1
                if (type == GameType.Visual)
                    _visualFeedback.value = GuessFeedback.Correct
                else
                    _audioFeedback.value = GuessFeedback.Correct
            } else if (currentEvent != previousEvent && if (type == GameType.Visual) !_visualGuess.value else !_audioGuess.value) {
                _score.value -= 1
                if (type == GameType.Visual)
                    _visualFeedback.value = GuessFeedback.Incorrect
                else
                    _audioFeedback.value = GuessFeedback.Incorrect
            }

            if (type == GameType.Visual)
                _visualGuess.value = true
            else
                _audioGuess.value = true

            // Ensure score doesn't fall below minimum
            if (_score.value < 0) {
                _score.value = 0
            }
        }

    }

    private fun sayLetter() {
        val eventValue = _audioState.value.eventValue

        // Ensure the eventValue is within 1 to 26
        if (eventValue in 1..26) {
            // Calculate the character based on the eventValue
            val letter = ('A'.code + eventValue - 1).toChar()
            _textToSpeech?.speak(letter.toString(), TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Log.e("sayLetter", "Invalid eventValue: $eventValue. Must be between 1 and 26.")
        }
    }

    private suspend fun runAudioGame() {
        // Delay so that GameScreen can load and user has time to react
        delay(1500L)

        for (value in audioEvents) {
            _audioFeedback.value = GuessFeedback.None
            _audioGuess.value = false

            _audioState.value =
                _audioState.value.copy(eventValue = value, index = _visualState.value.index + 1)
            Log.d("EVENT", "" + _audioState.value.eventValue)
            sayLetter()

            delay(eventInterval.value)
            _currentEventIndex.value++

        }
    }

    private suspend fun runVisualGame() {
        // Delay so that GameScreen can load and user has time to react
        delay(1500L)

        for (value in visualEvents) {
            _visualFeedback.value = GuessFeedback.None
            _visualGuess.value = false

            _visualState.value =
                _visualState.value.copy(eventValue = value, index = _visualState.value.index + 1)
            Log.d("EVENT", "" + _visualState.value.eventValue)

            delay(eventInterval.value)
            _currentEventIndex.value++
        }
    }

    private suspend fun runAudioVisualGame() {
        delay(1500L)

        for (i in 0 until numberOfEvents.value) {
            _visualFeedback.value = GuessFeedback.None
            _audioFeedback.value = GuessFeedback.None
            _visualGuess.value = false
            _audioGuess.value = false

            _audioState.value =
                _audioState.value.copy(
                    eventValue = audioEvents[_currentEventIndex.value],
                    index = _visualState.value.index + 1
                )
            Log.d("EVENT", "" + _audioState.value.eventValue)
            sayLetter()

            _visualState.value =
                _visualState.value.copy(
                    eventValue = visualEvents[_currentEventIndex.value],
                    index = _visualState.value.index + 1
                )
            Log.d("EVENT", "" + _visualState.value.eventValue)

            delay(eventInterval.value)
            _currentEventIndex.value++
        }
    }

    // Method to save Highscore
    private fun saveHighscore() {
        // Update high score if the current score is higher
        if (_score.value > _highscore.value) {
            _highscore.value = _score.value
            viewModelScope.launch {
                userPreferencesRepository.saveHighScore(_highscore.value)
            }
        }
    }

    // Method to save N-Back level
    override fun saveNBackLevel(level: Int) {
        if (isInitialized) {
            _nBack.value = level
            viewModelScope.launch {
                userPreferencesRepository.saveNBackLevel(_nBack.value)
            }
        }
    }

    // Method to save Grid Size
    override fun saveGridSize(size: Int) {
        if (isInitialized) {
            _gridSize.value = size
            viewModelScope.launch {
                userPreferencesRepository.saveGridSize(_gridSize.value)
            }
        }
    }

    // Method to save Number of Events
    override fun saveNumEvents(events: Int) {
        if (isInitialized) {
            _numberOfEvents.value = events
            viewModelScope.launch {
                userPreferencesRepository.saveNumEvents(_numberOfEvents.value)
            }
        }
    }

    // Method to save Event Interval
    override fun saveEventInterval(interval: Long) {
        if (isInitialized) {
            _eventInterval.value = interval
            viewModelScope.launch {
                userPreferencesRepository.saveEventInterval(_eventInterval.value)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GameApplication)
                GameVM(application, application.userPreferencesRespository)
            }
        }
    }

    init {
        // Initiate Text-to-Speech
        viewModelScope.launch {
            _textToSpeech = TextToSpeech(application) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    println("TextToSpeech Initialized Successfully")
                }
            }
        }

        // Get HighScore and User Settings
        viewModelScope.launch {
            userPreferencesRepository.userPreferencesFlow.collect { preferences ->
                _highscore.value = preferences.highscore
                _nBack.value = preferences.nBackLevel
                _gridSize.value = preferences.gridSize
                _numberOfEvents.value = preferences.numEvents
                _eventInterval.value = preferences.eventInterval
                isInitialized = true
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        _textToSpeech?.shutdown()

    }
}

// Class with the different game types
enum class GameType {
    Audio,
    Visual,
    AudioVisual
}

data class GameState(
    // You can use this state to push values from the VM to your UI.
    val eventValue: Int = -1,  // The value of the array string
    val index: Int = 0,
    var finished: Boolean = false
)

enum class GuessFeedback {
    None,
    Correct,
    Incorrect
}

class FakeVM : GameViewModel {
    private val _gameState = MutableStateFlow(GameState())
    override val gameType: StateFlow<GameType>
        get() = TODO("Not yet implemented")
    override val visualState: StateFlow<GameState>
        get() = _gameState.asStateFlow()
    override val audioState: StateFlow<GameState>
        get() = TODO("Not yet implemented")
    override val score: StateFlow<Int>
        get() = MutableStateFlow(2).asStateFlow()
    override val highscore: StateFlow<Int>
        get() = MutableStateFlow(42).asStateFlow()
    override val nBack: StateFlow<Int>
        get() = MutableStateFlow(42).asStateFlow()
    override val gridSize: StateFlow<Int>
        get() = MutableStateFlow(42).asStateFlow()
    override val eventInterval: StateFlow<Long>
        get() = MutableStateFlow(2000L).asStateFlow()
    override val numberOfEvents: StateFlow<Int>
        get() = MutableStateFlow(10).asStateFlow()
    override val currentEventIndex: StateFlow<Int>
        get() = TODO("Not yet implemented")
    override val visualGuess: StateFlow<Boolean>
        get() = MutableStateFlow(false).asStateFlow()
    override val audioGuess: StateFlow<Boolean>
        get() = MutableStateFlow(false).asStateFlow()
    override val visualFeedback: StateFlow<GuessFeedback>
        get() = TODO("Not yet implemented")
    override val audioFeedback: StateFlow<GuessFeedback>
        get() = TODO("Not yet implemented")

    override fun setGameType(gameType: GameType) {
        // update the gametype in the gamestate
    }

    override fun startGame() {
    }

    override fun endGame() {
    }

    override fun checkMatch(type: GameType) {
    }

    override fun saveNBackLevel(level: Int) {
    }

    override fun saveGridSize(size: Int) {
    }

    override fun saveNumEvents(events: Int) {
    }

    override fun saveEventInterval(interval: Long) {
    }
}