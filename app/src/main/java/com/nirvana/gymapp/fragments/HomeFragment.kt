package com.nirvana.gymapp.fragments

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.widget.*
import android.content.Context
import androidx.fragment.app.Fragment
import com.nirvana.gymapp.R
import com.nirvana.gymapp.database.UserDatabase
import com.nirvana.gymapp.activities.MainActivity

class HomeFragment : Fragment() {

    private lateinit var dropdownText: TextView
    private lateinit var routineListContainer: LinearLayout
    private lateinit var noRoutineText: TextView
    private lateinit var rootLayout: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        rootLayout = view.findViewById(R.id.homeRootLayout)
        rootLayout.visibility = View.INVISIBLE

        dropdownText = view.findViewById(R.id.myRoutineDropdown)
        routineListContainer = view.findViewById(R.id.routineListContainer)
        noRoutineText = view.findViewById(R.id.noRoutineText)
        val addRoutineBtn = view.findViewById<Button>(R.id.addRoutineBtn)

        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("loggedInUser", "guest") ?: "guest"

        addRoutineBtn.setOnClickListener {
            val userDb = UserDatabase(requireContext())
            userDb.clearRoutineExercises(username)

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

            val hasRoutines = UserDatabase(requireContext()).getAllSavedRoutines(username).isNotEmpty()
            noRoutineText.visibility = if (!isVisible && !hasRoutines) View.VISIBLE else View.GONE

            val arrowRes = if (isVisible) R.drawable.right_arrow else R.drawable.down_arrow
            dropdownText.setCompoundDrawablesWithIntrinsicBounds(0, 0, arrowRes, 0)
        }

        return view
    }

    override fun onResume() {
        super.onResume()

        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("loggedInUser", "guest") ?: "guest"

        displaySavedRoutines(username)

        val hasRoutines = UserDatabase(requireContext()).getAllSavedRoutines(username).isNotEmpty()
        routineListContainer.visibility = if (hasRoutines) View.VISIBLE else View.GONE
        noRoutineText.visibility = if (hasRoutines) View.GONE else View.VISIBLE
        dropdownText.setCompoundDrawablesWithIntrinsicBounds(
            0, 0,
            if (hasRoutines) R.drawable.down_arrow else R.drawable.right_arrow,
            0
        )

        rootLayout.alpha = 0f
        rootLayout.visibility = View.VISIBLE
        rootLayout.animate().alpha(1f).setDuration(150).start()
    }

    private fun displaySavedRoutines(username: String) {
        val db = UserDatabase(requireContext())
        val routines = db.getAllSavedRoutines(username)

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
                    setBackgroundColor(Color.parseColor("#E0E0E0"))
                    setPadding(32, 24, 32, 24)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        280
                    ).apply {
                        setMargins(16, 0, 16, 32)
                    }
                    gravity = Gravity.CENTER
                }

                val title = TextView(requireContext()).apply {
                    text = name
                    textSize = 22f
                    setTextColor(Color.parseColor("#212121"))
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    alpha = 0.85f
                    gravity = Gravity.CENTER
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
                routineListContainer.addView(card)
            }
        }

        dropdownText.text = "My Routine (${routines.size})"
    }
}
