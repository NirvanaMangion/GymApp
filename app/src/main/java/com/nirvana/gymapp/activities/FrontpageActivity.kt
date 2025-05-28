package com.nirvana.gymapp.activities

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
import com.nirvana.gymapp.R

class FrontpageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_frontpage)

        // Set the "Strive" title with a custom color for the first letter
        val titleView = findViewById<TextView>(R.id.striveTitle)
        val title = SpannableString("Strive")
        title.setSpan(ForegroundColorSpan(Color.parseColor("#FFD600")), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        titleView.text = title

        // Style "Log in" in the login prompt text
        val loginView = findViewById<TextView>(R.id.loginText)
        val loginText = SpannableStringBuilder("Already have an account? Log in")
        val start = loginText.indexOf("Log in")
        val end = start + "Log in".length
        loginText.setSpan(ForegroundColorSpan(Color.parseColor("#FFD600")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        loginView.text = loginText

        // When login text is clicked, open MainActivity and indicate login should be shown
        loginView.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("start", "login")
            startActivity(intent)
        }

        // When email signup button is clicked, open MainActivity and indicate email signup
        findViewById<Button>(R.id.emailSignup).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("start", "email")
            startActivity(intent)
        }

        // When phone signup button is clicked, open MainActivity and indicate phone signup
        findViewById<Button>(R.id.btnPhoneSignup).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("start", "phone")
            startActivity(intent)
        }
    }
}
