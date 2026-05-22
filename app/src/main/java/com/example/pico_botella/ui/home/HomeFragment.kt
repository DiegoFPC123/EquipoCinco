package com.example.pico_botella.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.pico_botella.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel

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
    }

    private fun setupClicks() {
        binding.btnStar.setOnClickListener {
            Toast.makeText(context, "Estrella", Toast.LENGTH_SHORT).show()
        }
        binding.btnPower.setOnClickListener {
            Toast.makeText(context, "Encendido/Apagado", Toast.LENGTH_SHORT).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}