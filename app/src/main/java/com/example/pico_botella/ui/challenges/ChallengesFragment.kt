package com.example.pico_botella.ui.challenges

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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
            onEdit = { challenge -> showChallengeDialog(challenge) },
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
            showChallengeDialog()
        }
    }

    private fun showChallengeDialog(challenge: Challenge? = null) {
        if (challenge == null) {
            // CASO AGREGAR: Se utiliza el diseño personalizado solicitado (Criterios 1-4)
            val dialogBinding = DialogAddChallengeBinding.inflate(layoutInflater)
            
            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogBinding.root)
                .setCancelable(false)
                .create()

            dialogBinding.tvTitle.text = "Agregar reto"
            dialogBinding.etChallenge.setText("")

            // Lógica de validación del botón guardar para el diseño nuevo
            val updateSaveButtonState = {
                val text = dialogBinding.etChallenge.text.toString()
                val isValid = text.isNotBlank()
                dialogBinding.btnSave.isEnabled = isValid
                if (isValid) {
                    dialogBinding.btnSave.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.orange_pico)
                    )
                    dialogBinding.btnSave.alpha = 1.0f
                } else {
                    dialogBinding.btnSave.backgroundTintList = ColorStateList.valueOf(
                        Color.parseColor("#E0E0E0")
                    )
                    dialogBinding.btnSave.alpha = 0.5f
                }
            }

            updateSaveButtonState()
            dialogBinding.etChallenge.doAfterTextChanged {
                updateSaveButtonState()
            }

            dialogBinding.btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialogBinding.btnSave.setOnClickListener {
                val description = dialogBinding.etChallenge.text.toString()
                if (description.isNotBlank()) {
                    viewModel.addChallenge(description)
                    dialog.dismiss()
                }
            }

            dialog.show()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        } else {
            // CASO EDITAR: Se utiliza el diseño estándar anterior
            val editText = EditText(requireContext()).apply {
                hint = "Escribe el reto aquí..."
                setText(challenge.description)
                setPadding(48, 48, 48, 48)
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Editar Reto")
                .setView(editText)
                .setPositiveButton("Guardar") { _, _ ->
                    val description = editText.text.toString()
                    if (description.isNotBlank()) {
                        viewModel.updateChallenge(challenge.copy(description = description))
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun showDeleteConfirmationDialog(challenge: Challenge) {
        val dialogBinding = DialogDeleteChallengeBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialogBinding.tvChallengeDescription.text = challenge.description

        dialogBinding.btnNo.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSi.setOnClickListener {
            viewModel.deleteChallenge(challenge)
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}