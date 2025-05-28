package com.nirvana.gymapp.fragments

import android.content.Context // for SharedPreferences access
import android.graphics.Color // for setting colors programmatically
import android.graphics.Typeface // for bold text style
import android.os.Bundle // for fragment lifecycle
import android.view.* // for View, LayoutInflater, ViewGroup, Gravity
import android.widget.LinearLayout // for dynamic layout container
import android.widget.TextView // for displaying text
import androidx.core.view.isVisible // extension to toggle visibility
import androidx.fragment.app.Fragment // base class for fragments
import com.nirvana.gymapp.R // project resources
import com.nirvana.gymapp.database.UserDatabase // custom DB helper
import java.text.SimpleDateFormat // for date formatting
import java.util.* // for Date and Locale

class WorkoutHistoryFragment : Fragment() {

    private lateinit var userDb: UserDatabase // database instance
    private lateinit var userId: String       // current user identifier
    private lateinit var historyContainer: LinearLayout // container for history items

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_workout_history, container, false)

        // Initialize database helper
        userDb = UserDatabase(requireContext())
        // Retrieve logged-in user ID from shared preferences
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        userId = sharedPref.getString("loggedInUser", "guest") ?: "guest"

        // Set custom toolbar title
        requireActivity().findViewById<TextView>(R.id.custom_title)?.text = "Workout History"
        requireActivity().findViewById<View>(R.id.custom_back)?.isVisible = true
        requireActivity().findViewById<View>(R.id.bottomNav)?.visibility = View.GONE

        // Find the container layout for history entries
        historyContainer = view.findViewById(R.id.historyContainer)
        displayWorkoutHistory()

        return view
    }

    private fun displayWorkoutHistory() {
        // Fetch workout logs for this user
        val workouts = userDb.getWorkoutHistoryLogs(userId)
        // Clear any existing views before adding new ones
        historyContainer.removeAllViews()

        // If no history exists, show a placeholder message
        if (workouts.isEmpty()) {
            val emptyText = TextView(requireContext()).apply {
                text = "No workout history available."
                setTextColor(Color.LTGRAY) // light gray text
                textSize = 16f
                setPadding(16, 32, 16, 32) // padding around the message
                gravity = Gravity.CENTER // center text horizontally
            }
            historyContainer.addView(emptyText) // add placeholder to container
            return // exit early
        }

        // Iterate over each workout entry
        for ((routineName, start, end) in workouts) {
            // Calculate duration in minutes
            val durationMins = ((end - start) / 1000 / 60).toInt()
            // Estimate volume and reps based on duration
            val volume = durationMins * 50
            val reps = durationMins * 2

            // Create a vertical layout for each entry
            val item = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(Color.parseColor("#1A1A1A")) // dark background
                setPadding(32, 32, 32, 32) // inner padding
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 32) // bottom margin between entries
                }
            }

            // Create a horizontal row for title and date
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                gravity = Gravity.CENTER_VERTICAL // align children vertically center
                setPadding(0, 0, 0, 8) // bottom padding between row and details
            }

            // Title text view showing routine name
            val title = TextView(requireContext()).apply {
                text = routineName
                textSize = 18f
                setTypeface(null, Typeface.BOLD) // bold font
                setTextColor(Color.WHITE) // white text
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            // Date text view showing formatted start time
            val dateText = TextView(requireContext()).apply {
                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                text = formatter.format(Date(start)) // format timestamp
                textSize = 13f
                setTextColor(Color.LTGRAY) // light gray text
            }

            // Add title and date to the row, then row to the item
            row.addView(title)
            row.addView(dateText)
            item.addView(row)

            // Detail text views for duration, volume, and reps
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

            // Add the detail views to the item layout
            item.addView(durationText)
            item.addView(volumeText)
            item.addView(repsText)
            // Finally, add the fully constructed item to the container
            historyContainer.addView(item)
        }
    }
}
