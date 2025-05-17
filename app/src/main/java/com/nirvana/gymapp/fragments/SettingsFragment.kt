package com.nirvana.gymapp.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.nirvana.gymapp.R
import com.nirvana.gymapp.activities.FrontpageActivity
import com.nirvana.gymapp.database.UserDatabase

class SettingsFragment : Fragment() {

    private lateinit var db: UserDatabase
    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        db = UserDatabase(requireContext())
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        userId = sharedPref.getString("loggedInUser", "user") ?: "user"
        val (email, phone) = db.getUserDetails(userId)

        val accountInfo = view.findViewById<TextView>(R.id.accountEmailOrPhone)
        val accountUsername = view.findViewById<TextView>(R.id.accountUsername)
        val currentPassInput = view.findViewById<EditText>(R.id.currentPassword)
        val newPassInput = view.findViewById<EditText>(R.id.newPassword)
        val confirmPassInput = view.findViewById<EditText>(R.id.confirmPassword)
        val changePassBtn = view.findViewById<Button>(R.id.btnChangePassword)
        val signOutBtn = view.findViewById<Button>(R.id.btnSignOut)
        val deleteBtn = view.findViewById<Button>(R.id.btnDeleteAccount)

        fun boldLabel(label: String, value: String): SpannableString {
            val full = "$label $value"
            val spannable = SpannableString(full)
            spannable.setSpan(
                StyleSpan(android.graphics.Typeface.BOLD),
                0,
                label.length + 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return spannable
        }

        accountUsername.text = boldLabel("Username:", userId)
        accountInfo.text = when {
            !email.isNullOrEmpty() -> boldLabel("Email:", email)
            !phone.isNullOrEmpty() -> boldLabel("Phone:", phone)
            else -> SpannableString("No contact set")
        }

        // Change password logic
        changePassBtn.setOnClickListener {
            val currentPass = currentPassInput.text.toString()
            val newPass = newPassInput.text.toString()
            val confirmPass = confirmPassInput.text.toString()

            if (currentPass.isBlank() || newPass.isBlank() || confirmPass.isBlank()) {
                Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val savedPass = db.getPasswordForUser(userId)

            if (currentPass != savedPass) {
                Toast.makeText(requireContext(), "Incorrect current password.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass != confirmPass) {
                Toast.makeText(requireContext(), "New passwords do not match.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val passwordPattern = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{10,}$")
            if (!passwordPattern.matches(newPass)) {
                Toast.makeText(
                    requireContext(),
                    "Password must be at least 10 characters and include uppercase, lowercase, number, and special character.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            val success = db.updateUserPassword(userId, newPass)
            if (success) {
                Toast.makeText(requireContext(), "Password updated successfully.", Toast.LENGTH_SHORT).show()
                currentPassInput.text.clear()
                newPassInput.text.clear()
                confirmPassInput.text.clear()
            } else {
                Toast.makeText(requireContext(), "Error updating password. Try again.", Toast.LENGTH_SHORT).show()
            }
        }

        // Sign out logic with yellow buttons
        signOutBtn.setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Yes") { _, _ ->
                    val prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    prefs.edit().remove("loggedInUser").apply()
                    val intent = Intent(requireContext(), FrontpageActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("No", null)
                .create()

            dialog.show()
            dialog.setButtonColors()
        }

        // Delete account logic with full wipe and yellow buttons
        deleteBtn.setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account and all its data?")
                .setPositiveButton("Yes") { _, _ ->
                    val success = db.deleteUserCompletely(userId)
                    if (success) {
                        val prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                        prefs.edit().remove("loggedInUser").apply()
                        Toast.makeText(requireContext(), "Account deleted successfully.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(requireContext(), FrontpageActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {
                        Toast.makeText(requireContext(), "Failed to delete account.", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("No", null)
                .create()

            dialog.show()
            dialog.setButtonColors()
        }

        return view
    }

    // Helper to style dialog buttons yellow
    private fun AlertDialog.setButtonColors() {
        val yellow = Color.parseColor("#FFD600")
        getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(yellow)
        getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(yellow)
    }
}
