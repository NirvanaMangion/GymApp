package com.nirvana.gymapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.widget.Button

class UnitSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unit_selection)

        // Set up the custom Toolbar as the ActionBar.
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        // Remove the default title ("Gym App" or any text) from the toolbar.
        supportActionBar?.setDisplayShowTitleEnabled(false)
        // Enable the Up arrow.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Optional: modern back-press handling via OnBackPressedDispatcher.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Custom back press behavior (e.g., finish the activity)
                finish()
            }
        })

        // Set up the Continue button to navigate to HomepageActivity.
        val continueBtn = findViewById<Button>(R.id.continueBtn)
        continueBtn.setOnClickListener {
            val intent = Intent(this, HomepageActivity::class.java)
            startActivity(intent)
        }
    }

    // Handle the Up navigation button press.
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
