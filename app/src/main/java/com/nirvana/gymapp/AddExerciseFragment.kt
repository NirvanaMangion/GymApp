package com.nirvana.gymapp

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class AddExerciseFragment : Fragment() {

    private lateinit var exerciseListLayout: LinearLayout
    private lateinit var categorySpinner: Spinner
    private lateinit var addButton: Button
    private lateinit var userDb: UserDatabase
    private var allExercises: List<Pair<String, String>> = emptyList()
    private val selectedExercises = mutableSetOf<Pair<String, String>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_exercise, container, false)

        exerciseListLayout = view.findViewById(R.id.exerciseList)
        categorySpinner = view.findViewById(R.id.categorySpinner)
        addButton = view.findViewById(R.id.addExerciseButton)
        addButton.visibility = View.GONE

        userDb = UserDatabase(requireContext())
        allExercises = ExerciseDatabase(requireContext()).getAllExercises()

        setupSpinner()
        displayExercises(allExercises)

        addButton.setOnClickListener {
            for ((name, category) in selectedExercises) {
                userDb.addExerciseToRoutine(name, category)
            }
            Toast.makeText(requireContext(), "Exercises added!", Toast.LENGTH_SHORT).show()
            selectedExercises.clear()
            addButton.visibility = View.GONE
            (activity as? MainActivity)?.onBackPressed()
        }

        return view
    }

    private fun setupSpinner() {
        val categories = allExercises.map { it.second }.toSet().toList().sorted()
        val spinnerItems = listOf("All Categories") + categories

        val adapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            spinnerItems
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(Color.WHITE)
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(Color.WHITE)
                view.setBackgroundColor(Color.parseColor("#222222"))
                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selected = spinnerItems[position]
                val filtered = if (selected == "All Categories") allExercises else allExercises.filter { it.second == selected }
                displayExercises(filtered)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun displayExercises(exercises: List<Pair<String, String>>) {
        exerciseListLayout.removeAllViews()
        val grouped = exercises.groupBy { it.first[0].uppercaseChar() }

        for ((letter, group) in grouped.toSortedMap()) {
            val header = TextView(requireContext()).apply {
                text = letter.toString()
                textSize = 26f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.parseColor("#FFD600"))
                setPadding(16, 40, 16, 16)
            }
            exerciseListLayout.addView(header)

            for ((name, category) in group) {
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

                val checkmark = TextView(requireContext()).apply {
                    text = "✓"
                    setTextColor(Color.parseColor("#FFD600"))
                    textSize = 20f
                    visibility = if ((name to category) in selectedExercises) View.VISIBLE else View.GONE
                }

                textContainer.addView(nameView)
                textContainer.addView(categoryView)
                container.addView(textContainer)
                container.addView(checkmark)

                container.setOnClickListener {
                    val pair = name to category
                    if (selectedExercises.contains(pair)) {
                        selectedExercises.remove(pair)
                        checkmark.visibility = View.GONE
                    } else {
                        selectedExercises.add(pair)
                        checkmark.visibility = View.VISIBLE
                    }
                    addButton.visibility = if (selectedExercises.isNotEmpty()) View.VISIBLE else View.GONE
                }

                val divider = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1
                    ).apply { topMargin = 8 }
                    setBackgroundColor(Color.DKGRAY)
                }

                exerciseListLayout.addView(container)
                exerciseListLayout.addView(divider)
            }
        }
    }
}
