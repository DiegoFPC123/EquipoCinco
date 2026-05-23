package com.example.pico_botella.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {
    private val _isAudioEnabled = MutableLiveData<Boolean>(true)
    val isAudioEnabled: LiveData<Boolean> get() = _isAudioEnabled

    fun toggleAudio() {
        _isAudioEnabled.value = !(_isAudioEnabled.value ?: true)
    }

    fun setAudioEnabled(enabled: Boolean) {
        _isAudioEnabled.value = enabled
    }
}