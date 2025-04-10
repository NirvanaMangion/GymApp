package com.nirvana.gymapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment

class AddRoutineFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_routine, container, false)

        val routineNameInput = view.findViewById<EditText>(R.id.routineNameInput)
        val saveButton = view.findViewById<Button>(R.id.saveRoutineButton)
        val addExerciseButton = view.findViewById<Button>(R.id.addExerciseBtn)

        saveButton.setOnClickListener {
            val routineName = routineNameInput.text.toString().trim()
            if (routineName.isNotEmpty()) {
                Toast.makeText(requireContext(), "Routine '$routineName' saved!", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressed()
            } else {
                Toast.makeText(requireContext(), "Please enter a routine name.", Toast.LENGTH_SHORT).show()
            }
        }

        addExerciseButton.setOnClickListener {
            Toast.makeText(requireContext(), "Add Exercise clicked", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}
