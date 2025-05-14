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
import com.nirvana.gymapp.fragments.*

class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var titleText: TextView
    private lateinit var customBack: ImageView
    private lateinit var bottomNav: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        titleText = findViewById(R.id.custom_title)
        customBack = findViewById(R.id.custom_back)
        bottomNav = findViewById(R.id.bottomNav)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        customBack.setOnClickListener {
            onBackPressed()
        }

        if (savedInstanceState == null) {
            when (intent.getStringExtra("start")) {
                "email" -> loadFragment(EmailSignupFragment(), "Sign up", true, false)
                "phone" -> loadFragment(PhoneSignupFragment(), "Sign up", true, false)
                "unit" -> loadFragment(UnitSelectionFragment(), "Choose Unit", false, false)
                "login" -> loadFragment(LoginFragment(), "Log In", true, false)
                else -> loadFragment(HomeFragment(), "Home", false, true)
            }
        }

        findViewById<View>(R.id.navHome).setOnClickListener {
            loadFragment(HomeFragment(), "Home", false, true)
        }

        findViewById<View>(R.id.navProfile).setOnClickListener {
            loadFragment(ProfileFragment(), "Profile", false, true)
        }

        findViewById<View>(R.id.navSettings).setOnClickListener {
            loadFragment(SettingsFragment(), "Settings", false, true)
        }
    }

    fun loadFragment(
        fragment: Fragment,
        title: String,
        showUpArrow: Boolean,
        showBottomNav: Boolean
    ) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()

        // Delay UI updates until the fragment is attached
        supportFragmentManager.executePendingTransactions()

        // Then update toolbar and bottom nav
        titleText.text = title
        customBack.visibility = if (showUpArrow) View.VISIBLE else View.GONE
        bottomNav.visibility = if (showBottomNav) View.VISIBLE else View.GONE
    }


    override fun onBackPressed() {
        val fragmentManager = supportFragmentManager

        if (fragmentManager.backStackEntryCount > 1) {
            fragmentManager.popBackStack()

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
            super.onBackPressed() // ✅ called only when appropriate
        }
    }

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
