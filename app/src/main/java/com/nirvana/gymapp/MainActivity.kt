package com.nirvana.gymapp

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment

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
                "unit" -> loadFragment(UnitSelectionFragment(), "Choose Unit", true, false)
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
        titleText.text = title
        customBack.visibility = if (showUpArrow) View.VISIBLE else View.GONE
        bottomNav.visibility = if (showBottomNav) View.VISIBLE else View.GONE

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onBackPressed() {
        val fragmentManager = supportFragmentManager

        if (fragmentManager.backStackEntryCount > 1) {
            fragmentManager.popBackStack()

            fragmentManager.addOnBackStackChangedListener(object : androidx.fragment.app.FragmentManager.OnBackStackChangedListener {
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
            finish()
        }
    }
}
