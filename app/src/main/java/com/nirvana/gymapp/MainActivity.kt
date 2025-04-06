package com.nirvana.gymapp

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import android.content.Intent
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Style "Strive"
        val titleView = findViewById<TextView>(R.id.striveTitle)
        val title = SpannableString("Strive")
        title.setSpan(
            ForegroundColorSpan(Color.parseColor("#FFD600")),
            0, 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        titleView.text = title

        // Log in text color
        val loginView = findViewById<TextView>(R.id.loginText)
        val loginText = SpannableStringBuilder("Already have an account? Log in")
        val start = loginText.indexOf("Log in")
        val end = start + "Log in".length
        loginText.setSpan(
            ForegroundColorSpan(Color.parseColor("#AA88FF")),
            start, end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        loginView.text = loginText

        // Launch Email Sign Up Activity
        val emailSignupBtn = findViewById<Button>(R.id.emailSignup)
        emailSignupBtn.setOnClickListener {
            startActivity(Intent(this, EmailSignupActivity::class.java))
        }

        // Launch Phone Sign Up Activity
        // Launch Phone Sign Up Activity
        val phoneSignupBtn = findViewById<Button>(R.id.btnPhoneSignup)  // This should match the ID in the XML
        phoneSignupBtn.setOnClickListener {
            // Start the PhoneSignupActivity when the button is clicked
            startActivity(Intent(this, PhoneSignupActivity::class.java))
        }

    }
}
