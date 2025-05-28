package com.nirvana.gymapp.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.Cursor

class ExerciseDatabase(context: Context) : SQLiteOpenHelper(context, "exercises.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        // Create main exercises table
        db.execSQL("""
            CREATE TABLE exercises (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                category TEXT NOT NULL
            );
        """.trimIndent())

        // Create table for selected exercises (used by user)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS selected_exercises (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                category TEXT NOT NULL
            );
        """.trimIndent())

        // Prepopulate default exercises
        val exercises = listOf(
            "Ab Wheel Rollout" to "Core",
            "Arnold Press" to "Shoulders",
            "Barbell Curl" to "Biceps",
            "Barbell Row" to "Back",
            "Bench Press" to "Chest",
            "Bent Over Row" to "Back",
            "Bicep Curl (Dumbbell)" to "Biceps",
            "Bodyweight Squat" to "Legs",
            "Box Jump" to "Cardio",
            "Bulgarian Split Squat" to "Legs",
            "Cable Crossover" to "Chest",
            "Cable Row" to "Back",
            "Calf Raise (Standing)" to "Legs",
            "Chest Fly (Machine or Dumbbell)" to "Chest",
            "Chin-Up" to "Back",
            "Clean and Press" to "Full Body",
            "Concentration Curl" to "Biceps",
            "Crunch" to "Core",
            "Deadlift" to "Back",
            "Decline Bench Press" to "Chest",
            "Dumbbell Fly" to "Chest",
            "Dumbbell Press" to "Chest/Shoulders",
            "Dumbbell Pullover" to "Chest",
            "Dumbbell Row" to "Back",
            "Face Pull" to "Shoulders",
            "Farmer’s Walk" to "Full Body",
            "Front Raise" to "Shoulders",
            "Front Squat" to "Legs",
            "Glute Bridge" to "Core",
            "Goblet Squat" to "Legs",
            "Hack Squat" to "Legs",
            "Hammer Curl" to "Biceps",
            "Hanging Leg Raise" to "Core",
            "Hip Thrust" to "Glutes",
            "Incline Bench Press" to "Chest",
            "Incline Dumbbell Curl" to "Biceps",
            "Jump Rope" to "Cardio",
            "Jump Squat" to "Legs",
            "Kettlebell Swing" to "Full Body",
            "Kickback (Tricep)" to "Triceps",
            "Lateral Raise" to "Shoulders",
            "Lat Pulldown" to "Back",
            "Leg Curl (Machine)" to "Legs",
            "Leg Extension (Machine)" to "Legs",
            "Leg Press" to "Legs",
            "Lunge" to "Legs",
            "Mountain Climbers" to "Cardio",
            "Overhead Press" to "Shoulders",
            "Overhead Tricep Extension" to "Triceps",
            "Pendlay Row" to "Back",
            "Plank" to "Core",
            "Power Clean" to "Full Body",
            "Preacher Curl" to "Biceps",
            "Pull-Up" to "Back",
            "Push Press" to "Shoulders",
            "Push-Up" to "Chest",
            "Rear Delt Fly" to "Shoulders",
            "Romanian Deadlift" to "Hamstrings",
            "Rope Pushdown" to "Triceps",
            "Russian Twist" to "Core",
            "Seated Cable Row" to "Back",
            "Seated Leg Curl" to "Legs",
            "Shoulder Press" to "Shoulders",
            "Side Plank" to "Core",
            "Sit-Up" to "Core",
            "Skull Crusher" to "Triceps",
            "Smith Machine Squat" to "Legs",
            "Snatch" to "Full Body",
            "Split Squat" to "Legs",
            "Step-Up" to "Legs",
            "Sumo Deadlift" to "Glutes",
            "Thruster" to "Full Body",
            "Toe Touch" to "Core",
            "Tricep Dip" to "Triceps",
            "Tricep Kickback" to "Triceps",
            "T-Bar Row" to "Back",
            "Upright Row" to "Shoulders",
            "Wall Sit" to "Legs",
            "Walking Lunge" to "Legs",
            "Weighted Crunch" to "Core",
            "Windshield Wiper" to "Core",
            "Woodchopper" to "Core",
            "Zercher Squat" to "Legs",
            "Zottman Curl" to "Biceps"
        )

        // Insert all exercises into database
        for ((name, category) in exercises) {
            db.execSQL("INSERT INTO exercises (name, category) VALUES (?, ?)", arrayOf(name, category))
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Add selected_exercises table if upgrading from older version
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE IF NOT EXISTS selected_exercises (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, category TEXT)")
        }
    }

    // Get all exercises from the database
    fun getAllExercises(): List<Pair<String, String>> {
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT name, category FROM exercises ORDER BY name", null)
        val list = mutableListOf<Pair<String, String>>()

        // Add each exercise to the list
        while (cursor.moveToNext()) {
            list.add(cursor.getString(0) to cursor.getString(1))
        }

        cursor.close()
        return list
    }

    // Add a user-selected exercise to the table
    fun addSelectedExercise(name: String, category: String) {
        val db = writableDatabase

        // Ensure table exists
        db.execSQL("CREATE TABLE IF NOT EXISTS selected_exercises (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, category TEXT)")

        // Insert selected exercise
        db.execSQL("INSERT INTO selected_exercises (name, category) VALUES (?, ?)", arrayOf(name, category))
    }

    // Get all selected exercises
    fun getSelectedExercises(): List<Pair<String, String>> {
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT name, category FROM selected_exercises", null)
        val list = mutableListOf<Pair<String, String>>()

        // Add each selected exercise to list
        while (cursor.moveToNext()) {
            list.add(cursor.getString(0) to cursor.getString(1))
        }

        cursor.close()
        return list
    }

    // Clear all selected exercises
    fun clearSelectedExercises() {
        val db = writableDatabase
        db.execSQL("DELETE FROM selected_exercises")
    }
}
