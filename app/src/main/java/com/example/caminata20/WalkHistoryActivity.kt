package com.example.caminata20

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import org.json.JSONArray

class WalkHistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_walk_history)

        val container = findViewById<LinearLayout>(R.id.containerSessions)
        val prefs = getSharedPreferences("walk_stats", Context.MODE_PRIVATE)
        val sessionsJson = prefs.getString("sessions", "[]")
        val sessionsArray = JSONArray(sessionsJson)

        if (sessionsArray.length() == 0) {
            val emptyText = TextView(this)
            emptyText.text = "Todavía no registraste ninguna caminata."
            emptyText.textSize = 16f
            container.addView(emptyText)
            return
        }

        // Mostrar las más recientes primero
        for (i in sessionsArray.length() - 1 downTo 0) {
            val session = sessionsArray.getJSONObject(i)
            val date = session.getString("date")
            val steps = session.getInt("steps")
            val km = session.getDouble("km")

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

            val innerLayout = LinearLayout(this)
            innerLayout.orientation = LinearLayout.VERTICAL

            val tvDate = TextView(this)
            tvDate.text = date
            tvDate.textSize = 13f
            tvDate.setTextColor(Color.parseColor("#777777"))

            val tvDetails = TextView(this)
            tvDetails.text = "$steps pasos · %.2f km".format(km)
            tvDetails.textSize = 16f
            tvDetails.setTextColor(Color.parseColor("#3F51B5"))

            innerLayout.addView(tvDate)
            innerLayout.addView(tvDetails)
            card.addView(innerLayout)
            container.addView(card)
        }
    }
}