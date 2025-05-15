package com.nirvana.gymapp.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.nirvana.gymapp.R
import com.nirvana.gymapp.database.UserDatabase

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val db = UserDatabase(requireContext())
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("loggedInUser", "user") ?: "user"
        val (email, phone) = db.getUserDetails(username)

        val accountInfo = view.findViewById<TextView>(R.id.accountEmailOrPhone)
        val accountUsername = view.findViewById<TextView>(R.id.accountUsername)
        val profileImage = view.findViewById<ImageView>(R.id.profileImage)

        // Load saved profile image if exists
        val savedUri = db.getProfileImage(username)
        if (!savedUri.isNullOrEmpty()) {
            profileImage.setImageURI(Uri.parse(savedUri))
        } else {
            profileImage.setImageResource(R.drawable.profileicon)
        }

        // Make labels bold, but not the value
        fun boldLabel(label: String, value: String): SpannableString {
            val full = "$label $value"
            val spannable = SpannableString(full)
            spannable.setSpan(
                StyleSpan(android.graphics.Typeface.BOLD),
                0,
                label.length + 1, // include space
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return spannable
        }

        accountUsername.text = boldLabel("Username:", username)

        if (!email.isNullOrEmpty()) {
            accountInfo.text = boldLabel("Email:", email)
        } else if (!phone.isNullOrEmpty()) {
            accountInfo.text = boldLabel("Phone:", phone)
        } else {
            accountInfo.text = "No contact set"
        }

        return view
    }
}
