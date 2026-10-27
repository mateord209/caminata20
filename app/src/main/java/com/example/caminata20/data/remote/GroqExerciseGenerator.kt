package com.example.caminata20.data.remote

import com.example.caminata20.domain.model.AiExercise
import com.example.caminata20.domain.model.Outcome
import com.example.caminata20.domain.repository.AiExerciseGenerator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/** Configuración de Groq. Ver docs/SEGURIDAD_GROQ.md (riesgo de la clave en el APK y proxy). */
data class GroqConfig(val apiKey: String, val endpoint: String, val model: String = "openai/gpt-oss-120b")

@Singleton
class GroqExerciseGenerator @Inject constructor(
    private val client: OkHttpClient,
    private val config: GroqConfig,
    @Named("io") private val io: CoroutineDispatcher,
) : AiExerciseGenerator {

    override suspend fun generate(): Outcome<List<AiExercise>> = withContext(io) {
        try {
            val list = parseExercises(extractContent(call(buildPrompt(ENFOQUES.random()))))
            if (list.isEmpty()) Outcome.Failure("No se pudo generar la rutina") else Outcome.Success(list)
        } catch (e: Exception) {
            Outcome.Failure("Error al generar la rutina: ${e.message}")
        }
    }

    private fun call(prompt: String): String {
        val body = JSONObject()
            .put("model", config.model)
            .put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", prompt)))
            .toString().toRequestBody("application/json".toMediaType())
        val req = Request.Builder().url(config.endpoint)
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .post(body).build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw IOException("Error de red: ${resp.code} ${resp.message}")
            return resp.body?.string() ?: throw IOException("Respuesta vacía")
        }
    }

    companion object {
        val ENFOQUES = listOf(
            "cuello y hombros", "espalda baja y cadera", "brazos y muñecas", "piernas y tobillos",
            "columna y postura general", "estiramientos de pie", "estiramientos sentado en la silla",
        )

        fun buildPrompt(enfoque: String) = """
            Generá una rutina de 5 ejercicios de estiramiento para una persona
            que trabaja muchas horas sentada frente a una computadora,
            con foco especial en: $enfoque.
            Variá los ejercicios respecto a rutinas típicas, sé creativo dentro de lo seguro y efectivo.
            Respondé ÚNICAMENTE con un array JSON válido, sin markdown, sin explicaciones,
            con este formato exacto:
            [
              {"name": "Nombre del ejercicio", "category": "cuello|hombros|espalda|piernas|brazos|muñecas",
               "sets": 2, "reps": 10, "instructions": "Instrucción breve de cómo hacerlo"}
            ]
            Los valores de "sets" y "reps" deben ser números enteros razonables.
        """.trimIndent()

        /** Extrae choices[0].message.content de la respuesta OpenAI-compatible. */
        fun extractContent(responseBody: String): String =
            JSONObject(responseBody).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")

        /** Limpia ```json ... ``` y convierte el array en ejercicios. */
        fun parseExercises(content: String): List<AiExercise> {
            val clean = content.replace("```json", "").replace("```", "").trim()
            val arr = JSONArray(clean)
            return (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                AiExercise(
                    name = o.getString("name"),
                    category = o.optString("category", "general"),
                    sets = o.optInt("sets", 1),
                    reps = o.optInt("reps", 10),
                    instructions = o.getString("instructions"),
                )
            }
        }
    }
}
