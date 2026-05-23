package com.example.pico_botella.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface PokemonApiService {
    @GET("master/pokedex.json")
    suspend fun getPokedex(): PokemonResponse

    companion object {
        private const val BASE_URL = "https://raw.githubusercontent.com/Biuni/PokemonGO-Pokedex/"

        fun create(): PokemonApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PokemonApiService::class.java)
        }
    }
}