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
import com.example.pico_botella.databinding.FragmentHomeBinding
import com.example.pico_botella.databinding.DialogRandomChallengeBinding
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

    // Variables para sincronizar el diálogo con el conteo (C6 y C7)
    private var pendingResult: ChallengeResult? = null
    private var isCountdownFinished = false

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

        // C3 y C2: Observamos el resultado
        viewModel.challengeResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                pendingResult = it
                checkAndShowDialog()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                // Si hay error, permitimos jugar de nuevo
                binding.btnPressMeContainer.isVisible = true
                binding.vCircle.startAnimation(blinkAnimation)
                viewModel.clearError()
            }
        }
    }

    private fun checkAndShowDialog() {
        val result = pendingResult
        if (isCountdownFinished && result != null) {
            showRandomChallengeDialog(result)
            pendingResult = null
            isCountdownFinished = false
            viewModel.clearChallengeResult()
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
            binding.btnPressMeContainer.isVisible = false // C7: El botón desaparece mientras la partida está en proceso
            spinBottle()
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
            Toast.makeText(context, "Compartir", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCountdown() {
        lifecycleScope.launch {
            isCountdownFinished = false
            binding.tvCountdown.isVisible = true
            for (i in 3 downTo 0) { // C6 y C7: Cuenta regresiva hasta 0
                binding.tvCountdown.text = i.toString()
                if (i == 0) {
                    // C7: El botón reaparece al llegar a 0
                    binding.btnPressMeContainer.isVisible = true
                    binding.vCircle.startAnimation(blinkAnimation)
                    
                    // C6: Marcamos listo para mostrar el diálogo al llegar a 0
                    isCountdownFinished = true
                    checkAndShowDialog()
                }
                delay(1000)
            }
            binding.tvCountdown.isVisible = false
        }
    }

    private fun spinBottle() {
        // Adelantamos la petición para que el resultado esté listo al terminar el conteo
        viewModel.fetchRandomChallengeAndPokemon()

        // Aseguramos aleatoriedad visual: giro mínimo de 5 vueltas + ángulo al azar
        val randomSpin = (Random.nextInt(5) + 5) * 360 + Random.nextInt(360)
        val newAngle = lastAngle + randomSpin.toFloat()
        
        val rotateAnimation = RotateAnimation(
            lastAngle,
            newAngle,
            RotateAnimation.RELATIVE_TO_SELF, 0.5f,
            RotateAnimation.RELATIVE_TO_SELF, 0.5f
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
                // Al terminar el giro, iniciamos la cuenta regresiva final (C6)
                startCountdown()
            }
        })
        binding.ivBottle.startAnimation(rotateAnimation)
    }

    private fun showRandomChallengeDialog(result: ChallengeResult) {
        val dialogBinding = DialogRandomChallengeBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false) // C6: Solo se cierra con "Cerrar"
            .create()

        // C3: Texto del reto (traído desde SQLite)
        dialogBinding.tvChallenge.text = result.challenge.description

        // C2: Carga de imagen Pokemon desde la API
        val imageUrl = result.pokemon.img.replace("http://", "https://")
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_launcher_foreground)
            .into(dialogBinding.ivPokemon)

        // C4: Botón "Cerrar"
        dialogBinding.btnClose.setOnClickListener {
            dialog.dismiss()
            // C5: El juego regresa al home listo para una nueva partida
            binding.btnPressMeContainer.isVisible = true
            binding.vCircle.startAnimation(blinkAnimation)
        }

        dialog.show()
        // C1: Fondo con transparencia sutil
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer?.release()
        mediaPlayer = null
        _binding = null
    }
}
