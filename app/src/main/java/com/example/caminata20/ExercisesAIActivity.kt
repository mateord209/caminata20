package com.example.caminata20

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

data class ExerciseItem(
    val name: String,
    val category: String,
    val sets: Int,
    val reps: Int,
    val instructions: String
)

class ExercisesAIActivity : AppCompatActivity() {

    private var exercises: List<ExerciseItem> = emptyList()
    private var currentIndex = 0

    private lateinit var btnGenerate: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var exerciseContainer: android.widget.LinearLayout
    private lateinit var tvProgress: TextView
    private lateinit var tvEmoji: TextView
    private lateinit var tvExerciseName: TextView
    private lateinit var tvSetsReps: TextView
    private lateinit var tvInstructions: TextView
    private lateinit var btnNext: Button

    private val client = OkHttpClient()

    private fun emojiForCategory(category: String): String {
        return when (category.lowercase()) {
            "cuello" -> "🙆"
            "hombros" -> "🤸"
            "espalda" -> "🧘"
            "piernas" -> "🦵"
            "brazos" -> "💪"
            "muñecas" -> "🤲"
            else -> "🤾"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercises_ai)

        btnGenerate = findViewById(R.id.btnGenerate)
        progressBar = findViewById(R.id.progressBar)
        exerciseContainer = findViewById(R.id.exerciseContainer)
        tvProgress = findViewById(R.id.tvProgress)
        tvEmoji = findViewById(R.id.tvEmoji)
        tvExerciseName = findViewById(R.id.tvExerciseName)
        tvSetsReps = findViewById(R.id.tvSetsReps)
        tvInstructions = findViewById(R.id.tvInstructions)
        btnNext = findViewById(R.id.btnNext)

        btnGenerate.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            exerciseContainer.visibility = View.GONE
            btnGenerate.isEnabled = false

            lifecycleScope.launch {
                try {
                    val jsonText = withContext(Dispatchers.IO) { callGroq() }
                    val cleanJson = jsonText
                        .replace("```json", "")
                        .replace("```", "")
                        .trim()

                    val jsonArray = JSONArray(cleanJson)
                    val list = mutableListOf<ExerciseItem>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        list.add(
                            ExerciseItem(
                                name = obj.getString("name"),
                                category = obj.optString("category", "general"),
                                sets = obj.optInt("sets", 1),
                                reps = obj.optInt("reps", 10),
                                instructions = obj.getString("instructions")
                            )
                        )
                    }

                    exercises = list
                    currentIndex = 0
                    if (exercises.isNotEmpty()) {
                        exerciseContainer.visibility = View.VISIBLE
                        showExercise(currentIndex)
                    } else {
                        Toast.makeText(this@ExercisesAIActivity, "No se pudo generar la rutina", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this@ExercisesAIActivity,
                        "Error al generar la rutina: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                } finally {
                    progressBar.visibility = View.GONE
                    btnGenerate.isEnabled = true
                }
            }
        }

        btnNext.setOnClickListener {
            currentIndex++
            if (currentIndex < exercises.size) {
                showExercise(currentIndex)
            } else {
                saveRoutineToHistory()
                Toast.makeText(this, "¡Rutina completa! Bien hecho 💪", Toast.LENGTH_LONG).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
        }
    }

    private fun callGroq(): String {
        val enfoques = listOf(
            "cuello y hombros",
            "espalda baja y cadera",
            "brazos y muñecas",
            "piernas y tobillos",
            "columna y postura general",
            "estiramientos de pie",
            "estiramientos sentado en la silla"
        )
        val enfoqueElegido = enfoques.random()

        val prompt = """
            Generá una rutina de 5 ejercicios de estiramiento para una persona 
            que trabaja muchas horas sentada frente a una computadora, 
            con foco especial en: $enfoqueElegido.
            Variá los ejercicios respecto a rutinas típicas, sé creativo dentro de lo seguro y efectivo.
            Respondé ÚNICAMENTE con un array JSON válido, sin markdown, sin explicaciones,
            con este formato exacto:
            [
              {"name": "Nombre del ejercicio", "category": "cuello|hombros|espalda|piernas|brazos|muñecas", 
               "sets": 2, "reps": 10, "instructions": "Instrucción breve de cómo hacerlo"}
            ]
            Los valores de "sets" y "reps" deben ser números enteros razonables.
        """.trimIndent()

        val bodyJson = JSONObject().apply {
            put("model", "openai/gpt-oss-120b")
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
        }

        val mediaType = "application/json".toMediaType()
        val requestBody = bodyJson.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/chat/completions")
            .addHeader("Authorization", "Bearer ${BuildConfig.GROQ_API_KEY}")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Error de red: ${response.code} ${response.message}")
            }
            val responseBody = response.body?.string() ?: throw IOException("Respuesta vacía")
            val responseJson = JSONObject(responseBody)
            return responseJson
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
        }
    }

    private fun showExercise(index: Int) {
        val exercise = exercises[index]
        tvProgress.text = "Ejercicio ${index + 1} de ${exercises.size}"
        tvEmoji.text = emojiForCategory(exercise.category)
        tvExerciseName.text = exercise.name
        tvSetsReps.text = "${exercise.sets} series x ${exercise.reps} repeticiones"
        tvInstructions.text = exercise.instructions
        btnNext.text = if (index == exercises.size - 1) "Finalizar rutina" else "Completado, siguiente"
    }

    private fun saveRoutineToHistory() {
        val prefs = getSharedPreferences("ai_exercises_stats", MODE_PRIVATE)
        val sessionsJson = prefs.getString("routines", "[]")
        val sessionsArray = JSONArray(sessionsJson)

        val routineObj = JSONObject()
        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy hh:mm a", java.util.Locale.getDefault())
        routineObj.put("date", dateFormat.format(java.util.Date()))

        val exercisesArray = JSONArray()
        for (ex in exercises) {
            val exObj = JSONObject()
            exObj.put("name", ex.name)
            exObj.put("category", ex.category)
            exObj.put("sets", ex.sets)
            exObj.put("reps", ex.reps)
            exercisesArray.put(exObj)
        }
        routineObj.put("exercises", exercisesArray)
        sessionsArray.put(routineObj)

        prefs.edit().putString("routines", sessionsArray.toString()).apply()
    }
}