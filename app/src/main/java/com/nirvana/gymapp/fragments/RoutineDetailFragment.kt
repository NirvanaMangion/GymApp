package com.nirvana.gymapp.fragments

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import com.nirvana.gymapp.R
import com.nirvana.gymapp.database.UserDatabase

class RoutineDetailFragment : Fragment() {

    companion object {
        private const val ARG_ROUTINE_ID = "routine_id"
        private const val ARG_ROUTINE_NAME = "routine_name"

        // Factory method to create a new instance with arguments
        fun newInstance(id: Int, name: String): RoutineDetailFragment {
            val fragment = RoutineDetailFragment()
            val args = Bundle()
            args.putInt(ARG_ROUTINE_ID, id)
            args.putString(ARG_ROUTINE_NAME, name)
            fragment.arguments = args
            return fragment
        }
    }

   // Routine metadata passed via arguments
    private var routineId: Int = -1
    private var routineName: String? = null

    // UI component references
    private lateinit var containerLayout: LinearLayout
    private lateinit var timerTextView: TextView
    private lateinit var topButton: Button
    private lateinit var completeButton: Button

    // Timer-related state
    private var secondsElapsed = 0
    private var isPaused = false
    private var routineStartTime: Long = 0L

    // Handler to update the timer every second
    private val handler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            if (!isPaused) {
                secondsElapsed++
                timerTextView.text = formatTime(secondsElapsed)
            }
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        routineId = arguments?.getInt(ARG_ROUTINE_ID) ?: -1
        routineName = arguments?.getString(ARG_ROUTINE_NAME)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_routine_detail, container, false)

        // Bind UI elements
        containerLayout = view.findViewById(R.id.exerciseLogContainer)
        timerTextView = view.findViewById(R.id.timerDisplay)
        topButton = view.findViewById(R.id.startRoutineBtn)
        completeButton = view.findViewById(R.id.completeRoutineBtn)

        // Hide keyboard when tapping outside input
        view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
            false
        }

        // Start or pause the routine timer
        topButton.setOnClickListener {
            if (topButton.text == "Start Routine") {
                routineStartTime = System.currentTimeMillis()
                handler.post(timerRunnable)
                topButton.text = "Pause Routine"
                topButton.setBackgroundColor(Color.parseColor("#FFD600"))
                topButton.setTextColor(Color.BLACK)
                completeButton.visibility = View.VISIBLE
            } else {
                isPaused = !isPaused
                topButton.text = if (isPaused) "Resume Routine" else "Pause Routine"
                val bgColor = if (isPaused) "#444444" else "#FFD600"
                val textColor = if (isPaused) "#FFFFFF" else "#000000"
                topButton.setBackgroundColor(Color.parseColor(bgColor))
                topButton.setTextColor(Color.parseColor(textColor))
            }
        }

        // Validate and complete routine
        completeButton.setOnClickListener {
            val isValid = (0 until containerLayout.childCount).all { i ->
                val exerciseLayout = containerLayout.getChildAt(i) as LinearLayout
                var foundValidSet = false

                // Loop to find the setContainer inside exerciseLayout
                for (j in 0 until exerciseLayout.childCount) {
                    val maybeSetContainer = exerciseLayout.getChildAt(j)

                    if (maybeSetContainer is LinearLayout) {
                        for (k in 0 until maybeSetContainer.childCount) {
                            val setRow = maybeSetContainer.getChildAt(k)

                            // Only check rows tagged as "set_row"
                            if (setRow is LinearLayout && setRow.tag == "set_row") {
                                val kgInput = setRow.getChildAt(1)
                                val repsInput = setRow.getChildAt(2)

                                if (kgInput is EditText && repsInput is EditText) {
                                    val kgText = kgInput.text.toString().trim()
                                    val repsText = repsInput.text.toString().trim()

                                    if (kgText.isNotEmpty() && kgText != "-" &&
                                        repsText.isNotEmpty() && repsText != "-"
                                    ) {
                                        foundValidSet = true
                                        break
                                    }
                                }
                            }
                        }
                    }

                    if (foundValidSet) break
                }

                foundValidSet
            }

            if (!isValid) {
                Toast.makeText(requireContext(), "Please fill at least one set per exercise.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Prevent completion if routine is shorter than 60 seconds
            if (secondsElapsed < 60) {
                Toast.makeText(requireContext(), "Routine must last at least 1 minute to complete.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // All checks passed: mark as completed
            handler.removeCallbacks(timerRunnable)
            val routineEndTime = System.currentTimeMillis()
            val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val userId = sharedPref.getString("loggedInUser", "guest") ?: "guest"
            val db = UserDatabase(requireContext())
            db.insertCompletedRoutineV2(userId, routineName ?: "Unnamed", routineStartTime, routineEndTime)
            Toast.makeText(requireContext(), "Routine Completed!", Toast.LENGTH_SHORT).show()

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment())
                .commit()
        }


        // Load exercises and create UI for each
        val exercises = UserDatabase(requireContext()).getExercisesForRoutine(routineId)
        for ((name, category) in exercises) {
            containerLayout.addView(createExerciseLog(name, category))
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(timerRunnable)
    }

    // Format seconds into MM:SS format
    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }

    // Create UI layout for one exercise (with sets)
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

        // Add a single input row for a set
        fun addSetRow(setNumber: Int) {
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

            val valueRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(0, 6, 0, 12)
                tag = "set_row" // Important: tag this as a valid set row
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
        addSetRow(setCount)

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
            addSetRow(setCount)
        }

        container.addView(title)
        container.addView(setContainer)
        container.addView(addSetButton)

        return container
    }
}
