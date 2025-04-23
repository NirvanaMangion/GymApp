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
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.nirvana.gymapp.R
import com.nirvana.gymapp.database.UserDatabase
import java.text.SimpleDateFormat
import java.util.*

class MeasureFragment : Fragment() {

    private lateinit var historyContainer: LinearLayout
    private lateinit var db: UserDatabase
    private lateinit var userId: String
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Void?>
    private val progressPhotos = mutableListOf<Bitmap>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_measures, container, false)

        db = UserDatabase(requireContext())
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        userId = sharedPref.getString("loggedInUser", "") ?: ""

        val weightInput = view.findViewById<EditText>(R.id.weightInput)
        val chestInput = view.findViewById<EditText>(R.id.chestInput)
        val waistInput = view.findViewById<EditText>(R.id.waistInput)
        val armsInput = view.findViewById<EditText>(R.id.armsInput)
        val uploadBtn = view.findViewById<Button>(R.id.uploadBtn)
        val saveBtn = view.findViewById<Button>(R.id.saveBtn)
        historyContainer = view.findViewById(R.id.historyContainer)

        galleryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val clipData = result.data?.clipData
            val image = result.data?.data

            try {
                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                        progressPhotos.add(bitmap)
                    }
                } else if (image != null) {
                    val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, image)
                    progressPhotos.add(bitmap)
                }
            } catch (e: Exception) {
                Log.e("MeasureFragment", "Error loading image(s): ${e.message}", e)
            }
        }

        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let {
                progressPhotos.add(it)
            }
        }

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

            val success = db.insertMeasurement(
                username = userId,
                weight = weight,
                chest = chest,
                waist = waist,
                arms = arms,
                timestamp = now
            )

            if (success) {
                Toast.makeText(requireContext(), "Entry saved to database.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to save to database.", Toast.LENGTH_SHORT).show()
            }

            val logCard = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(0xFF1E1E1E.toInt())
                setPadding(32, 24, 32, 24)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, 24)
                layoutParams = params
            }

            val dateView = TextView(requireContext()).apply {
                text = now
                setTextColor(0xFFAAAAAA.toInt())
                textSize = 12f
            }

            val details = TextView(requireContext()).apply {
                text = """
                    Weight: $weight kg
                    Chest: $chest cm
                    Waist: $waist cm
                    Arms: $arms cm
                """.trimIndent()
                setTextColor(0xFFFFFFFF.toInt())
                textSize = 14f
                setPadding(0, 8, 0, 0)
            }

            logCard.addView(dateView)
            logCard.addView(details)

            if (progressPhotos.isNotEmpty()) {
                val photoScroll = HorizontalScrollView(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                val imageRow = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                }

                for (bitmap in progressPhotos) {
                    val imageView = ImageView(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(200, 200).apply {
                            setMargins(8, 8, 8, 8)
                        }
                        setImageBitmap(bitmap)
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                    imageRow.addView(imageView)
                }

                photoScroll.addView(imageRow)
                logCard.addView(photoScroll)
            }

            historyContainer.addView(logCard, 0)

            weightInput.text.clear()
            chestInput.text.clear()
            waistInput.text.clear()
            armsInput.text.clear()
            progressPhotos.clear()
        }

        view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
            false
        }

        return view
    }
}
