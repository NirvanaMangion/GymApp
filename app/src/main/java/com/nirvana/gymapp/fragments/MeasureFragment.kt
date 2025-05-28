package com.nirvana.gymapp.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.nirvana.gymapp.R
import com.nirvana.gymapp.database.UserDatabase
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MeasureFragment : Fragment() {

    private lateinit var historyContainer: LinearLayout
    private lateinit var db: UserDatabase
    private lateinit var userId: String
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Void?>
    private val progressPhotos = mutableListOf<Bitmap>()
    private lateinit var uploadedPhotosText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_measures, container, false)

        db = UserDatabase(requireContext())

        // Retrieve currently logged-in user ID from shared preferences
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        userId = sharedPref.getString("loggedInUser", "") ?: ""

        // Find views by ID
        val weightInput = view.findViewById<EditText>(R.id.weightInput)
        val chestInput = view.findViewById<EditText>(R.id.chestInput)
        val waistInput = view.findViewById<EditText>(R.id.waistInput)
        val armsInput = view.findViewById<EditText>(R.id.armsInput)
        val uploadBtn = view.findViewById<Button>(R.id.uploadBtn)
        val saveBtn = view.findViewById<Button>(R.id.saveBtn)
        historyContainer = view.findViewById(R.id.historyContainer)
        uploadedPhotosText = view.findViewById(R.id.uploadedPhotosText)

        // Populate UI with previous entries from DB
        val previousEntries = db.getMeasurementsForUser(userId)
        for ((timestamp, weight, chest, waist, arms) in previousEntries) {
            addMeasurementToHistory(timestamp, weight, chest, waist, arms)
        }

        // Set up gallery image picker
        galleryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val clipData = result.data?.clipData
            val image = result.data?.data

            try {
                if (clipData != null) {
                    // Multiple images selected
                    for (i in 0 until clipData.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                        progressPhotos.add(bitmap)
                    }
                } else if (image != null) {
                    // Single image selected
                    val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, image)
                    progressPhotos.add(bitmap)
                }
                if (progressPhotos.isNotEmpty()) {
                    uploadedPhotosText.text = "${progressPhotos.size} photo(s) selected"
                    uploadedPhotosText.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                Log.e("MeasureFragment", "Error loading image(s): ${e.message}", e)
            }
        }

        // Set up camera preview for taking a new image
        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let {
                progressPhotos.add(it)
                uploadedPhotosText.text = "${progressPhotos.size} photo(s) selected"
                uploadedPhotosText.visibility = View.VISIBLE
            }
        }

        // Prompt user to choose photo upload method
        uploadBtn.setOnClickListener {
            val options = arrayOf("Choose from gallery", "Take a photo")
            AlertDialog.Builder(requireContext())
                .setTitle("Upload Progress Photo")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> {
                            val intent = Intent(Intent.ACTION_PICK).apply {
                                type = "image/*"
                                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                            }
                            galleryLauncher.launch(intent)
                        }
                        1 -> {
                            cameraLauncher.launch(null)
                        }
                    }
                }
                .show()
        }

        // Save measurement and photos to database
        saveBtn.setOnClickListener {
            val weight = weightInput.text.toString().trim()
            val chest = chestInput.text.toString().trim()
            val waist = waistInput.text.toString().trim()
            val arms = armsInput.text.toString().trim()

            if (weight.isEmpty() || chest.isEmpty() || waist.isEmpty() || arms.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val now = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            val success = db.insertMeasurement(userId, weight, chest, waist, arms, now)

            if (success) {
                for ((index, photo) in progressPhotos.withIndex()) {
                    val filename = "progress_${userId}_${System.currentTimeMillis()}_$index.png"
                    val path = savePhotoToStorage(photo, filename)
                    db.insertProgressPhoto(userId, now, path)
                }

                addMeasurementToHistory(now, weight, chest, waist, arms)
                Toast.makeText(requireContext(), "Entry saved to database.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to save to database.", Toast.LENGTH_SHORT).show()
            }

            weightInput.text.clear()
            chestInput.text.clear()
            waistInput.text.clear()
            armsInput.text.clear()
            progressPhotos.clear()
            uploadedPhotosText.text = ""
            uploadedPhotosText.visibility = View.GONE
        }

        // Dismiss keyboard on outside tap
        view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
            false
        }

        return view
    }

    // Render a log entry to the history section
    private fun addMeasurementToHistory(
        timestamp: String,
        weight: String,
        chest: String,
        waist: String,
        arms: String
    ) {
        val context = requireContext()

        val logCard = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF1E1E1E.toInt())
            setPadding(32, 32, 32, 32)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 32) }
        }

        val topRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val dateView = TextView(context).apply {
            text = timestamp
            setTextColor(0xFFAAAAAA.toInt())
            textSize = 13f
        }

        val spacer = Space(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, 0, 1f)
        }

        // Delete button to remove entry
        val deleteBtn = ImageButton(context).apply {
            setImageResource(R.drawable.binicon)
            background = null
            layoutParams = LinearLayout.LayoutParams(130, 130)
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
            setColorFilter(Color.RED)
            contentDescription = "Delete Entry"
            setOnClickListener {
                AlertDialog.Builder(context)
                    .setTitle("Delete Entry")
                    .setMessage("Are you sure you want to delete this entry?")
                    .setPositiveButton("Delete") { _, _ ->
                        db.deleteMeasurement(userId, timestamp)
                        historyContainer.removeView(logCard)
                        Toast.makeText(context, "Entry deleted", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        topRow.addView(dateView)
        topRow.addView(spacer)
        topRow.addView(deleteBtn)
        logCard.addView(topRow)

        logCard.addView(Space(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 16
            )
        })

        // Add measurement values to card
        val entries = listOf("Weight" to weight, "Chest" to chest, "Waist" to waist, "Arms" to arms)
        for ((label, value) in entries) {
            val unit = if (label == "Weight") "kg" else "cm"
            val fullText = "$label: $value $unit"
            val styledText = android.text.SpannableString(fullText).apply {
                setSpan(android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, label.length, 0)
                setSpan(android.text.style.RelativeSizeSpan(0.8f), fullText.indexOf(unit), fullText.length, 0)
            }

            val textView = TextView(context).apply {
                text = styledText
                textSize = 16f
                setTextColor(0xFFFFFFFF.toInt())
                setPadding(0, 8, 0, 0)
            }
            logCard.addView(textView)
        }

        // Add space before photo section
        logCard.addView(Space(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 48
            )
        })

        // Load and show progress photos
        val photoPaths = db.getPhotosForMeasurement(userId, timestamp)
        if (photoPaths.isNotEmpty()) {
            val scrollView = HorizontalScrollView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val imageRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            for (path in photoPaths) {
                val bitmap = BitmapFactory.decodeFile(path)
                if (bitmap != null) {
                    val imageView = ImageView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(300, 300).apply {
                            setMargins(12, 12, 12, 0)
                        }
                        setImageBitmap(bitmap)
                        scaleType = ImageView.ScaleType.CENTER_CROP

                        setOnClickListener {
                            AlertDialog.Builder(context)
                                .setTitle("Progress Photo")
                                .setView(ImageView(context).apply {
                                    setImageBitmap(bitmap)
                                    scaleType = ImageView.ScaleType.FIT_CENTER
                                    adjustViewBounds = true
                                    setPadding(32, 32, 32, 32)
                                })
                                .setPositiveButton("Close", null)
                                .show()
                        }
                    }
                    imageRow.addView(imageView)
                }
            }

            scrollView.addView(imageRow)
            logCard.addView(scrollView)
        }

        historyContainer.addView(logCard, 0)
    }

    // Save a Bitmap image to internal storage and return file path
    private fun savePhotoToStorage(bitmap: Bitmap, filename: String): String {
        val file = File(requireContext().filesDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file.absolutePath
    }
}