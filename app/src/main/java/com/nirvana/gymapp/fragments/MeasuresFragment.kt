package com.nirvana.gymapp.fragments

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import com.nirvana.gymapp.R
import java.text.SimpleDateFormat
import java.util.*

class MeasureFragment : Fragment() {

    private lateinit var historyContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_measures, container, false)

        val weightInput = view.findViewById<EditText>(R.id.weightInput)
        val chestInput = view.findViewById<EditText>(R.id.chestInput)
        val waistInput = view.findViewById<EditText>(R.id.waistInput)
        val armsInput = view.findViewById<EditText>(R.id.armsInput)
        val uploadBtn = view.findViewById<Button>(R.id.uploadBtn)
        val saveBtn = view.findViewById<Button>(R.id.saveBtn)
        historyContainer = view.findViewById(R.id.historyContainer)

        uploadBtn.setOnClickListener {
            Toast.makeText(requireContext(), "Photo upload feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        saveBtn.setOnClickListener {
            val weight = weightInput.text.toString().trim()
            val chest = chestInput.text.toString().trim()
            val waist = waistInput.text.toString().trim()
            val arms = armsInput.text.toString().trim()

            if (weight.isEmpty() || chest.isEmpty() || waist.isEmpty() || arms.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val now = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

            val logCard = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(0xFF1E1E1E.toInt())
                setPadding(32, 24, 32, 24)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, 24)
                layoutParams = params
            }

            val dateView = TextView(requireContext()).apply {
                text = now
                setTextColor(0xFFAAAAAA.toInt())
                textSize = 12f
            }

            val details = TextView(requireContext()).apply {
                text = """
                    Weight: $weight kg
                    Chest: $chest cm
                    Waist: $waist cm
                    Arms: $arms cm
                    Photo: Not uploaded
                """.trimIndent()
                setTextColor(0xFFFFFFFF.toInt())
                textSize = 14f
                setPadding(0, 8, 0, 0)
            }

            logCard.addView(dateView)
            logCard.addView(details)
            historyContainer.addView(logCard, 0)

            Toast.makeText(requireContext(), "Entry saved.", Toast.LENGTH_SHORT).show()

            weightInput.text.clear()
            chestInput.text.clear()
            waistInput.text.clear()
            armsInput.text.clear()
        }

        // Hide keyboard when tapping outside input fields
        view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
            false
        }

        return view
    }
}
