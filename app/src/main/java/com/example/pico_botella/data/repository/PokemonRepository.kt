package com.example.pico_botella.data.repository

import com.example.pico_botella.data.network.Pokemon
import com.example.pico_botella.data.network.PokemonApiService

class PokemonRepository(private val apiService: PokemonApiService) {
    private var cachedPokemonList: List<Pokemon>? = null

    suspend fun getRandomPokemon(): Pokemon? {
        return try {
            val list = cachedPokemonList ?: apiService.getPokedex().pokemon.also {
                cachedPokemonList = it
            }
            if (list.isNotEmpty()) list.random() else null
        } catch (e: Exception) {
            null
        }
    }
}