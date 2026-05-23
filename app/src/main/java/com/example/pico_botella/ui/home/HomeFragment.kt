package com.example.pico_botella.ui.home

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.pico_botella.R
import com.example.pico_botella.databinding.FragmentHomeBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel
    private var blinkAnimation: Animation? = null
    
    private var mediaPlayer: MediaPlayer? = null
    private var lastAngle = 0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClicks()
        setupAnimations()
    }

    override fun onStart() {
        super.onStart()
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(requireContext(), R.raw.background_music)
            mediaPlayer?.isLooping = true
        }
        // Solo iniciamos si no estaba pausado por el botón
        if (!binding.btnPower.isActivated) {
            mediaPlayer?.start()
        }
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.pause()
    }

    private fun setupAnimations() {
        try {
            blinkAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.blink)
            binding.vCircle.startAnimation(blinkAnimation)
        } catch (e: Exception) {}
    }

    private fun setupClicks() {
        binding.btnPressMeContainer.setOnClickListener {
            binding.vCircle.clearAnimation()
            binding.btnPressMeContainer.isVisible = false
            startCountdown()
        }

        binding.btnStar.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=com.nequi.MobileApp&hl=es_419&gl=es")
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "No se pudo abrir la tienda", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnPower.setOnClickListener {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                it.isActivated = true // Cambia al icono con línea (Mute)
                Toast.makeText(context, "Audio pausado", Toast.LENGTH_SHORT).show()
            } else {
                mediaPlayer?.start()
                it.isActivated = false // Cambia al icono normal
                Toast.makeText(context, "Audio activo", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnInfo.setOnClickListener {
            Toast.makeText(context, "Instrucciones", Toast.LENGTH_SHORT).show()
        }
        binding.btnAdd.setOnClickListener {
            Toast.makeText(context, "Agregar retos", Toast.LENGTH_SHORT).show()
        }
        binding.btnShare.setOnClickListener {
            Toast.makeText(context, "Compartir", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCountdown() {
        lifecycleScope.launch {
            binding.tvCountdown.isVisible = true
            for (i in 3 downTo 1) {
                binding.tvCountdown.text = i.toString()
                delay(1000)
            }
            binding.tvCountdown.isVisible = false
            spinBottle()
        }
    }

    private fun spinBottle() {
        val randomSpin = Random.nextInt(3600).toFloat() + 720
        val newAngle = randomSpin
        val pivotX = binding.ivBottle.width / 2f
        val pivotY = binding.ivBottle.height / 2f
        
        val rotateAnimation = RotateAnimation(
            lastAngle,
            newAngle,
            pivotX,
            pivotY
        ).apply {
            duration = 3000
            fillAfter = true
            interpolator = android.view.animation.DecelerateInterpolator()
        }
        
        rotateAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                lastAngle = newAngle % 360
                binding.btnPressMeContainer.isVisible = true
                binding.vCircle.startAnimation(blinkAnimation)
                Toast.makeText(context, "¡Reto seleccionado!", Toast.LENGTH_SHORT).show()
            }
        })
        binding.ivBottle.startAnimation(rotateAnimation)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        _binding = null
    }
}