package com.nirvana.gymapp

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.toColorInt

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

        // Custom title setup (Spannable String for the title)
        val titleView = findViewById<TextView>(R.id.striveTitle)
        val styledTitle = SpannableString("Strive")
        styledTitle.setSpan(
            ForegroundColorSpan("#FFD600".toColorInt()),  // Use toColorInt() for the color
            0, 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        titleView.text = styledTitle

        // Inside onCreate method of EmailSignupActivity
        val continueButton = findViewById<Button>(R.id.continueBtn)
        continueButton.setOnClickListener {
            // Navigate to UnitSectionActivity when Continue is clicked
            val intent = Intent(this, UnitSelectionActivity::class.java)
            startActivity(intent)
        }
    }

    // Override dispatchTouchEvent to handle keyboard dismissal
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        val focusedView = currentFocus
        if (focusedView is EditText) {
            val outRect = android.graphics.Rect()
            focusedView.getGlobalVisibleRect(outRect)
            if (!outRect.contains(event?.rawX?.toInt() ?: 0, event?.rawY?.toInt() ?: 0)) {
                // If the touch event is outside the EditText, hide the keyboard
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(focusedView.windowToken, 0)
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()  // Closes this activity and goes back
        return true
    }
}
