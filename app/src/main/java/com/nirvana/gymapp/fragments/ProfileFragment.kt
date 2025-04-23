package com.nirvana.gymapp.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.nirvana.gymapp.R
import com.nirvana.gymapp.database.UserDatabase
import com.nirvana.gymapp.activities.MainActivity

class ProfileFragment : Fragment() {

    private lateinit var db: UserDatabase
    private lateinit var userId: String
    private lateinit var profileImageView: ImageView
    private lateinit var usernameTextView: TextView
    private lateinit var chart: com.github.mikephil.charting.charts.LineChart
    private lateinit var noDataText: TextView

    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Void?>
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Gallery launcher
        galleryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK && result.data?.data != null) {
                imageUri = result.data?.data
                profileImageView.setImageURI(imageUri)
                db.updateProfileImage(userId, imageUri.toString()) // Save to DB
            }
        }

        // Camera launcher
        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicturePreview()
        ) { bitmap: Bitmap? ->
            bitmap?.let {
                profileImageView.setImageBitmap(it)
                // Note: Not saved to DB since it's just a Bitmap. You could encode/save it to internal storage if needed.
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        db = UserDatabase(requireContext())

        // Get logged-in username from SharedPreferences
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        userId = sharedPref.getString("loggedInUser", "Username") ?: "Username"

        profileImageView = view.findViewById(R.id.profileImageView)
        usernameTextView = view.findViewById(R.id.usernameTextView)
        usernameTextView.text = userId

        // Load saved image URI from DB
        val savedUri = db.getProfileImage(userId)
        if (!savedUri.isNullOrEmpty()) {
            profileImageView.setImageURI(Uri.parse(savedUri))
        } else {
            profileImageView.setImageResource(R.drawable.personicon) // Default icon
        }

        profileImageView.setOnClickListener {
            showImageSourceDialog()
        }

        // Chart UI setup
        val btnDuration = view.findViewById<Button>(R.id.btnDuration)
        val btnVolume = view.findViewById<Button>(R.id.btnVolume)
        val btnReps = view.findViewById<Button>(R.id.btnReps)
        val btnWorkoutHistory = view.findViewById<Button>(R.id.btnWorkoutHistory)
        val btnMeasures = view.findViewById<Button>(R.id.btnMeasures)

        chart = view.findViewById(R.id.lineChart)
        noDataText = view.findViewById(R.id.tvNoData)

        btnWorkoutHistory.setOnClickListener {
            (activity as? MainActivity)?.loadFragment(
                WorkoutHistoryFragment(),
                title = "Workout History",
                showUpArrow = true,
                showBottomNav = false
            )
        }

        btnMeasures.setOnClickListener {
            (activity as? MainActivity)?.loadFragment(
                MeasureFragment(),
                title = "Body Measures",
                showUpArrow = true,
                showBottomNav = false
            )
        }

        val selectedColor = "#FFD600"
        val defaultColor = "#333333"

        fun highlightSelected(selected: Button) {
            listOf(btnDuration, btnVolume, btnReps).forEach { button ->
                if (button == selected) {
                    button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(selectedColor)))
                    button.setTextColor(android.graphics.Color.BLACK)
                } else {
                    button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(defaultColor)))
                    button.setTextColor(android.graphics.Color.WHITE)
                }
            }
        }

        fun loadAndShowLineChart(type: String) {
            try {
                val entries = mutableListOf<com.github.mikephil.charting.data.Entry>()
                val labels = mutableListOf<String>()

                val rawData = when (type) {
                    "duration" -> db.getV2Durations(userId)
                    "volume" -> db.getDailyVolumes(userId)
                    "reps" -> db.getDailyReps(userId)
                    else -> emptyMap()
                }

                if (rawData.isEmpty()) {
                    chart.visibility = View.GONE
                    noDataText.visibility = View.VISIBLE
                    return
                }

                chart.visibility = View.VISIBLE
                noDataText.visibility = View.GONE

                val sorted = rawData.toSortedMap()
                var index = 0f
                if (sorted.size == 1) {
                    entries.add(com.github.mikephil.charting.data.Entry(index, 0f))
                    labels.add("Start")
                    index += 1f
                }

                for ((date, value) in sorted) {
                    entries.add(com.github.mikephil.charting.data.Entry(index, value.toFloat()))
                    labels.add(date.substring(5))
                    index += 1f
                }

                val dataSet = com.github.mikephil.charting.data.LineDataSet(entries, type.replaceFirstChar { it.uppercaseChar() }).apply {
                    color = android.graphics.Color.parseColor("#FFD600")
                    fillColor = android.graphics.Color.parseColor("#FFD600")
                    setCircleColor(android.graphics.Color.WHITE)
                    valueTextColor = android.graphics.Color.WHITE
                    lineWidth = 2f
                    setDrawValues(true)
                    setDrawCircles(true)
                    setDrawFilled(true)
                    fillAlpha = 50
                }

                chart.apply {
                    data = com.github.mikephil.charting.data.LineData(dataSet)
                    xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels)
                    xAxis.granularity = 1f
                    xAxis.isGranularityEnabled = true
                    xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                    xAxis.textColor = android.graphics.Color.WHITE
                    axisLeft.textColor = android.graphics.Color.WHITE
                    axisRight.isEnabled = false
                    legend.textColor = android.graphics.Color.WHITE
                    description.isEnabled = false
                    invalidate()
                }

            } catch (e: Exception) {
                Log.e("ProfileFragment", "Error loading chart: ${e.message}", e)
                chart.visibility = View.GONE
                noDataText.visibility = View.VISIBLE
            }
        }

        btnDuration.setOnClickListener {
            highlightSelected(btnDuration)
            loadAndShowLineChart("duration")
        }

        btnVolume.setOnClickListener {
            highlightSelected(btnVolume)
            loadAndShowLineChart("volume")
        }

        btnReps.setOnClickListener {
            highlightSelected(btnReps)
            loadAndShowLineChart("reps")
        }

        highlightSelected(btnDuration)
        loadAndShowLineChart("duration")

        return view
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Choose from gallery", "Take a photo")
        AlertDialog.Builder(requireContext())
            .setTitle("Select profile image")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        intent.type = "image/*"
                        galleryLauncher.launch(intent)
                    }
                    1 -> {
                        cameraLauncher.launch(null)
                    }
                }
            }
            .show()
    }
}
