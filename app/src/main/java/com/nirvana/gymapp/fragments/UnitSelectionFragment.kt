package com.nirvana.gymapp.fragments

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.nirvana.gymapp.R
import com.nirvana.gymapp.activities.MainActivity
import com.nirvana.gymapp.database.UserDatabase

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
        val defaultGrey = ContextCompat.getColor(requireContext(), R.color.grey_400)

        // Create styled text with smaller unit descriptions
        fun styledButtonText(main: String, unit: String): SpannableString {
            val full = "$main $unit"
            return SpannableString(full).apply {
                setSpan(
                    AbsoluteSizeSpan(12, true),
                    main.length + 1,
                    full.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        val europeanText = styledButtonText("European Units", "(Kg, cm, km)")
        val imperialText = styledButtonText("Imperial Units", "(lbs, in, miles)")

        europeanBtn.text = europeanText
        imperialBtn.text = imperialText

        // Initial colors
        europeanBtn.setBackgroundColor(defaultGrey)
        imperialBtn.setBackgroundColor(defaultGrey)
        europeanBtn.setTextColor(black)
        imperialBtn.setTextColor(black)

        // Selection logic
        fun select(
            selected: Button, selectedText: SpannableString,
            other: Button, otherText: SpannableString,
            onSelect: () -> Unit
        ) {
            selected.setBackgroundColor(yellow)
            selected.setTextColor(white)
            selected.text = selectedText

            other.setBackgroundColor(defaultGrey)
            other.setTextColor(black)
            other.text = otherText

            onSelect()
        }

        europeanBtn.setOnClickListener {
            select(europeanBtn, europeanText, imperialBtn, imperialText) {
                weight = "Kg"
                distance = "Kilometres"
                measurement = "cm"
            }
        }

        imperialBtn.setOnClickListener {
            select(imperialBtn, imperialText, europeanBtn, europeanText) {
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
