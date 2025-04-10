package com.nirvana.gymapp

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
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


class EmailSignupFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_email_signup, container, false)

        // Your styling code remains unchanged
        val titleView = view.findViewById<TextView>(R.id.striveTitle)
        val styledTitle = SpannableString("Strive")
        styledTitle.setSpan(
            ForegroundColorSpan("#FFD600".toColorInt()),
            0, 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        titleView.text = styledTitle

        // Continue button navigates to UnitSelectionFragment
        val continueBtn = view.findViewById<Button>(R.id.continueBtn)
        continueBtn.setOnClickListener {
            (activity as MainActivity).loadFragment(
                UnitSelectionFragment(),
                title = "Choose Unit",
                showUpArrow = true,
                showBottomNav = false
            )
        }

        return view
    }
}
