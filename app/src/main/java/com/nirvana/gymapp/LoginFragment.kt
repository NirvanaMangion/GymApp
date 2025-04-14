package com.nirvana.gymapp

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

class LoginFragment : Fragment() {

    private lateinit var userDb: UserDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        userDb = UserDatabase(requireContext())

        val title = view.findViewById<TextView>(R.id.striveTitle)
        val usernameInput = view.findViewById<EditText>(R.id.usernameInput)
        val passwordInput = view.findViewById<EditText>(R.id.passwordInput)
        val loginButton = view.findViewById<Button>(R.id.loginBtn)

        title?.let {
            val styled = SpannableString("Strive")
            styled.setSpan(
                ForegroundColorSpan("#FFD600".toColorInt()),
                0, 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            it.text = styled
        }

        loginButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            when {
                username.isEmpty() || password.isEmpty() -> {
                    toast("Please fill in all fields.")
                }
                userDb.validateCredentials(username, password) -> {
                    toast("Login successful!")
                    (activity as MainActivity).loadFragment(
                        HomeFragment(),
                        title = "Home",
                        showUpArrow = false,
                        showBottomNav = true
                    )
                }
                else -> {
                    toast("Invalid username or password.")
                }
            }
        }

        // Dismiss keyboard on touch outside
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
