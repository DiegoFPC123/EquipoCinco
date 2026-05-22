package com.example.pico_botella.ui.home

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
    
    // Para guardar la última posición de la botella y que el siguiente giro sea fluido
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

    private fun setupAnimations() {
        // C6: Configurar animación de parpadeo infinita para el círculo naranja
        try {
            blinkAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.blink)
            binding.vCircle.startAnimation(blinkAnimation)
        } catch (e: Exception) {
            // Si la animación no existe aún, evitamos el crash
        }
    }

    private fun setupClicks() {
        binding.btnPressMeContainer.setOnClickListener {
            // Detener animación y ocultar el botón al iniciar el juego
            binding.vCircle.clearAnimation()
            binding.btnPressMeContainer.isVisible = false
            
            // C5: Iniciar contador regresivo
            startCountdown()
        }

        binding.btnStar.setOnClickListener {
            Toast.makeText(context, "Estrella", Toast.LENGTH_SHORT).show()
        }
        binding.btnPower.setOnClickListener {
            Toast.makeText(context, "Sonido/Energía", Toast.LENGTH_SHORT).show()
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
            
            // Conteo regresivo del 3 al 1 con delay de 1 segundo (usando Corrutinas)
            for (i in 3 downTo 1) {
                binding.tvCountdown.text = i.toString()
                delay(1000) // Suspensión que no bloquea el hilo principal
            }
            
            binding.tvCountdown.isVisible = false
            spinBottle()
        }
    }

    private fun spinBottle() {
        // Generar un giro aleatorio (mínimo 2 vueltas completas para emoción)
        val randomSpin = Random.nextInt(3600).toFloat() + 720
        val newAngle = randomSpin
        
        val pivotX = binding.ivBottle.width / 2f
        val pivotY = binding.ivBottle.height / 2f
        
        // Animación de rotación desde la última posición conocida
        val rotateAnim = RotateAnimation(
            lastAngle,
            newAngle,
            pivotX,
            pivotY
        ).apply {
            duration = 3000
            fillAfter = true // Mantiene la botella en la posición final escogida
            interpolator = android.view.animation.DecelerateInterpolator()
        }
        
        rotateAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                // Guardamos el ángulo actual para que el próximo giro sea coherente
                lastAngle = newAngle % 360
                
                // Volvemos a mostrar el botón para permitir otra ronda
                binding.btnPressMeContainer.isVisible = true
                binding.vCircle.startAnimation(blinkAnimation)
                
                Toast.makeText(context, "¡Reto seleccionado!", Toast.LENGTH_SHORT).show()
            }
        })

        binding.ivBottle.startAnimation(rotateAnim)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}