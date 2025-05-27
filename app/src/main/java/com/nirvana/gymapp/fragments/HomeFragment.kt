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
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class HomeFragment : Fragment() {

    private lateinit var dropdownText: TextView
    private lateinit var routineListContainer: LinearLayout
    private lateinit var noRoutineText: TextView
    private lateinit var rootLayout: View
    private lateinit var quoteText: TextView
    private lateinit var quoteBox: FrameLayout

    private var cachedRoutines: List<Pair<Int, String>> = emptyList()

    private val PREFS_NAME = "AppSessionPrefs"
    private val KEY_QUOTE = "cachedQuote"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        rootLayout = view.findViewById(R.id.homeRootLayout)
        rootLayout.visibility = View.VISIBLE

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
                showBottomNav = true
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

        var routinesDone = false
        var quoteDone = false

        fun tryShowUI() {
            if (routinesDone && quoteDone) {
                // UI already visible, nothing extra needed
            }
        }

        displaySavedRoutines(username) {
            routinesDone = true
            tryShowUI()
        }

        fetchMotivationalQuote {
            quoteDone = true
            tryShowUI()
        }
    }

    private fun getCachedQuote(): String? {
        val sharedPref = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString(KEY_QUOTE, null)
    }

    private fun cacheQuote(quote: String) {
        val sharedPref = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPref.edit().putString(KEY_QUOTE, quote).apply()
    }

    private fun displaySavedRoutines(username: String, onComplete: () -> Unit) {
        val db = UserDatabase(requireContext())
        val routines = db.getAllSavedRoutines(username)

        if (routines == cachedRoutines) {
            onComplete()
            return
        }
        cachedRoutines = routines

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
                    typeface = Typeface.DEFAULT_BOLD
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
                                displaySavedRoutines(username) { /* no need callback here */ }
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

        onComplete()
    }

    private fun fetchMotivationalQuote(onComplete: () -> Unit) {
        val cached = getCachedQuote()
        if (cached != null) {
            quoteText.text = cached
            onComplete()
            return
        }

        val client = OkHttpClient()
        val url = "http://api.forismatic.com/api/1.0/?method=getQuote&lang=en&format=json"

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    val fallback = "Push yourself – no one else will."
                    quoteText.text = fallback
                    cacheQuote(fallback)
                    onComplete()
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
                        cacheQuote(full)
                        onComplete()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    activity?.runOnUiThread {
                        val fallback = "Stay focused and never quit."
                        quoteText.text = fallback
                        cacheQuote(fallback)
                        onComplete()
                    }
                }
            }
        })
    }
}
