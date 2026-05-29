package com.example.pico_botella.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.pico_botella.data.database.AppDatabase
import com.example.pico_botella.data.entity.Challenge
import com.example.pico_botella.data.network.Pokemon
import com.example.pico_botella.data.network.PokemonApiService
import com.example.pico_botella.data.repository.ChallengeRepository
import com.example.pico_botella.data.repository.PokemonRepository
import kotlinx.coroutines.launch

data class ChallengeResult(val challenge: Challenge, val pokemon: Pokemon)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val challengeRepository: ChallengeRepository
    private val pokemonRepository: PokemonRepository
    
    private val _isAudioEnabled = MutableLiveData<Boolean>(true)
    val isAudioEnabled: LiveData<Boolean> get() = _isAudioEnabled

    private val _challengeResult = MutableLiveData<ChallengeResult?>()
    val challengeResult: LiveData<ChallengeResult?> get() = _challengeResult

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    init {
        val dao = AppDatabase.getDatabase(application).challengeDao()
        challengeRepository = ChallengeRepository(dao)
        pokemonRepository = PokemonRepository(PokemonApiService.create())
        
        // Pre-cargamos los pokémon para que la primera vez sea instantáneo (Solución C2)
        viewModelScope.launch {
            try {
                pokemonRepository.getRandomPokemon()
            } catch (e: Exception) {
                // Silencioso, se reintentará al girar
            }
        }
    }

    fun toggleAudio() {
        _isAudioEnabled.value = !(_isAudioEnabled.value ?: true)
    }

    fun fetchRandomChallengeAndPokemon() {
        viewModelScope.launch {
            try {
                val challenge = challengeRepository.getRandomChallenge()
                val pokemon = pokemonRepository.getRandomPokemon()

                if (challenge != null && pokemon != null) {
                    // postValue asegura que se notifique al hilo principal correctamente
                    _challengeResult.postValue(ChallengeResult(challenge, pokemon))
                } else if (challenge == null) {
                    _error.postValue("No hay retos guardados. ¡Agrega uno primero!")
                }
            } catch (e: Exception) {
                _error.postValue("Error al conectar con la Pokedex")
            }
        }
    }

    fun clearChallengeResult() {
        _challengeResult.value = null
    }

    fun clearError() {
        _error.value = null
    }
}