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

        // Buttons
        val kgBtn = view.findViewById<Button>(R.id.kgBtn)
        val lbsBtn = view.findViewById<Button>(R.id.lbsBtn)
        val kmBtn = view.findViewById<Button>(R.id.kmBtn)
        val milesBtn = view.findViewById<Button>(R.id.milesBtn)
        val cmBtn = view.findViewById<Button>(R.id.cmBtn)
        val inBtn = view.findViewById<Button>(R.id.inBtn)

        var weight = "Kg"
        var distance = "Kilometres"
        var measurement = "cm"

        val yellow = Color.parseColor("#FFD600")
        val white = Color.parseColor("#FFFFFF")
        val black = Color.parseColor("#000000")

        fun select(btn1: Button, btn2: Button, selected: Button, onSelect: () -> Unit) {
            btn1.setBackgroundColor(if (btn1 == selected) yellow else white)
            btn1.setTextColor(black)

            btn2.setBackgroundColor(if (btn2 == selected) yellow else white)
            btn2.setTextColor(black)

            onSelect()
        }

        kgBtn.setOnClickListener {
            select(kgBtn, lbsBtn, kgBtn) { weight = "Kg" }
        }
        lbsBtn.setOnClickListener {
            select(kgBtn, lbsBtn, lbsBtn) { weight = "lbs" }
        }

        kmBtn.setOnClickListener {
            select(kmBtn, milesBtn, kmBtn) { distance = "Kilometres" }
        }
        milesBtn.setOnClickListener {
            select(kmBtn, milesBtn, milesBtn) { distance = "Miles" }
        }

        cmBtn.setOnClickListener {
            select(cmBtn, inBtn, cmBtn) { measurement = "cm" }
        }
        inBtn.setOnClickListener {
            select(cmBtn, inBtn, inBtn) { measurement = "in" }
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
