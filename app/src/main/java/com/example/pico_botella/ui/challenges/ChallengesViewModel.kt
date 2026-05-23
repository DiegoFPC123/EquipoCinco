package com.example.pico_botella.ui.challenges

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.pico_botella.data.database.AppDatabase
import com.example.pico_botella.data.entity.Challenge
import kotlinx.coroutines.launch

class ChallengesViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).challengeDao()
    val allChallenges = dao.getAllChallenges().asLiveData()

    fun addChallenge(description: String) {
        viewModelScope.launch {
            dao.insertChallenge(Challenge(description = description))
        }
    }

    fun updateChallenge(challenge: Challenge) {
        viewModelScope.launch {
            dao.updateChallenge(challenge)
        }
    }

    fun deleteChallenge(challenge: Challenge) {
        viewModelScope.launch {
            dao.deleteChallenge(challenge)
        }
    }
}