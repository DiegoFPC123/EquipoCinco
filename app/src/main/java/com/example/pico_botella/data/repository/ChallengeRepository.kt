package com.example.pico_botella.data.repository

import com.example.pico_botella.data.dao.ChallengeDao
import com.example.pico_botella.data.entity.Challenge
import kotlinx.coroutines.flow.Flow

class ChallengeRepository(private val challengeDao: ChallengeDao) {
    
    val allChallenges: Flow<List<Challenge>> = challengeDao.getAllChallenges()

    suspend fun insertChallenge(challenge: Challenge) {
        challengeDao.insertChallenge(challenge)
    }

    suspend fun updateChallenge(challenge: Challenge) {
        challengeDao.updateChallenge(challenge)
    }

    suspend fun deleteChallenge(challenge: Challenge) {
        challengeDao.deleteChallenge(challenge)
    }
}