package com.nirvana.gymapp

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class EmailSignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_signup)

        // Set up the Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Remove the default title and set the custom title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle("")  // Remove default title

        // Custom title setup
        val titleView = findViewById<TextView>(R.id.striveTitle)
        val styledTitle = SpannableString("Strive")
        styledTitle.setSpan(
            ForegroundColorSpan(Color.parseColor("#FFD600")),
            0, 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        titleView.text = styledTitle
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
