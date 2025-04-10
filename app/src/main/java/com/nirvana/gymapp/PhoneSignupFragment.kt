package com.nirvana.gymapp

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment

class PhoneSignupFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment. Make sure this layout does not include its own toolbar.
        val view = inflater.inflate(R.layout.fragment_phone_signup, container, false)

        // OPTIONAL: If there's a "Strive" TextView (for visual branding) in the layout,
        // style the text as needed. (If you don't want it, you can remove this section.)
        val striveTitle = view.findViewById<TextView>(R.id.striveTitle)
        striveTitle?.let {
            val styled = SpannableString("Strive")
            // Change the color of the first character to #FFD600
            styled.setSpan(ForegroundColorSpan("#FFD600".toColorInt()),
                0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            it.text = styled
        }

        // Set up the Continue button to navigate to the UnitSelectionFragment.
        val continueBtn = view.findViewById<Button>(R.id.continueBtn)
        continueBtn.setOnClickListener {
            // Calling our public loadFragment method in MainActivity:
            (activity as MainActivity).loadFragment(
                fragment = UnitSelectionFragment(),
                title = "Choose Unit",
                showUpArrow = true,
                showBottomNav = false
            )
        }

        return view
    }
}
