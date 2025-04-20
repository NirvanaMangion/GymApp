package com.nirvana.gymapp.fragments

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.nirvana.gymapp.R
import com.nirvana.gymapp.database.UserDatabase

class ProfileFragment : Fragment() {

    private lateinit var db: UserDatabase
    private lateinit var userId: String
    private lateinit var chart: LineChart
    private lateinit var noDataText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        db = UserDatabase(requireContext())

        // ✅ Load logged-in username from SharedPreferences
        val usernameText = view.findViewById<TextView>(R.id.usernameTextView)
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val loggedInUser = sharedPref.getString("loggedInUser", "Username") ?: "Username"
        userId = loggedInUser // Also used for chart query
        usernameText.text = loggedInUser

        val btnDuration = view.findViewById<Button>(R.id.btnDuration)
        val btnVolume = view.findViewById<Button>(R.id.btnVolume)
        val btnReps = view.findViewById<Button>(R.id.btnReps)

        chart = view.findViewById(R.id.lineChart)
        noDataText = view.findViewById(R.id.tvNoData)

        val selectedColor = "#FFD600"
        val defaultColor = "#333333"

        fun highlightSelected(selected: Button) {
            val allButtons = listOf(btnDuration, btnVolume, btnReps)
            allButtons.forEach { button ->
                if (button == selected) {
                    button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor(selectedColor)))
                    button.setTextColor(Color.BLACK)
                } else {
                    button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor(defaultColor)))
                    button.setTextColor(Color.WHITE)
                }
            }
        }

        fun loadAndShowLineChart(type: String) {
            try {
                val entries = mutableListOf<Entry>()
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
                    entries.add(Entry(index, 0f))
                    labels.add("Start")
                    index += 1f
                }

                for ((date, value) in sorted) {
                    entries.add(Entry(index, value.toFloat()))
                    labels.add(date.substring(5)) // MM-DD
                    index += 1f
                }

                val dataSet = LineDataSet(entries, type.replaceFirstChar { it.uppercaseChar() }).apply {
                    color = Color.parseColor("#FFD600")
                    fillColor = Color.parseColor("#FFD600")
                    setCircleColor(Color.WHITE)
                    valueTextColor = Color.WHITE
                    lineWidth = 2f
                    setDrawValues(true)
                    setDrawCircles(true)
                    setDrawFilled(true)
                    fillAlpha = 50
                }

                chart.apply {
                    data = LineData(dataSet)
                    xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                    xAxis.granularity = 1f
                    xAxis.isGranularityEnabled = true
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.textColor = Color.WHITE
                    axisLeft.textColor = Color.WHITE
                    axisRight.isEnabled = false
                    legend.textColor = Color.WHITE
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
}
