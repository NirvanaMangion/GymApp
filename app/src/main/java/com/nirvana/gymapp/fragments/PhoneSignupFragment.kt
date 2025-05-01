package com.nirvana.gymapp.fragments

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
        userDb = UserDatabase(requireContext())

        val title = view.findViewById<TextView>(R.id.striveTitle)
        val usernameInput = view.findViewById<EditText>(R.id.usernameInput)
        val countryCodeInput = view.findViewById<EditText>(R.id.countryCodeInput)
        val phoneInput = view.findViewById<EditText>(R.id.phoneInput)
        val passwordInput = view.findViewById<EditText>(R.id.passwordInput)
        val termsCheckbox = view.findViewById<CheckBox>(R.id.termsCheckbox)
        val continueBtn = view.findViewById<Button>(R.id.continueBtn)

        title?.let {
            val styled = SpannableString("Strive")
            styled.setSpan(ForegroundColorSpan("#FFD600".toColorInt()), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            it.text = styled
        }

        continueBtn.setOnClickListener {
            val username = usernameInput.text?.toString()?.trim() ?: ""
            val code = countryCodeInput.text?.toString()?.trim() ?: ""
            val phone = phoneInput.text?.toString()?.trim() ?: ""
            val password = passwordInput.text?.toString()?.trim() ?: ""

            val fullPhone = "$code$phone"

            when {
                // Check if any field is empty
                username.isEmpty() || code.isEmpty() || phone.isEmpty() || password.isEmpty() ->
                    toast("Please fill in all fields.")

                // Check if country code is valid (starts with + and 1–4 digits)
                !code.matches(Regex("^\\+\\d{1,4}\$")) ->
                    toast("Enter a valid country code, like +356, +1, or +44.")

                // Check if phone number is valid (only digits, 7–15 digits)
                !phone.matches(Regex("^\\d{7,15}\$")) ->
                    toast("Enter a valid phone number (7 to 15 digits, numbers only).")

                // Check if password is strong enough
                password.length < 6 || !password.any { it.isDigit() } || !password.any { it.isLetter() } ->
                    toast("Password must be at least 6 characters with both letters and numbers.")

                // Check if terms and conditions are accepted
                !termsCheckbox.isChecked ->
                    toast("You must accept the terms.")

                // Check if username already exists
                userDb.checkUserExists(username) ->
                    toast("Username already exists.")

                else -> {
                    // Save user to database
                    userDb.addUser(username, null, fullPhone, password)
                    toast("Account created successfully!")

                    // Save username in SharedPreferences
                    val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    sharedPref.edit().putString("loggedInUser", username).apply()

                    // Move to UnitSelectionFragment
                    val fragment = UnitSelectionFragment()
                    fragment.arguments = Bundle().apply {
                        putString("username", username)
                    }

                    (activity as? MainActivity)?.loadFragment(
                        fragment,
                        title = "Choose Unit",
                        showUpArrow = false,
                        showBottomNav = false
                    )
                }
            }
        }

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

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
