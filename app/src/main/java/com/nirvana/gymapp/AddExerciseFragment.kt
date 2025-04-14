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
    private lateinit var db: ExerciseDatabase
    private var allExercises: List<Pair<String, String>> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_exercise, container, false)

        exerciseListLayout = view.findViewById(R.id.exerciseList)
        categorySpinner = view.findViewById(R.id.categorySpinner)

        db = ExerciseDatabase(requireContext())
        allExercises = db.getAllExercises()

        setupSpinner()
        displayExercises(allExercises)

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
                val filtered = if (selected == "All Categories") {
                    allExercises
                } else {
                    allExercises.filter { it.second == selected }
                }
                displayExercises(filtered)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun displayExercises(exercises: List<Pair<String, String>>) {
        exerciseListLayout.removeAllViews()

        val grouped = exercises.groupBy { it.first[0].uppercaseChar() }

        for ((letter, group) in grouped.toSortedMap()) {
            val sectionHeader = TextView(requireContext()).apply {
                text = letter.toString()
                textSize = 22f
                setTextColor(Color.parseColor("#FFD600"))  // Yellow
                setPadding(16, 40, 16, 16) // Bigger top spacing
            }
            exerciseListLayout.addView(sectionHeader)

            for ((name, category) in group) {
                val container = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(24, 16, 24, 16)
                }

                val nameView = TextView(requireContext()).apply {
                    text = name
                    setTextColor(Color.WHITE)
                    textSize = 15f
                    setTypeface(null, Typeface.BOLD)
                }

                val categoryView = TextView(requireContext()).apply {
                    text = category
                    setTextColor(Color.WHITE)  // Now fully visible on dark background
                    textSize = 12f
                }

                val divider = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                    ).apply { topMargin = 8 }
                    setBackgroundColor(Color.DKGRAY)
                }

                container.addView(nameView)
                container.addView(categoryView)
                exerciseListLayout.addView(container)
                exerciseListLayout.addView(divider)
            }
        }
    }
}
