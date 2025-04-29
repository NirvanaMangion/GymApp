package com.nirvana.gymapp.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.nirvana.gymapp.R
import com.nirvana.gymapp.database.UserDatabase
import com.nirvana.gymapp.activities.MainActivity

class UnitSelectionFragment : Fragment() {

    private var currentUsername: String? = null
    private lateinit var userDb: UserDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_unit_selection, container, false)
        userDb = UserDatabase(requireContext())
        currentUsername = arguments?.getString("username")

        if (currentUsername == null) {
            Toast.makeText(requireContext(), "Missing user session.", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressed()
            return view
        }

        val europeanBtn = view.findViewById<Button>(R.id.europeanBtn)
        val imperialBtn = view.findViewById<Button>(R.id.imperialBtn)

        var weight = "Kg"
        var distance = "Kilometres"
        var measurement = "cm"

        val yellow = Color.parseColor("#FFD600")
        val white = Color.parseColor("#FFFFFF")
        val black = Color.parseColor("#000000")

        fun select(selected: Button, other: Button, onSelect: () -> Unit) {
            selected.setBackgroundColor(yellow)
            selected.setTextColor(white)

            other.setBackgroundColor(white)
            other.setTextColor(black)

            onSelect()
        }

        europeanBtn.setOnClickListener {
            select(europeanBtn, imperialBtn) {
                weight = "Kg"
                distance = "Kilometres"
                measurement = "cm"
            }
        }

        imperialBtn.setOnClickListener {
            select(imperialBtn, europeanBtn) {
                weight = "lbs"
                distance = "Miles"
                measurement = "in"
            }
        }

        val continueBtn = view.findViewById<Button>(R.id.continueBtn)
        continueBtn.setOnClickListener {
            val success = userDb.setUserUnits(currentUsername!!, weight, distance, measurement)
            if (success) {
                (activity as? MainActivity)?.loadFragment(
                    fragment = HomeFragment(),
                    title = "Home",
                    showUpArrow = false,
                    showBottomNav = true
                )
            } else {
                Toast.makeText(requireContext(), "Failed to save units", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}
