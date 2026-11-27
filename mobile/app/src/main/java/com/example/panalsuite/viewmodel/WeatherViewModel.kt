package com.example.panalsuite.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.panalsuite.remote.ApiResult
import com.example.panalsuite.repository.WeatherInfo
import com.example.panalsuite.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data class Success(val info: WeatherInfo) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

// MVVM: la pantalla solo observa "state"; la carga y los errores se manejan aqui.
class WeatherViewModel(
    private val repository: WeatherRepository = WeatherRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val state: StateFlow<WeatherUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.value = WeatherUiState.Loading
        viewModelScope.launch {
            _state.value = when (val result = repository.getCurrentWeather(SANTIAGO_LAT, SANTIAGO_LON)) {
                is ApiResult.Success -> WeatherUiState.Success(result.data)
                is ApiResult.Failure -> WeatherUiState.Error(result.message)
            }
        }
    }

    private companion object {
        const val SANTIAGO_LAT = -33.4489
        const val SANTIAGO_LON = -70.6693
    }
}
