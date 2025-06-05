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
import com.nirvana.gymapp.R
import com.nirvana.gymapp.database.UserDatabase
import com.nirvana.gymapp.fragments.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var titleText: TextView
    private lateinit var customBack: ImageView
    private lateinit var bottomNav: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize toolbar and navigation UI elements
        toolbar = findViewById(R.id.toolbar)
        titleText = findViewById(R.id.custom_title)
        customBack = findViewById(R.id.custom_back)
        bottomNav = findViewById(R.id.bottomNav)

        // Setup the toolbar without default title
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        customBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Update toolbar and nav whenever fragment stack changes
        supportFragmentManager.addOnBackStackChangedListener {
            updateToolbarAndBottomNav()
        }

        // Load initial fragment depending on intent extra
        if (savedInstanceState == null) {
            when (intent.getStringExtra("start")) {
                "email" -> loadFragment(EmailSignupFragment(), "Sign up", true, false, false)
                "phone" -> loadFragment(PhoneSignupFragment(), "Sign up", true, false, false)
                "login" -> loadFragment(LoginFragment(), "Log In", true, false, false)
                else -> preloadAndLoadHome()
            }
        }

        // Setup bottom navigation listeners
        findViewById<View>(R.id.navHome).setOnClickListener {
            preloadAndLoadHome()
        }
        findViewById<View>(R.id.navProfile).setOnClickListener {
            loadFragment(ProfileFragment(), "Profile", false, true)
        }
        findViewById<View>(R.id.navSettings).setOnClickListener {
            loadFragment(SettingsFragment(), "Settings", false, true)
        }
    }

    // Handles switching fragments with UI control options
    fun loadFragment(
        fragment: Fragment,
        title: String,
        showUpArrow: Boolean,
        showBottomNav: Boolean,
        addToBackStack: Boolean = true
    ) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
        if (addToBackStack) transaction.addToBackStack(null)
        transaction.commit()
        supportFragmentManager.executePendingTransactions()

        titleText.text = title
        customBack.visibility = if (showUpArrow) View.VISIBLE else View.GONE
        bottomNav.visibility = if (showBottomNav) View.VISIBLE else View.GONE
    }

    // Adjusts toolbar and bottom nav visibility based on current fragment
    private fun updateToolbarAndBottomNav() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        when (fragment) {
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
            else -> {
                customBack.visibility = View.VISIBLE
                bottomNav.visibility = View.GONE
            }
        }
    }

    // Load user data and launch HomeFragment
    fun preloadAndLoadHome() {
        val userPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val username = userPref.getString("loggedInUser", "guest") ?: "guest"

        Thread {
            val db = UserDatabase(this)
            db.getAllSavedRoutines(username)
            runOnUiThread {
                loadFragment(HomeFragment(), "Home", false, true)
            }
        }.start()
    }

    // Retrieve motivational quote from API or fallback
    fun fetchAndCacheQuote(onDone: (() -> Unit)? = null) {
        val sharedPref = getSharedPreferences("AppSessionPrefs", Context.MODE_PRIVATE)
        val client = OkHttpClient()
        val url = "http://api.forismatic.com/api/1.0/?method=getQuote&lang=en&format=json"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val fallback = listOf(
                    "No pain, no gain.",
                    "Train insane or remain the same.",
                    "Push harder than yesterday.",
                    "The body achieves what the mind believes.",
                    "Success starts with self-discipline.",
                    "Sweat is fat crying."
                ).random()
                sharedPref.edit().putString("cachedQuote", fallback).apply()
                onDone?.invoke()
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
                    val fallback = listOf(
                        "No pain, no gain.",
                        "Train insane or remain the same.",
                        "Push harder than yesterday.",
                        "The body achieves what the mind believes.",
                        "Success starts with self-discipline.",
                        "Sweat is fat crying."
                    ).random()
                    sharedPref.edit().putString("cachedQuote", fallback).apply()
                }
                onDone?.invoke()
            }
        })
    }

    // Custom back navigation handling
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    // Handle keyboard visibility and navigation bar visibility on touch
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is android.widget.EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    // Tap outside input field: hide keyboard and nav bar if in Settings
                    v.clearFocus()
                    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                    if (currentFragment is SettingsFragment) {
                        bottomNav.visibility = View.GONE
                    }
                } else {
                    // Tap inside input field: keep nav bar if in Settings
                    if (currentFragment is SettingsFragment) {
                        bottomNav.visibility = View.VISIBLE
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}
