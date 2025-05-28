package com.nirvana.gymapp.fragments

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import com.nirvana.gymapp.R
import com.nirvana.gymapp.database.UserDatabase

class RoutineDetailFragment : Fragment() {

    companion object {
        private const val ARG_ROUTINE_ID = "routine_id" // Key for routine ID in arguments
        private const val ARG_ROUTINE_NAME = "routine_name" // Key for routine name in arguments

        // Factory method to create a new instance of this fragment
        fun newInstance(id: Int, name: String): RoutineDetailFragment {
            val fragment = RoutineDetailFragment()
            val args = Bundle()
            args.putInt(ARG_ROUTINE_ID, id)
            args.putString(ARG_ROUTINE_NAME, name)
            fragment.arguments = args
            return fragment
        }
    }

    private var routineId: Int = -1 // Routine ID passed in arguments
    private var routineName: String? = null
    private lateinit var containerLayout: LinearLayout
    private lateinit var timerTextView: TextView
    private lateinit var topButton: Button // Start/Pause/Resume button
    private lateinit var completeButton: Button // Button to complete routine

    private var secondsElapsed = 0
    private var isPaused = false
    private var routineStartTime: Long = 0L // Start timestamp for the routine

    private val handler = Handler(Looper.getMainLooper()) // Handler for timer updates
    private val timerRunnable = object : Runnable {
        override fun run() {
            if (!isPaused) {
                secondsElapsed++
                timerTextView.text = formatTime(secondsElapsed) // Update timer display
            }
            handler.postDelayed(this, 1000) // Repeat every second
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        routineId = arguments?.getInt(ARG_ROUTINE_ID) ?: -1
        routineName = arguments?.getString(ARG_ROUTINE_NAME)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_routine_detail, container, false)

        // Bind views
        containerLayout = view.findViewById(R.id.exerciseLogContainer)
        timerTextView = view.findViewById(R.id.timerDisplay)
        topButton = view.findViewById(R.id.startRoutineBtn)
        completeButton = view.findViewById(R.id.completeRoutineBtn)

        // Hide keyboard when touching outside input
        view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
            false
        }

        //  Start/Pause/Resume Button
        topButton.setOnClickListener {
            if (topButton.text == "Start Routine") {
                routineStartTime = System.currentTimeMillis() // Record start time
                handler.post(timerRunnable) // Start timer
                topButton.text = "Pause Routine"
                topButton.setBackgroundColor(Color.parseColor("#FFD600"))
                topButton.setTextColor(Color.BLACK)
                completeButton.visibility = View.VISIBLE // Show complete button
            } else {
                isPaused = !isPaused // Toggle pause state
                topButton.text = if (isPaused) "Resume Routine" else "Pause Routine"
                val bgColor = if (isPaused) "#444444" else "#FFD600"
                val textColor = if (isPaused) "#FFFFFF" else "#000000"
                topButton.setBackgroundColor(Color.parseColor(bgColor))
                topButton.setTextColor(Color.parseColor(textColor))
            }
        }

        // Complete Button
        completeButton.setOnClickListener {
            handler.removeCallbacks(timerRunnable) // Stop timer
            val routineEndTime = System.currentTimeMillis()
            val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val userId = sharedPref.getString("loggedInUser", "guest") ?: "guest"
            val db = UserDatabase(requireContext())
            db.insertCompletedRoutineV2(userId, routineName ?: "Unnamed", routineStartTime, routineEndTime)
            Toast.makeText(requireContext(), "Routine Completed!", Toast.LENGTH_SHORT).show()

            // Navigate to ProfileFragment
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment())
                .commit()
        }

        //  Add exercises to the screen
        val exercises = UserDatabase(requireContext()).getExercisesForRoutine(routineId)
        for ((name, category) in exercises) {
            containerLayout.addView(createExerciseLog(name, category)) // Add exercise log
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(timerRunnable) // Stop timer when fragment is destroyed
    }

    // Format seconds into MM:SS format
    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }

    // Create the UI view for a single exercise log
    private fun createExerciseLog(name: String, category: String): View {
        val context = requireContext()
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 48, 0, 96)
        }

        val title = TextView(context).apply {
            text = "$name ($category)"
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setPadding(0, 0, 0, 12)
        }

        val setContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        fun addSetRow(setNumber: Int) {
            // Header row (SET, KG, REPS)
            val headerRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            listOf("SET", "KG", "REPS").forEach { label ->
                headerRow.addView(TextView(context).apply {
                    text = label
                    setTextColor(Color.LTGRAY)
                    textSize = 13f
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                })
            }

            // Input row for a set (set number, kg input, reps input)
            val valueRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(0, 6, 0, 12)
            }

            val setText = TextView(context).apply {
                text = setNumber.toString()
                setTextColor(Color.WHITE)
                textSize = 15f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val kgInput = EditText(context).apply {
                setText("-")
                setTextColor(Color.WHITE)
                textSize = 15f
                setSingleLine(true)
                inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                imeOptions = EditorInfo.IME_ACTION_DONE
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus && text.toString() == "-") setText("")
                }
            }

            val repsInput = EditText(context).apply {
                setText("-")
                setTextColor(Color.WHITE)
                textSize = 15f
                setSingleLine(true)
                inputType = android.text.InputType.TYPE_CLASS_NUMBER
                imeOptions = EditorInfo.IME_ACTION_DONE
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus && text.toString() == "-") setText("")
                }
            }

            valueRow.addView(setText)
            valueRow.addView(kgInput)
            valueRow.addView(repsInput)

            setContainer.addView(headerRow)
            setContainer.addView(valueRow)
        }

        var setCount = 1
        addSetRow(setCount) // Add initial set

        val roundedBackground = GradientDrawable().apply {
            setColor(Color.parseColor("#2D2D2D"))
            cornerRadius = 48f
        }

        val addSetButton = Button(context).apply {
            text = "+ Add Set"
            textSize = 11f
            setTextColor(Color.WHITE)
            background = roundedBackground
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                110
            ).apply {
                topMargin = 16
            }
            gravity = Gravity.CENTER
            isAllCaps = false
        }

        addSetButton.setOnClickListener {
            setCount++
            addSetRow(setCount) // Add new input row for another set
        }

        // Add all elements to the container and return it
        container.addView(title)
        container.addView(setContainer)
        container.addView(addSetButton)

        return container
    }
}