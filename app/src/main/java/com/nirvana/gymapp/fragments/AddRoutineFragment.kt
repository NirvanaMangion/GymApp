package com.nirvana.gymapp.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import android.content.Context
import android.graphics.Typeface
import androidx.fragment.app.Fragment
import com.nirvana.gymapp.R
import com.nirvana.gymapp.database.UserDatabase
import com.nirvana.gymapp.activities.MainActivity

class AddRoutineFragment : Fragment() {

    private lateinit var routineContainer: LinearLayout
    private lateinit var userDb: UserDatabase
    private lateinit var routineNameInput: EditText
    private lateinit var saveButton: Button
    private lateinit var addExerciseButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_routine, container, false)

        routineContainer = view.findViewById(R.id.routineExerciseContainer)
        routineNameInput = view.findViewById(R.id.routineNameInput)
        saveButton = view.findViewById(R.id.saveRoutineButton)
        addExerciseButton = view.findViewById(R.id.addExerciseBtn)

        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("loggedInUser", "guest") ?: "guest"

        userDb = UserDatabase(requireContext())

        displayRoutineExercises(username)

        addExerciseButton.setOnClickListener {
            (activity as? MainActivity)?.loadFragment(
                AddExerciseFragment(),
                title = "Select Exercise",
                showUpArrow = true,
                showBottomNav = false
            )
        }

        saveButton.setOnClickListener {
            val name = routineNameInput.text.toString().trim()
            val exercises = userDb.getRoutineExercises(username)

            when {
                name.isEmpty() -> {
                    Toast.makeText(requireContext(), "Please enter a routine name.", Toast.LENGTH_SHORT).show()
                }
                exercises.isEmpty() -> {
                    Toast.makeText(requireContext(), "Please add at least one exercise.", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    userDb.saveRoutine(username, name)
                    Toast.makeText(requireContext(), "Routine '$name' saved!", Toast.LENGTH_SHORT).show()
                    userDb.clearRoutineExercises(username)
                    (activity as? MainActivity)?.onBackPressed()
                }
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val name = routineNameInput.text.toString().trim()
                val exercises = userDb.getRoutineExercises("username")

                if (name.isEmpty() || exercises.isEmpty()) {
                    userDb.clearRoutineExercises("username")
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun displayRoutineExercises(username: String) {
        val exercises = userDb.getRoutineExercises(username)
        routineContainer.removeAllViews()

        for ((name, category) in exercises) {
            val container = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(32, 16, 32, 16)
            }

            val textContainer = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val nameView = TextView(requireContext()).apply {
                text = name
                setTextColor(Color.WHITE)
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
            }

            val categoryView = TextView(requireContext()).apply {
                text = category
                setTextColor(Color.LTGRAY)
                textSize = 12f
            }

            textContainer.addView(nameView)
            textContainer.addView(categoryView)
            container.addView(textContainer)

            val divider = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1
                ).apply { topMargin = 8 }
                setBackgroundColor(Color.DKGRAY)
            }

            routineContainer.addView(container)
            routineContainer.addView(divider)
        }
    }
}
