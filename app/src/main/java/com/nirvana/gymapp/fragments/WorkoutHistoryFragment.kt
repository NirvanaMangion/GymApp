package com.nirvana.gymapp.fragments

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.nirvana.gymapp.R
import com.nirvana.gymapp.database.UserDatabase
import java.text.SimpleDateFormat
import java.util.*

class WorkoutHistoryFragment : Fragment() {

    private lateinit var userDb: UserDatabase
    private lateinit var userId: String
    private lateinit var historyContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_workout_history, container, false)

        userDb = UserDatabase(requireContext())
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        userId = sharedPref.getString("loggedInUser", "guest") ?: "guest"

        // Toolbar setup
        requireActivity().findViewById<TextView>(R.id.custom_title)?.text = "Workout History"
        requireActivity().findViewById<View>(R.id.custom_back)?.isVisible = true
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.GONE

        historyContainer = view.findViewById(R.id.historyContainer)
        displayWorkoutHistory()

        return view
    }

    private fun displayWorkoutHistory() {
        val workouts = userDb.getWorkoutHistoryLogs(userId)
        historyContainer.removeAllViews()

        if (workouts.isEmpty()) {
            val emptyText = TextView(requireContext()).apply {
                text = "No workout history available."
                setTextColor(Color.LTGRAY)
                textSize = 16f
                setPadding(16, 32, 16, 32)
                gravity = Gravity.CENTER
            }
            historyContainer.addView(emptyText)
            return
        }

        for ((routineName, start, end) in workouts) {
            val durationMins = ((end - start) / 1000 / 60).toInt()
            val volume = durationMins * 50
            val reps = durationMins * 2

            val item = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(Color.parseColor("#1A1A1A"))
                setPadding(32, 32, 32, 32)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 32)
                }
            }

            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 0, 0, 8)
            }

            val title = TextView(requireContext()).apply {
                text = routineName
                textSize = 18f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val dateText = TextView(requireContext()).apply {
                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                text = formatter.format(Date(start))
                textSize = 13f
                setTextColor(Color.LTGRAY)
            }

            row.addView(title)
            row.addView(dateText)
            item.addView(row)

            val durationText = TextView(requireContext()).apply {
                text = "Duration: $durationMins min"
                setTextColor(Color.WHITE)
                textSize = 14f
            }

            val volumeText = TextView(requireContext()).apply {
                text = "Volume: $volume"
                setTextColor(Color.WHITE)
                textSize = 14f
            }

            val repsText = TextView(requireContext()).apply {
                text = "Reps: $reps"
                setTextColor(Color.WHITE)
                textSize = 14f
            }

            item.addView(durationText)
            item.addView(volumeText)
            item.addView(repsText)
            historyContainer.addView(item)
        }
    }
}
