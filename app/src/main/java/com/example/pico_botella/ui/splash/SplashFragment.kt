package com.example.pico_botella.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.pico_botella.R
import com.example.pico_botella.databinding.FragmentSplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * SplashFragment: Pantalla de inicio de la aplicación.
 * Muestra una animación de botella y navega al Home tras 5 segundos.
 */
class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Iniciar animación de la botella
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.bottle_animation)
        binding.ivBottle.startAnimation(animation)

        // 2. Temporizador de 5 segundos usando corrutinas (lifecycleScope)
        viewLifecycleOwner.lifecycleScope.launch {
            delay(5000)
            navigateToHome()
        }
    }

    private fun navigateToHome() {
        // Navegación segura usando Navigation Component
        // El popUpTo se encarga de limpiar el Splash del backstack (definido en nav_graph.xml)
        if (isAdded) {
            findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}