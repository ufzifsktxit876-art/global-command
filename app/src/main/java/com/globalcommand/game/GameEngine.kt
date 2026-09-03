package com.globalcommand.game

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class GameEngine(
    initialState: GameState,
    private val saveRepo: SaveGameRepository
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val _gameState = MutableStateFlow(initialState)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val events: SharedFlow<String> = _events.asSharedFlow()

    private var loopJob: Job? = null

    init {
        loopJob = scope.launch {
            while (isActive) {
                val s = _gameState.value
                if (!s.isPaused) {
                    val delayMs = when (s.simulationSpeed) {
                        1 -> 1200L
                        2 -> 700L
                        3 -> 350L
                        4 -> 150L
                        else -> 40L
                    }
                    delay(delayMs)
                    val nextDate = s.currentDate.advanceOneHour()
                    _gameState.value = s.copy(currentDate = nextDate, tickCounter = s.tickCounter + 1)
                } else {
                    delay(100L)
                }
            }
        }
    }

    fun togglePause() { _gameState.value = _gameState.value.copy(isPaused = !_gameState.value.isPaused) }
    fun setSpeed(spd: Int) { _gameState.value = _gameState.value.copy(simulationSpeed = spd.coerceIn(1, 5)) }
    fun selectProvince(id: Int?) {
        val prov = id?.let { _gameState.value.provinces[it] }
        _gameState.value = _gameState.value.copy(selectedProvinceId = id, selectedCountryId = prov?.controllerCountryId ?: _gameState.value.selectedCountryId)
    }

    fun shutdown() {
        loopJob?.cancel()
        scope.cancel()
    }
}
