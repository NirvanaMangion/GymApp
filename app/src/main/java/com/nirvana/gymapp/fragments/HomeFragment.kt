package com.nirvana.gymapp.fragments

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.nirvana.gymapp.R
import com.nirvana.gymapp.activities.MainActivity
import com.nirvana.gymapp.database.UserDatabase

class HomeFragment : Fragment() {

    private lateinit var dropdownText: TextView
    private lateinit var routineListContainer: LinearLayout
    private lateinit var noRoutineText: TextView
    private lateinit var rootLayout: View
    private lateinit var quoteText: TextView
    private lateinit var quoteBox: FrameLayout

    private var cachedRoutines: List<Pair<Int, String>> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Bind UI elements
        rootLayout = view.findViewById(R.id.homeRootLayout)
        dropdownText = view.findViewById(R.id.myRoutineDropdown)
        routineListContainer = view.findViewById(R.id.routineListContainer)
        noRoutineText = view.findViewById(R.id.noRoutineText)
        quoteText = view.findViewById(R.id.quoteText)
        quoteBox = view.findViewById(R.id.quoteBox)
        val addRoutineBtn = view.findViewById<Button>(R.id.addRoutineBtn)

        // Get current username
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("loggedInUser", "guest") ?: "guest"

        // Clear temp routine builder and navigate to AddRoutineFragment
        addRoutineBtn.setOnClickListener {
            val userDb = UserDatabase(requireContext())
            userDb.clearRoutineExercises(username)

            (activity as MainActivity).loadFragment(
                AddRoutineFragment(),
                title = "Add Routine",
                showUpArrow = true,
                showBottomNav = false  // Explicitly hide bottom nav
            )
        }

        return view
    }

    override fun onResume() {
        super.onResume()

        // Refresh quote and routines on return
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("loggedInUser", "guest") ?: "guest"

        val quotePrefs = requireContext().getSharedPreferences("AppSessionPrefs", Context.MODE_PRIVATE)
        quoteText.text = quotePrefs.getString("cachedQuote", "Stay motivated!")

        displaySavedRoutines(username) {}
    }

    // Load and display user's saved routines
    private fun displaySavedRoutines(username: String, onComplete: () -> Unit) {
        val db = UserDatabase(requireContext())
        val routines = db.getAllSavedRoutines(username)
        cachedRoutines = routines

        routineListContainer.removeAllViews()

        if (routines.isEmpty()) {
            // Show "no routines" message if none
            routineListContainer.visibility = View.GONE
            noRoutineText.visibility = View.VISIBLE
        } else {
            // Show routine cards
            routineListContainer.visibility = View.VISIBLE
            noRoutineText.visibility = View.GONE

            for ((id, name) in routines) {
                // Create card container
                val card = FrameLayout(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        260
                    ).apply {
                        setMargins(0, 0, 0, 40)
                    }
                    background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_grey_background)
                    isClickable = true
                    isFocusable = true
                    setOnClickListener {
                        // Navigate to RoutineDetailFragment on card click
                        val detailFragment = RoutineDetailFragment.newInstance(id, name)
                        (activity as? MainActivity)?.loadFragment(
                            fragment = detailFragment,
                            title = name,
                            showUpArrow = true,
                            showBottomNav = false  // Explicitly hide bottom nav
                        )
                    }
                }

                // Inner layout for routine title
                val container = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(32, 24, 32, 24)
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    gravity = Gravity.CENTER
                }

                // Routine title text
                val title = TextView(requireContext()).apply {
                    text = name
                    textSize = 22f
                    setTextColor(Color.parseColor("#F5F5F5"))
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                }

                // Delete (bin) icon
                val binIcon = ImageView(requireContext()).apply {
                    setImageResource(R.drawable.binicon)
                    layoutParams = FrameLayout.LayoutParams(60, 60, Gravity.END or Gravity.BOTTOM).apply {
                        setMargins(0, 0, 24, 24)
                    }
                    setOnClickListener {
                        // Show confirmation dialog before deletion
                        AlertDialog.Builder(requireContext())
                            .setTitle("Delete Routine")
                            .setMessage("Are you sure you want to delete this routine?")
                            .setPositiveButton("Yes") { _, _ ->
                                db.deleteRoutineById(id)
                                displaySavedRoutines(username) {} // Refresh list
                            }
                            .setNegativeButton("No", null)
                            .show()
                    }
                }

                // Add views to layout
                container.addView(title)
                card.addView(container)
                card.addView(binIcon)
                routineListContainer.addView(card)
            }
        }

        // Update dropdown header with routine count
        dropdownText.text = "My Routine (${routines.size})"

        onComplete()
    }
}
