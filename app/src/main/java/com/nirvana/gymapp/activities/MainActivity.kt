package com.nirvana.gymapp.activities

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.nirvana.gymapp.R
import com.nirvana.gymapp.database.UserDatabase
import com.nirvana.gymapp.fragments.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    // UI components
    private lateinit var toolbar: Toolbar
    private lateinit var titleText: TextView
    private lateinit var customBack: ImageView
    private lateinit var bottomNav: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bind toolbar and elements
        toolbar = findViewById(R.id.toolbar)
        titleText = findViewById(R.id.custom_title)
        customBack = findViewById(R.id.custom_back)
        bottomNav = findViewById(R.id.bottomNav)

        // Set custom toolbar without default title
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Handle back arrow click
        customBack.setOnClickListener { onBackPressed() }

        // Show initial screen based on intent extra
        if (savedInstanceState == null) {
            when (intent.getStringExtra("start")) {
                "email" -> loadFragment(EmailSignupFragment(), "Sign up", true, false, false)
                "phone" -> loadFragment(PhoneSignupFragment(), "Sign up", true, false, false)
                "login" -> loadFragment(LoginFragment(), "Log In", true, false, false)
                else -> preloadAndLoadHome() // Default to home if no intent
            }
        }

        // Bottom navigation buttons
        findViewById<View>(R.id.navHome).setOnClickListener {
            preloadAndLoadHome() // Refresh home
        }

        findViewById<View>(R.id.navProfile).setOnClickListener {
            loadFragment(ProfileFragment(), "Profile", false, true)
        }

        findViewById<View>(R.id.navSettings).setOnClickListener {
            loadFragment(SettingsFragment(), "Settings", false, true)
        }
    }


    // Loads a fragment with toolbar/nav visibility control
    fun loadFragment(
        fragment: Fragment,
        title: String,
        showUpArrow: Boolean,
        showBottomNav: Boolean,
        addToBackStack: Boolean = true
    ) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
        supportFragmentManager.executePendingTransactions()

        // Update toolbar title and visibility
        titleText.text = title
        customBack.visibility = if (showUpArrow) View.VISIBLE else View.GONE
        bottomNav.visibility = if (showBottomNav) View.VISIBLE else View.GONE
    }


    // Loads user routines and motivational quote before showing HomeFragment
    fun preloadAndLoadHome() {
        val sharedPref = getSharedPreferences("AppSessionPrefs", Context.MODE_PRIVATE)
        val userPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val username = userPref.getString("loggedInUser", "guest") ?: "guest"
        val cachedQuote = sharedPref.getString("cachedQuote", null)

        var routinesDone = false
        var quoteDone = false

        // Wait for both routines and quote to load
        fun tryLoadHome() {
            if (routinesDone && quoteDone) {
                runOnUiThread {
                    loadFragment(HomeFragment(), "Home", false, true, false)
                }
            }
        }

        // Load saved routines in background thread
        Thread {
            val db = UserDatabase(this)
            db.getAllSavedRoutines(username) // Assuming this caches or populates memory
            routinesDone = true
            tryLoadHome()
        }.start()

        // Load daily motivational quote using OkHttp
        val client = OkHttpClient()
        val url = "http://api.forismatic.com/api/1.0/?method=getQuote&lang=en&format=json"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // On failure, use a fallback quote
                if (cachedQuote == null) {
                    val fallbackQuotes = listOf(
                        "No pain, no gain.",
                        "Train insane or remain the same.",
                        "Push harder than yesterday if you want a different tomorrow.",
                        "The body achieves what the mind believes.",
                        "Success starts with self-discipline.",
                        "Sweat is fat crying."
                    )
                    val random = fallbackQuotes.random()
                    sharedPref.edit().putString("cachedQuote", random).apply()
                }
                quoteDone = true
                tryLoadHome()
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val raw = response.body?.string()?.replace("\\\"", "\"")
                    val json = JSONObject(raw ?: "")
                    val quote = json.optString("quoteText", "Keep pushing forward.").trim()
                    val author = json.optString("quoteAuthor", "").trim()
                    val full = if (author.isNotEmpty()) "$quote\n\n– $author" else quote
                    sharedPref.edit().putString("cachedQuote", full).apply()
                } catch (e: Exception) {
                    // Fallback if JSON fails
                    if (cachedQuote == null) {
                        val fallbackQuotes = listOf(
                            "No pain, no gain.",
                            "Train insane or remain the same.",
                            "Push harder than yesterday if you want a different tomorrow.",
                            "The body achieves what the mind believes.",
                            "Success starts with self-discipline.",
                            "Sweat is fat crying."
                        )
                        val random = fallbackQuotes.random()
                        sharedPref.edit().putString("cachedQuote", random).apply()
                    }
                }
                quoteDone = true
                tryLoadHome()
            }
        })
    }


    // Handles back navigation logic and updates toolbar/nav visibility
    override fun onBackPressed() {
        val fragmentManager = supportFragmentManager

        if (fragmentManager.backStackEntryCount > 1) {
            fragmentManager.popBackStack()

            // Listener to update UI based on the new top fragment
            fragmentManager.addOnBackStackChangedListener(object : FragmentManager.OnBackStackChangedListener {
                override fun onBackStackChanged() {
                    val currentFragment = fragmentManager.findFragmentById(R.id.fragment_container)
                    when (currentFragment) {
                        is HomeFragment -> {
                            titleText.text = "Home"
                            customBack.visibility = View.GONE
                            bottomNav.visibility = View.VISIBLE
                        }
                        is ProfileFragment -> {
                            titleText.text = "Profile"
                            customBack.visibility = View.GONE
                            bottomNav.visibility = View.VISIBLE
                        }
                        is SettingsFragment -> {
                            titleText.text = "Settings"
                            customBack.visibility = View.GONE
                            bottomNav.visibility = View.VISIBLE
                        }
                    }
                    fragmentManager.removeOnBackStackChangedListener(this)
                }
            })
        } else {
            // Default system back if no more fragments
            super.onBackPressed()
        }
    }


    // Hide keyboard when touching outside EditText
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is android.widget.EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}
