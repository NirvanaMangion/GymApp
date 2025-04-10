package com.nirvana.gymapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FrontpageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_frontpage)

        // Style the "Strive" title
        val titleView = findViewById<TextView>(R.id.striveTitle)
        val title = SpannableString("Strive")
        title.setSpan(ForegroundColorSpan(Color.parseColor("#FFD600")), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        titleView.text = title

        // Style "Log in" link
        val loginView = findViewById<TextView>(R.id.loginText)
        val loginText = SpannableStringBuilder("Already have an account? Log in")
        val start = loginText.indexOf("Log in")
        val end = start + "Log in".length
        loginText.setSpan(ForegroundColorSpan(Color.parseColor("#AA88FF")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        loginView.text = loginText

        // Sign up with email → Launch MainActivity and load EmailSignupFragment
        findViewById<Button>(R.id.emailSignup).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("start", "email")
            startActivity(intent)
        }

        // Sign up with phone → Launch MainActivity and load PhoneSignupFragment
        findViewById<Button>(R.id.btnPhoneSignup).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("start", "phone")
            startActivity(intent)
        }
    }
}
