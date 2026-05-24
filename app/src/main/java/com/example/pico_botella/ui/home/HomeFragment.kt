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
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.pico_botella.R
import com.example.pico_botella.data.entity.Challenge
import com.example.pico_botella.data.network.Pokemon
import com.example.pico_botella.databinding.DialogRandomChallengeBinding
import com.example.pico_botella.databinding.FragmentHomeBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HomeViewModel by activityViewModels()
    
    private var blinkAnimation: Animation? = null
    private var backgroundMediaPlayer: MediaPlayer? = null
    private var bottleMediaPlayer: MediaPlayer? = null
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
                if (backgroundMediaPlayer == null) initBackgroundPlayer()
                if (isResumed) backgroundMediaPlayer?.start()
            } else {
                backgroundMediaPlayer?.pause()
            }
        }

        viewModel.randomChallenge.observe(viewLifecycleOwner) { result ->
            result?.let { (challenge, pokemon) ->
                showRandomChallengeDialog(challenge, pokemon)
                viewModel.clearRandomChallenge()
            }
        }
    }

    private fun initBackgroundPlayer() {
        backgroundMediaPlayer = MediaPlayer.create(requireContext(), R.raw.background_music)
        backgroundMediaPlayer?.isLooping = true
    }

    private fun initBottlePlayer() {
        // Asumiendo que el archivo se llama sonido_botella.mp3
        bottleMediaPlayer = MediaPlayer.create(requireContext(), R.raw.sonido_botella)
        bottleMediaPlayer?.isLooping = true
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.isAudioEnabled.value == true) {
            if (backgroundMediaPlayer == null) initBackgroundPlayer()
            backgroundMediaPlayer?.start()
        }
    }

    override fun onPause() {
        super.onPause()
        backgroundMediaPlayer?.pause()
        bottleMediaPlayer?.stop()
        bottleMediaPlayer?.release()
        bottleMediaPlayer = null
    }

    private fun setupAnimations() {
        try {
            blinkAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.blink)
            binding.vCircle.startAnimation(blinkAnimation)
        } catch (e: Exception) {}
    }

    private fun setupClicks() {
        binding.btnPressMeContainer.setOnClickListener {
            startGameSequence()
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

    private fun startGameSequence() {
        // C7: El botón desaparece mientras la partida está en proceso
        binding.vCircle.clearAnimation()
        binding.btnPressMeContainer.isVisible = false

        // C8: Pausar audio de fondo si estaba ON
        if (viewModel.isAudioEnabled.value == true) {
            backgroundMediaPlayer?.pause()
        }

        spinBottle()
    }

    private fun spinBottle() {
        // C1: Giro entre 3 y 5 segundos
        val spinDuration = Random.nextLong(3000, 5001)

        // C3: Giro aleatorio
        val randomDegrees = Random.nextInt(360, 3600).toFloat()
        val newAngle = lastAngle + randomDegrees

        val pivotX = binding.ivBottle.width / 2f
        val pivotY = binding.ivBottle.height / 2f
        
        // C4: Empieza desde donde se detuvo el anterior
        val rotateAnimation = RotateAnimation(
            lastAngle,
            newAngle,
            pivotX,
            pivotY
        ).apply {
            duration = spinDuration
            fillAfter = true
            interpolator = android.view.animation.DecelerateInterpolator()
        }
        
        rotateAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                // C2: Sonido de botella girando
                initBottlePlayer()
                bottleMediaPlayer?.start()
            }

            override fun onAnimationRepeat(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                // C2: Se pausa al detenerse
                bottleMediaPlayer?.stop()
                bottleMediaPlayer?.release()
                bottleMediaPlayer = null

                lastAngle = newAngle % 360
                startCountdown()
            }
        })
        binding.ivBottle.startAnimation(rotateAnimation)
    }

    private fun startCountdown() {
        // C5: Cuenta regresiva naranja (3, 2, 1, 0)
        lifecycleScope.launch {
            binding.tvCountdown.isVisible = true
            for (i in 3 downTo 0) {
                binding.tvCountdown.text = i.toString()
                delay(1000)
            }
            binding.tvCountdown.isVisible = false
            // C6: Al llegar a 0, mostrar el diálogo de HU-12
            viewModel.getRandomChallenge()
        }
    }

    private fun showRandomChallengeDialog(challenge: Challenge?, pokemon: Pokemon?) {
        val dialogBinding = DialogRandomChallengeBinding.inflate(layoutInflater)
        
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialogBinding.tvChallenge.text = challenge?.description ?: "No hay retos disponibles"

        pokemon?.let {
            val imgUrl = it.img.replace("http://", "https://")
            Glide.with(this)
                .load(imgUrl)
                .placeholder(R.drawable.botella_label)
                .into(dialogBinding.ivPokemon)
        }

        dialogBinding.btnClose.setOnClickListener {
            // Al cerrar, el juego regresa al home listo para una nueva partida
            // C7: El botón reaparece
            binding.btnPressMeContainer.isVisible = true
            binding.vCircle.startAnimation(blinkAnimation)

            // C8: Reanudar audio de fondo si estaba ON
            if (viewModel.isAudioEnabled.value == true) {
                backgroundMediaPlayer?.start()
            }

            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        backgroundMediaPlayer?.release()
        backgroundMediaPlayer = null
        bottleMediaPlayer?.release()
        bottleMediaPlayer = null
        _binding = null
    }
}