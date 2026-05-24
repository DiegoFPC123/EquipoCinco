package com.example.pico_botella.ui.challenges

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.pico_botella.data.database.AppDatabase
import com.example.pico_botella.data.entity.Challenge
import com.example.pico_botella.data.repository.ChallengeRepository
import kotlinx.coroutines.launch

class ChallengesViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: ChallengeRepository
    val allChallenges: androidx.lifecycle.LiveData<List<Challenge>>

    init {
        val dao = AppDatabase.getDatabase(application).challengeDao()
        repository = ChallengeRepository(dao)
        allChallenges = repository.allChallenges.asLiveData()
    }

    fun addChallenge(description: String) {
        viewModelScope.launch {
            repository.insertChallenge(Challenge(description = description))
        }
    }

    fun updateChallenge(challenge: Challenge) {
        viewModelScope.launch {
            repository.updateChallenge(challenge)
        }
    }

    fun deleteChallenge(challenge: Challenge) {
        viewModelScope.launch {
            repository.deleteChallenge(challenge)
        }
    }
}