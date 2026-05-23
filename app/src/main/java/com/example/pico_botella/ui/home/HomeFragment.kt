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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.pico_botella.R
import com.example.pico_botella.databinding.FragmentHomeBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HomeViewModel by activityViewModels()
    
    private var blinkAnimation: Animation? = null
    private var mediaPlayer: MediaPlayer? = null
    private var lastAngle = 0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClicks()
        setupAnimations()
    }

    private fun setupObservers() {
        viewModel.isAudioEnabled.observe(viewLifecycleOwner) { isEnabled ->
            binding.btnPower.isActivated = !isEnabled
            if (isEnabled) {
                if (mediaPlayer == null) initMediaPlayer()
                if (isResumed) mediaPlayer?.start()
            } else {
                mediaPlayer?.pause()
            }
        }
    }

    private fun initMediaPlayer() {
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.background_music)
        mediaPlayer?.isLooping = true
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.isAudioEnabled.value == true) {
            if (mediaPlayer == null) initMediaPlayer()
            mediaPlayer?.start()
        }
    }

    override fun onPause() {
        super.onPause()
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
            viewModel.toggleAudio()
            val message = if (viewModel.isAudioEnabled.value == true) "Audio activo" else "Audio pausado"
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        binding.btnInfo.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_instructionsFragment)
        }

        binding.btnAdd.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_challengesFragment)
        }

        binding.btnShare.setOnClickListener {
            shareApp()
        }
    }

    private fun shareApp() {
        val appTitle = "App pico botella"
        val slogan = "Solo los valientes lo juegan !!"
        val url = "https://play.google.com/store/apps/details?id=com.nequi.MobileApp&hl=es_419&gl=es"
        val shareMessage = "$appTitle\n$slogan\n$url"

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, "Compartir")
        startActivity(shareIntent)
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
        mediaPlayer?.release()
        mediaPlayer = null
        _binding = null
    }
}