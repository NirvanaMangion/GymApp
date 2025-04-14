package com.nirvana.gymapp

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

class EmailSignupFragment : Fragment() {

    private lateinit var userDb: UserDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_email_signup, container, false)
        userDb = UserDatabase(requireContext())

        val title = view.findViewById<TextView>(R.id.striveTitle)
        val usernameInput = view.findViewById<EditText>(R.id.usernameInput)
        val emailInput = view.findViewById<EditText>(R.id.emailInput)
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
            val email = emailInput.text?.toString()?.trim() ?: ""
            val password = passwordInput.text?.toString()?.trim() ?: ""

            when {
                username.isEmpty() || email.isEmpty() || password.isEmpty() ->
                    toast("Please fill in all fields.")

                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                    toast("Please enter a valid email address.")

                password.length < 6 || !password.any { it.isDigit() } || !password.any { it.isLetter() } ->
                    toast("Password must be at least 6 characters, with letters and numbers.")

                !termsCheckbox.isChecked ->
                    toast("You must accept the terms.")

                userDb.checkUserExists(username) ->
                    toast("Username already exists.")

                else -> {
                    userDb.addUser(username, email, null, password)
                    toast("Account created!")
                    val fragment = UnitSelectionFragment()
                    fragment.arguments = Bundle().apply {
                        putString("username", username)  // ✅ pass the signed-up username
                    }
                    (activity as MainActivity).loadFragment(
                        fragment,
                        title = "Choose Unit",
                        showUpArrow = true,
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
