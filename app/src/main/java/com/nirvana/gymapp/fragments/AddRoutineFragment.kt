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

        // Bind UI views
        routineContainer = view.findViewById(R.id.routineExerciseContainer)
        routineNameInput = view.findViewById(R.id.routineNameInput)
        saveButton = view.findViewById(R.id.saveRoutineButton)
        addExerciseButton = view.findViewById(R.id.addExerciseBtn)

        // Get logged-in username
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("loggedInUser", "guest") ?: "guest"

        userDb = UserDatabase(requireContext())

        // Show current exercises in the routine builder
        displayRoutineExercises(username)

        // Go to AddExerciseFragment to add exercises
        addExerciseButton.setOnClickListener {
            (activity as? MainActivity)?.loadFragment(
                AddExerciseFragment(),
                title = "Select Exercise",
                showUpArrow = true,
                showBottomNav = false
            )
        }

        // Save routine to DB
        saveButton.setOnClickListener {
            val name = routineNameInput.text.toString().trim()
            val exercises = userDb.getRoutineExercises(username)

            when {
                name.isEmpty() -> {
                    // Warn if name is empty
                    Toast.makeText(requireContext(), "Please enter a routine name.", Toast.LENGTH_SHORT).show()
                }
                exercises.isEmpty() -> {
                    // Warn if no exercises added
                    Toast.makeText(requireContext(), "Please add at least one exercise.", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    // Save routine, clear builder, go back
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

        // Handle physical back press
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val name = routineNameInput.text.toString().trim()
                val exercises = userDb.getRoutineExercises("username") // Note: Hardcoded "username" — should be fixed

                if (name.isEmpty() || exercises.isEmpty()) {
                    // Clear routine builder if abandoned
                    userDb.clearRoutineExercises("username")
                }

                // Proceed with default back
                isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        })
    }

    // Display the list of exercises currently in the routine builder
    private fun displayRoutineExercises(username: String) {
        val exercises = userDb.getRoutineExercises(username)
        routineContainer.removeAllViews()

        for ((name, category) in exercises) {
            // Container for each exercise row
            val container = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(32, 16, 32, 16)
            }

            // Holds name and category stacked vertically
            val textContainer = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            // Exercise name (bold)
            val nameView = TextView(requireContext()).apply {
                text = name
                setTextColor(Color.WHITE)
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
            }

            // Exercise category (smaller, gray)
            val categoryView = TextView(requireContext()).apply {
                text = category
                setTextColor(Color.LTGRAY)
                textSize = 12f
            }

            // Add views to layout
            textContainer.addView(nameView)
            textContainer.addView(categoryView)
            container.addView(textContainer)

            // Divider line below each entry
            val divider = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1
                ).apply { topMargin = 8 }
                setBackgroundColor(Color.DKGRAY)
            }

            // Add everything to the list container
            routineContainer.addView(container)
            routineContainer.addView(divider)
        }
    }
}
