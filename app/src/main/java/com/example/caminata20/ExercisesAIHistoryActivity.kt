package com.example.caminata20

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import org.json.JSONArray

class ExercisesAIHistoryActivity : AppCompatActivity() {

    private lateinit var prefs: android.content.SharedPreferences
    private val checkBoxes = mutableListOf<Pair<CheckBox, Int>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercises_ai_history)

        prefs = getSharedPreferences("ai_exercises_stats", MODE_PRIVATE)
        loadList()

        findViewById<android.widget.Button>(R.id.btnDeleteSelected).setOnClickListener {
            deleteSelected()
        }
    }

    private fun loadList() {
        val container = findViewById<LinearLayout>(R.id.containerRoutines)
        container.removeAllViews()
        checkBoxes.clear()

        val routinesJson = prefs.getString("routines", "[]")
        val routinesArray = JSONArray(routinesJson)

        if (routinesArray.length() == 0) {
            val emptyText = TextView(this)
            emptyText.text = "Todavía no completaste ninguna rutina de estiramientos."
            emptyText.textSize = 16f
            container.addView(emptyText)
            return
        }

        for (i in routinesArray.length() - 1 downTo 0) {
            val routine = routinesArray.getJSONObject(i)
            val date = routine.getString("date")
            val exercisesArray = routine.getJSONArray("exercises")

            val card = CardView(this)
            val cardParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            cardParams.setMargins(0, 0, 0, 12)
            card.layoutParams = cardParams
            card.radius = 12f
            card.cardElevation = 4f
            card.setContentPadding(16, 16, 16, 16)

            val rowLayout = LinearLayout(this)
            rowLayout.orientation = LinearLayout.HORIZONTAL
            rowLayout.gravity = Gravity.TOP

            val checkBox = CheckBox(this)
            checkBoxes.add(Pair(checkBox, i))

            val innerLayout = LinearLayout(this)
            innerLayout.orientation = LinearLayout.VERTICAL
            val innerParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            innerLayout.layoutParams = innerParams

            val tvDate = TextView(this)
            tvDate.text = date
            tvDate.textSize = 13f
            tvDate.setTextColor(Color.parseColor("#777777"))
            innerLayout.addView(tvDate)

            for (j in 0 until exercisesArray.length()) {
                val ex = exercisesArray.getJSONObject(j)
                val tvEx = TextView(this)
                tvEx.text = "• ${ex.getString("name")} — ${ex.getInt("sets")}x${ex.getInt("reps")}"
                tvEx.textSize = 15f
                tvEx.setTextColor(Color.parseColor("#3F51B5"))
                innerLayout.addView(tvEx)
            }

            rowLayout.addView(checkBox)
            rowLayout.addView(innerLayout)
            card.addView(rowLayout)
            container.addView(card)
        }
    }

    private fun deleteSelected() {
        val indicesToDelete = checkBoxes.filter { it.first.isChecked }.map { it.second }.sortedDescending()

        if (indicesToDelete.isEmpty()) {
            Toast.makeText(this, "No seleccionaste ninguna", Toast.LENGTH_SHORT).show()
            return
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Confirmar eliminación")
            .setMessage("¿Estás seguro de eliminar ${indicesToDelete.size} rutina(s)? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                val routinesJson = prefs.getString("routines", "[]")
                val routinesArray = JSONArray(routinesJson)
                val newArray = JSONArray()

                for (i in 0 until routinesArray.length()) {
                    if (!indicesToDelete.contains(i)) {
                        newArray.put(routinesArray.getJSONObject(i))
                    }
                }

                prefs.edit().putString("routines", newArray.toString()).apply()
                Toast.makeText(this, "Eliminada(s) ${indicesToDelete.size}", Toast.LENGTH_SHORT).show()
                loadList()
            }
            .setNegativeButton("Cancelar", null)
            .setCancelable(true)
            .show()
    }
}