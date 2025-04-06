package com.nirvana.gymapp

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set colored "Strive"
        val titleView = findViewById<TextView>(R.id.striveTitle)
        val title = SpannableString("Strive")
        title.setSpan(ForegroundColorSpan(Color.parseColor("#FFD600")), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        titleView.text = title

        // Set "Already have an account? Log in" with Log in in purple
        val loginView = findViewById<TextView>(R.id.loginText)
        val loginText = SpannableStringBuilder("Already have an account? Log in")
        val loginStart = loginText.indexOf("Log in")
        val loginEnd = loginStart + "Log in".length
        loginText.setSpan(
            ForegroundColorSpan(Color.parseColor("#AA88FF")),
            loginStart, loginEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        loginView.text = loginText
    }
}
