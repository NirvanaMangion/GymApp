package com.nirvana.gymapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class UnitSelectionFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_unit_selection, container, false)

        // Set up the Continue button so that it navigates to the HomeFragment
        val continueBtn = view.findViewById<Button>(R.id.continueBtn)
        continueBtn.setOnClickListener {
            // Call loadFragment() in MainActivity to navigate to HomeFragment
            (activity as? MainActivity)?.loadFragment(
                fragment = HomeFragment(),
                title = "Home",
                showUpArrow = false,
                showBottomNav = true
            )
        }

        return view
    }
}
