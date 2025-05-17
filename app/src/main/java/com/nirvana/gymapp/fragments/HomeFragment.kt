package com.nirvana.gymapp.fragments

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.nirvana.gymapp.R
import com.nirvana.gymapp.activities.MainActivity
import com.nirvana.gymapp.database.UserDatabase
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class HomeFragment : Fragment() {

    private lateinit var dropdownText: TextView
    private lateinit var routineListContainer: LinearLayout
    private lateinit var noRoutineText: TextView
    private lateinit var rootLayout: View
    private lateinit var quoteText: TextView
    private lateinit var quoteBox: FrameLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        rootLayout = view.findViewById(R.id.homeRootLayout)
        rootLayout.visibility = View.INVISIBLE

        dropdownText = view.findViewById(R.id.myRoutineDropdown)
        routineListContainer = view.findViewById(R.id.routineListContainer)
        noRoutineText = view.findViewById(R.id.noRoutineText)
        quoteText = view.findViewById(R.id.quoteText)
        quoteBox = view.findViewById(R.id.quoteBox)
        val addRoutineBtn = view.findViewById<Button>(R.id.addRoutineBtn)

        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("loggedInUser", "guest") ?: "guest"

        addRoutineBtn.setOnClickListener {
            val userDb = UserDatabase(requireContext())
            userDb.clearRoutineExercises(username)

            (activity as MainActivity).loadFragment(
                AddRoutineFragment(),
                title = "Add Routine",
                showUpArrow = true,
                showBottomNav = false
            )
        }

        dropdownText.setOnClickListener {
            val isVisible = routineListContainer.visibility == View.VISIBLE
            routineListContainer.visibility = if (isVisible) View.GONE else View.VISIBLE

            val hasRoutines = UserDatabase(requireContext()).getAllSavedRoutines(username).isNotEmpty()
            noRoutineText.visibility = if (!isVisible && !hasRoutines) View.VISIBLE else View.GONE

            val arrowRes = if (isVisible) R.drawable.right_arrow else R.drawable.down_arrow
            dropdownText.setCompoundDrawablesWithIntrinsicBounds(0, 0, arrowRes, 0)
        }

        return view
    }

    override fun onResume() {
        super.onResume()

        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("loggedInUser", "guest") ?: "guest"

        displaySavedRoutines(username)

        val hasRoutines = UserDatabase(requireContext()).getAllSavedRoutines(username).isNotEmpty()
        routineListContainer.visibility = if (hasRoutines) View.VISIBLE else View.GONE
        noRoutineText.visibility = if (hasRoutines) View.GONE else View.VISIBLE
        dropdownText.setCompoundDrawablesWithIntrinsicBounds(
            0, 0,
            if (hasRoutines) R.drawable.down_arrow else R.drawable.right_arrow,
            0
        )

        rootLayout.alpha = 0f
        rootLayout.visibility = View.VISIBLE
        rootLayout.animate().alpha(1f).setDuration(150).start()

        fetchMotivationalQuote()
    }

    private fun fetchMotivationalQuote() {
        val client = OkHttpClient()
        val url = "http://api.forismatic.com/api/1.0/?method=getQuote&lang=en&format=json"

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    quoteText.text = "Push yourself – no one else will."
                }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val raw = response.body?.string()?.replace("\\\"", "\"")
                    val json = JSONObject(raw ?: "")
                    val quote = json.optString("quoteText", "Keep pushing forward.").trim()
                    val author = json.optString("quoteAuthor", "").trim()
                    val full = if (author.isNotEmpty()) "$quote\n\n– $author" else quote

                    activity?.runOnUiThread {
                        quoteText.text = full
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    activity?.runOnUiThread {
                        quoteText.text = "Stay focused and never quit."
                    }
                }
            }
        })
    }

    private fun displaySavedRoutines(username: String) {
        val db = UserDatabase(requireContext())
        val routines = db.getAllSavedRoutines(username)

        routineListContainer.removeAllViews()

        if (routines.isEmpty()) {
            noRoutineText.visibility = View.VISIBLE
            routineListContainer.visibility = View.GONE
        } else {
            noRoutineText.visibility = View.GONE
            routineListContainer.visibility = View.VISIBLE

            for ((id, name) in routines) {
                val card = FrameLayout(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        260
                    ).apply {
                        setMargins(0, 0, 0, 40)
                    }
                    background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_grey_background)

                    // ⬅️ Make the entire card clickable here
                    isClickable = true
                    isFocusable = true
                    setOnClickListener {
                        val detailFragment = RoutineDetailFragment.newInstance(id, name)
                        (activity as? MainActivity)?.loadFragment(
                            fragment = detailFragment,
                            title = name,
                            showUpArrow = true,
                            showBottomNav = false
                        )
                    }
                }

                val container = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(32, 24, 32, 24)
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    gravity = Gravity.CENTER
                }

                val title = TextView(requireContext()).apply {
                    text = name
                    textSize = 22f
                    setTextColor(Color.parseColor("#F5F5F5"))
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    gravity = Gravity.CENTER
                }

                val binIcon = ImageView(requireContext()).apply {
                    setImageResource(R.drawable.binicon)
                    layoutParams = FrameLayout.LayoutParams(60, 60, Gravity.END or Gravity.BOTTOM).apply {
                        setMargins(0, 0, 24, 24)
                    }
                    setOnClickListener {
                        AlertDialog.Builder(requireContext())
                            .setTitle("Delete Routine")
                            .setMessage("Are you sure you want to delete this routine?")
                            .setPositiveButton("Yes") { _, _ ->
                                db.deleteRoutineById(id)
                                displaySavedRoutines(username)
                            }
                            .setNegativeButton("No", null)
                            .show()
                    }
                }

                container.addView(title)
                card.addView(container)
                card.addView(binIcon)
                routineListContainer.addView(card)
            }

        }

        dropdownText.text = "My Routine (${routines.size})"
    }
}
