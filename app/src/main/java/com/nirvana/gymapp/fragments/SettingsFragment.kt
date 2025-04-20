package com.nirvana.gymapp.fragments

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.nirvana.gymapp.R

class SettingsFragment : Fragment() {

    private lateinit var btnPersonal: Button
    private lateinit var btnEmailPass: Button
    private lateinit var personalLayout: LinearLayout
    private lateinit var passwordLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        btnPersonal = view.findViewById(R.id.btnPersonal)
        btnEmailPass = view.findViewById(R.id.btnEmailPass)
        personalLayout = view.findViewById(R.id.personalLayout)
        passwordLayout = view.findViewById(R.id.passwordLayout)

        fun highlightActiveButton(active: Button, inactive: Button) {
            active.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.grey_700))
            active.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))

            inactive.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.grey_900))
            inactive.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_400))
        }

        btnPersonal.setOnClickListener {
            personalLayout.visibility = View.VISIBLE
            passwordLayout.visibility = View.GONE
            highlightActiveButton(btnPersonal, btnEmailPass)
        }

        btnEmailPass.setOnClickListener {
            personalLayout.visibility = View.GONE
            passwordLayout.visibility = View.VISIBLE
            highlightActiveButton(btnEmailPass, btnPersonal)
        }

        return view
    }
}
