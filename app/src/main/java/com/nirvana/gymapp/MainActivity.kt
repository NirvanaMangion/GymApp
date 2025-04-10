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

        // Find toolbar components
        toolbar = findViewById(R.id.toolbar)
        titleText = findViewById(R.id.custom_title)
        customBack = findViewById(R.id.custom_back)
        bottomNav = findViewById(R.id.bottomNav)

        // Set as support action bar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Back button click
        customBack.setOnClickListener {
            onBackPressed()
        }

        // Load fragment based on intent extra
        when (intent.getStringExtra("start")) {
            "email" -> loadFragment(EmailSignupFragment(), "Sign up", true, false)
            "phone" -> loadFragment(PhoneSignupFragment(), "Sign up", true, false)
            "unit" -> loadFragment(UnitSelectionFragment(), "Choose Unit", true, false)
            else    -> loadFragment(HomeFragment(), "Home", false, true)
        }

        // Bottom nav buttons
        findViewById<View>(R.id.navHome).setOnClickListener {
            loadFragment(HomeFragment(), "Home", false, true)
        }

        findViewById<View>(R.id.navProfile).setOnClickListener {
            loadFragment(ProfileFragment(), "Profile", false, true) // hide back button here
        }

        findViewById<View>(R.id.navSettings).setOnClickListener {
            // Settings fragment placeholder
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
        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
        } else {
            finish()
        }
    }
}
