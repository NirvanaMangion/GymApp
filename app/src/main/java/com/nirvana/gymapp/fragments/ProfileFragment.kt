package com.nirvana.gymapp.fragments

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import java.io.File
import java.io.IOException

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

        galleryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val dataUri = result.data?.data
                if (dataUri != null) {
                    try {
                        val bitmap = loadBitmapSafelyFromUri(dataUri)
                        if (bitmap != null) {
                            val savedUri = saveBitmapToGallery(bitmap)
                            if (savedUri != null) {
                                profileImageView.setImageBitmap(bitmap)
                                db.updateProfileImage(userId, savedUri.toString())
                            }
                        } else {
                            Toast.makeText(requireContext(), "Unsupported image format", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("ProfileFragment", "Error loading gallery image", e)
                        Toast.makeText(requireContext(), "Error loading image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicturePreview()
        ) { bitmap: Bitmap? ->
            bitmap?.let {
                val uri = saveBitmapToGallery(it)
                try {
                    profileImageView.setImageBitmap(it)
                    if (uri != null) db.updateProfileImage(userId, uri.toString())
                } catch (e: Exception) {
                    Log.e("ProfileFragment", "Failed to show image from camera", e)
                    Toast.makeText(requireContext(), "Error showing camera image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        db = UserDatabase(requireContext())
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        userId = sharedPref.getString("loggedInUser", "Username") ?: "Username"

        profileImageView = view.findViewById(R.id.profileImageView)
        usernameTextView = view.findViewById(R.id.usernameTextView)
        chart = view.findViewById(R.id.lineChart)
        noDataText = view.findViewById(R.id.tvNoData)

        val btnDuration = view.findViewById<Button>(R.id.btnDuration)
        val btnVolume = view.findViewById<Button>(R.id.btnVolume)
        val btnReps = view.findViewById<Button>(R.id.btnReps)
        val btnWorkoutHistory = view.findViewById<Button>(R.id.btnWorkoutHistory)
        val btnMeasures = view.findViewById<Button>(R.id.btnMeasures)

        usernameTextView.text = userId

        val savedUri = db.getProfileImage(userId)
        if (!savedUri.isNullOrEmpty()) {
            try {
                val bitmap = loadBitmapSafelyFromUri(Uri.parse(savedUri))
                bitmap?.let { profileImageView.setImageBitmap(it) }
            } catch (e: Exception) {
                Log.e("ProfileFragment", "Error loading saved image", e)
                profileImageView.setImageResource(R.drawable.profileicon)
            }
        } else {
            profileImageView.setImageResource(R.drawable.profileicon)
        }

        profileImageView.setOnClickListener {
            showImageSourceDialog()
        }

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

        fun highlightSelected(selected: Button) {
            val selectedColor = "#FFD600"
            val defaultColor = "#333333"
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

    private fun loadBitmapSafelyFromUri(uri: Uri): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            requireContext().contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            val inputStream2 = requireContext().contentResolver.openInputStream(uri)
            inputStream2?.use {
                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = calculateInSampleSize(options, 512, 512)
                    inJustDecodeBounds = false
                }
                BitmapFactory.decodeStream(it, null, decodeOptions)
            }
        } catch (e: Exception) {
            Log.e("ProfileFragment", "loadBitmapSafelyFromUri: ${e.message}", e)
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun saveBitmapToGallery(bitmap: Bitmap): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "profile_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Strive")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val resolver = requireContext().contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        return try {
            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
            uri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
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
