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
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).challengeDao()
    private val pokemonApi = PokemonApiService.create()

    private val _isAudioEnabled = MutableLiveData<Boolean>(true)
    val isAudioEnabled: LiveData<Boolean> get() = _isAudioEnabled

    private val _randomChallenge = MutableLiveData<Pair<Challenge?, Pokemon?>>()
    val randomChallenge: LiveData<Pair<Challenge?, Pokemon?>> get() = _randomChallenge

    private var pokemonList: List<Pokemon> = emptyList()

    init {
        fetchPokemons()
    }

    private fun fetchPokemons() {
        viewModelScope.launch {
            try {
                val response = pokemonApi.getPokedex()
                pokemonList = response.pokemon
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleAudio() {
        _isAudioEnabled.value = !(_isAudioEnabled.value ?: true)
    }

    fun getRandomChallenge() {
        viewModelScope.launch {
            val challenge = dao.getRandomChallenge()
            val pokemon = if (pokemonList.isNotEmpty()) pokemonList.random() else null
            _randomChallenge.value = Pair(challenge, pokemon)
        }
    }

    fun clearRandomChallenge() {
        _randomChallenge.value = null
    }
}