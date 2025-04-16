package com.nirvana.gymapp

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {

    private lateinit var dropdownText: TextView
    private lateinit var routineListContainer: LinearLayout
    private lateinit var noRoutineText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        dropdownText = view.findViewById(R.id.myRoutineDropdown)
        routineListContainer = view.findViewById(R.id.routineListContainer)
        noRoutineText = view.findViewById(R.id.noRoutineText)
        val addRoutineBtn = view.findViewById<Button>(R.id.addRoutineBtn)

        addRoutineBtn.setOnClickListener {
            (activity as MainActivity).loadFragment(
                AddRoutineFragment(),
                title = "Add Routine",
                showUpArrow = true,
                showBottomNav = false
            )
        }

        dropdownText.setOnClickListener {
            val isVisible = routineListContainer.visibility == View.VISIBLE
            routineListContainer.visibility = if (isVisible) View.GONE else View.VISIBLE
            noRoutineText.visibility = if (!isVisible && UserDatabase(requireContext()).getAllSavedRoutines().isEmpty()) View.VISIBLE else View.GONE

            val arrowRes = if (isVisible) R.drawable.right_arrow else R.drawable.down_arrow
            dropdownText.setCompoundDrawablesWithIntrinsicBounds(0, 0, arrowRes, 0)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        displaySavedRoutines()

        val hasRoutines = UserDatabase(requireContext()).getAllSavedRoutines().isNotEmpty()
        routineListContainer.visibility = if (hasRoutines) View.VISIBLE else View.GONE
        noRoutineText.visibility = if (hasRoutines) View.GONE else View.VISIBLE
        dropdownText.setCompoundDrawablesWithIntrinsicBounds(
            0, 0,
            if (hasRoutines) R.drawable.down_arrow else R.drawable.right_arrow,
            0
        )
    }

    private fun displaySavedRoutines() {
        val db = UserDatabase(requireContext())
        val routines = db.getAllSavedRoutines()

        routineListContainer.removeAllViews()

        if (routines.isEmpty()) {
            noRoutineText.visibility = View.VISIBLE
            routineListContainer.visibility = View.GONE
        } else {
            noRoutineText.visibility = View.GONE
            routineListContainer.visibility = View.VISIBLE

            for ((id, name) in routines) {
                val card = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    setBackgroundColor(Color.parseColor("#2A2A2A"))
                    setPadding(32, 24, 32, 24)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        280 // Enough space for title and 2 exercises
                    ).apply {
                        setMargins(0, 0, 0, 32)
                    }
                }

                val title = TextView(requireContext()).apply {
                    text = name
                    textSize = 18f
                    setTextColor(Color.WHITE)
                    setTypeface(null, Typeface.BOLD)
                }

                val exercisePreview = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(0, 12, 0, 0)
                }

                val exercises = db.getExercisesForRoutine(id).take(2)

                for (i in 0 until 2) {
                    val previewText = TextView(requireContext()).apply {
                        textSize = 14f
                        setTextColor(Color.LTGRAY)
                        text = if (i < exercises.size) "• ${exercises[i].first} (${exercises[i].second})" else " "
                    }
                    exercisePreview.addView(previewText)
                }

                card.setOnClickListener {
                    val detailFragment = RoutineDetailFragment.newInstance(id, name)
                    (activity as? MainActivity)?.loadFragment(
                        fragment = detailFragment,
                        title = name,
                        showUpArrow = true,
                        showBottomNav = false
                    )
                }

                card.addView(title)
                card.addView(exercisePreview)
                routineListContainer.addView(card)
            }
        }

        dropdownText.text = "My Routine (${routines.size})"
    }
}
