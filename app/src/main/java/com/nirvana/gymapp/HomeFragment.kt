package com.nirvana.gymapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {

    private lateinit var dropdownText: TextView
    private lateinit var routineListContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize views
        dropdownText = view.findViewById(R.id.myRoutineDropdown)
        routineListContainer = view.findViewById(R.id.routineListContainer)
        val addRoutineBtn = view.findViewById<Button>(R.id.addRoutineBtn)

        // Toggle show/hide routines
        dropdownText.setOnClickListener {
            routineListContainer.visibility = if (routineListContainer.visibility == View.GONE)
                View.VISIBLE else View.GONE
        }

        // Launch AddRoutineFragment
        addRoutineBtn.setOnClickListener {
            (activity as MainActivity).loadFragment(
                AddRoutineFragment(),
                title = "Add Routine",
                showUpArrow = true,
                showBottomNav = false
            )
        }

        return view
    }
}
