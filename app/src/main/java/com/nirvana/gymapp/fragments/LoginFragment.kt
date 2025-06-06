package com.nirvana.gymapp.fragments

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
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

class LoginFragment : Fragment() {

    private lateinit var userDb: UserDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        // Initialize database
        userDb = UserDatabase(requireContext())

        // Bind UI views
        val title = view.findViewById<TextView>(R.id.striveTitle)              // Title text ("Strive")
        val usernameInput = view.findViewById<EditText>(R.id.usernameInput)    // Input field for username
        val passwordInput = view.findViewById<EditText>(R.id.passwordInput)    // Input field for password
        val loginButton = view.findViewById<Button>(R.id.loginBtn)             // Login button

        // Style first letter of title
        title?.let {
            val styled = SpannableString("Strive")
            styled.setSpan(
                ForegroundColorSpan("#FFD600".toColorInt()), // Yellow color
                0, 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            it.text = styled
        }

        // Handle login button click
        loginButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()  // Get username input
            val password = passwordInput.text.toString().trim()  // Get password input

            when {
                username.isEmpty() || password.isEmpty() -> {
                    toast("Please fill in all fields.")          // Check for empty input
                }
                userDb.validateCredentials(username, password) -> {
                    toast("Login successful!")                   // Valid credentials

                    val sharedPref = requireContext()
                        .getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    sharedPref.edit().putString("loggedInUser", username).apply() // Save user

                    // Load home screen only after background data loads
                    (activity as? MainActivity)?.fetchAndCacheQuote {
                        activity?.runOnUiThread {
                            (activity as? MainActivity)?.preloadAndLoadHome()
                        }
                    }

                }
                else -> {
                    toast("Invalid username or password.")       // Invalid credentials
                }
            }
        }

        // Hide keyboard when tapping outside input fields
        view.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                view.clearFocus() // Remove focus from EditText
                val imm = requireContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0) // Hide keyboard
                v.performClick()
            }
            false
        }

        return view
    }

    // Show toast message
    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
