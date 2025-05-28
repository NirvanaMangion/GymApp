package com.nirvana.gymapp.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.SpannableString
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.content.Context
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import com.nirvana.gymapp.R
import com.nirvana.gymapp.database.UserDatabase
import com.nirvana.gymapp.activities.MainActivity

class PhoneSignupFragment : Fragment() {

    private lateinit var userDb: UserDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_phone_signup, container, false)
        userDb = UserDatabase(requireContext()) // Initialize the local SQLite database handler

        // UI element references
        val title = view.findViewById<TextView>(R.id.striveTitle)
        val usernameInput = view.findViewById<EditText>(R.id.usernameInput)
        val countryCodeInput = view.findViewById<EditText>(R.id.countryCodeInput)
        val phoneInput = view.findViewById<EditText>(R.id.phoneInput)
        val passwordInput = view.findViewById<EditText>(R.id.passwordInput)
        val termsCheckbox = view.findViewById<CheckBox>(R.id.termsCheckbox)
        val continueBtn = view.findViewById<Button>(R.id.continueBtn)

        // Pre-fill country code input with '+' symbol
        countryCodeInput.setText("+")
        countryCodeInput.setSelection(countryCodeInput.text.length)

        // Highlight the first letter of "Strive" title
        title?.let {
            val styled = SpannableString("Strive")
            styled.setSpan(ForegroundColorSpan("#FFD600".toColorInt()), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            it.text = styled
        }

        // Handle signup logic
        continueBtn.setOnClickListener {
            val username = usernameInput.text?.toString()?.trim() ?: ""
            val code = countryCodeInput.text?.toString()?.trim() ?: ""
            val phone = phoneInput.text?.toString()?.trim() ?: ""
            val password = passwordInput.text?.toString()?.trim() ?: ""
            val fullPhone = "$code$phone"

            val usernamePattern = Regex("^[a-z]+")
            val passwordPattern = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{10,}$")

            val passwordRequirementsMsg = """
                Password Requirements:
                • At least 10 characters
                • Include uppercase and lowercase letters
                • Include a number
                • Include a special character
            """.trimIndent()

            // Validation checks
            when {
                username.isEmpty() || code.isEmpty() || phone.isEmpty() || password.isEmpty() ->
                    toast("Please fill in all fields.")

                !usernamePattern.matches(username) ->
                    toast("Username must contain only lowercase letters with no spaces or symbols.")

                !code.matches(Regex("^\\+\\d{1,4}$")) ->
                    toast("Enter a valid country code, like +356, +1, or +44.")

                !phone.matches(Regex("^\\d{7,15}$")) ->
                    toast("Enter a valid phone number (7 to 15 digits, numbers only).")

                !passwordPattern.matches(password) -> {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Invalid Password")
                        .setMessage(passwordRequirementsMsg)
                        .setPositiveButton("OK", null)
                        .show()
                }

                !termsCheckbox.isChecked ->
                    toast("You must accept the terms.")

                userDb.checkUserExists(username) ->
                    toast("Username already exists.")

                else -> {
                    // Add user to database with phone credentials
                    userDb.addUser(username, null, fullPhone, password)
                    toast("Account created successfully!")

                    // Store session info
                    val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    sharedPref.edit().putString("loggedInUser", username).apply()

                    val quotePrefs = requireContext().getSharedPreferences("AppSessionPrefs", Context.MODE_PRIVATE)
                    quotePrefs.edit().remove("cachedQuote").apply()

                    // Proceed to home
                    (activity as? MainActivity)?.fetchAndCacheQuote {
                        activity?.runOnUiThread {
                            (activity as? MainActivity)?.preloadAndLoadHome()
                        }
                    }
                }
            }
        }

        // Dismiss keyboard on outside tap
        view.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                view.clearFocus()
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
                v.performClick()
            }
            false
        }

        return view
    }

    // Utility method to show a toast message
    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
