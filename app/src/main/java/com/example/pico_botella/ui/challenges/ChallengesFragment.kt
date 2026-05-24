package com.example.pico_botella.ui.challenges

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.pico_botella.R
import com.example.pico_botella.data.entity.Challenge
import com.example.pico_botella.databinding.FragmentChallengesBinding
import com.example.pico_botella.databinding.DialogDeleteChallengeBinding
import com.example.pico_botella.databinding.DialogAddChallengeBinding
import com.example.pico_botella.databinding.DialogEditarRetoBinding

class ChallengesFragment : Fragment() {

    private var _binding: FragmentChallengesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChallengesViewModel by viewModels()
    private lateinit var adapter: ChallengesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChallengesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        adapter = ChallengesAdapter(
            onEdit = { challenge -> showEditChallengeDialog(challenge) },
            onDelete = { challenge -> showDeleteConfirmationDialog(challenge) }
        )
        binding.rvChallenges.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.allChallenges.observe(viewLifecycleOwner) { challenges ->
            adapter.submitList(challenges)
        }
    }

    private fun setupListeners() {
        binding.fabAddChallenge.setOnClickListener {
            showAddChallengeDialog()
        }
    }

    /**
     * Criterio 5: Botón Guardar dinámico para Agregar Reto
     */
    private fun showAddChallengeDialog() {
        val dialogBinding = DialogAddChallengeBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        // Lógica de validación dinámica (Criterio 5)
        val updateSaveButtonState = {
            val text = dialogBinding.etChallenge.text.toString()
            val isValid = text.isNotBlank()
            dialogBinding.btnSave.isEnabled = isValid
            
            if (isValid) {
                // Color naranja del proyecto cuando está habilitado
                dialogBinding.btnSave.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.orange_pico)
                )
                dialogBinding.btnSave.alpha = 1.0f
            } else {
                // Color gris cuando está inhabilitado
                dialogBinding.btnSave.backgroundTintList = ColorStateList.valueOf(
                    Color.parseColor("#E0E0E0")
                )
                dialogBinding.btnSave.alpha = 0.5f
            }
        }

        // Estado inicial (Inhabilitado)
        updateSaveButtonState()

        // Escuchar cambios de texto (viceversa)
        dialogBinding.etChallenge.doAfterTextChanged {
            updateSaveButtonState()
        }

        dialogBinding.btnSave.setOnClickListener {
            val description = dialogBinding.etChallenge.text.toString()
            if (description.isNotBlank()) {
                viewModel.addChallenge(description)
                dialog.dismiss()
            }
        }

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    /**
     * Criterio 5 aplicado también a Editar Reto
     */
    private fun showEditChallengeDialog(challenge: Challenge) {
        val dialogBinding = DialogEditarRetoBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialogBinding.etReto.setText(challenge.description)

        val updateSaveButtonState = {
            val text = dialogBinding.etReto.text.toString()
            val isValid = text.isNotBlank()
            dialogBinding.btnGuardar.isEnabled = isValid
            
            if (isValid) {
                dialogBinding.btnGuardar.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.orange_pico)
                )
                dialogBinding.btnGuardar.alpha = 1.0f
            } else {
                dialogBinding.btnGuardar.backgroundTintList = ColorStateList.valueOf(
                    Color.parseColor("#E0E0E0")
                )
                dialogBinding.btnGuardar.alpha = 0.5f
            }
        }

        // Estado inicial basado en el texto existente
        updateSaveButtonState()

        dialogBinding.etReto.doAfterTextChanged {
            updateSaveButtonState()
        }

        dialogBinding.btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnGuardar.setOnClickListener {
            val newDescription = dialogBinding.etReto.text.toString()
            if (newDescription.isNotBlank()) {
                viewModel.updateChallenge(challenge.copy(description = newDescription))
                dialog.dismiss()
            }
        }

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun showDeleteConfirmationDialog(challenge: Challenge) {
        val dialogBinding = DialogDeleteChallengeBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.tvChallengeDescription.text = challenge.description
        dialogBinding.btnSi.setOnClickListener {
            viewModel.deleteChallenge(challenge)
            dialog.dismiss()
        }
        dialogBinding.btnNo.setOnClickListener { dialog.dismiss() }

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}