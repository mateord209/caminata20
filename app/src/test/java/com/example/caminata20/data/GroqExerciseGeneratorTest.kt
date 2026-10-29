package com.example.caminata20.data

import com.example.caminata20.data.remote.GroqConfig
import com.example.caminata20.data.remote.GroqExerciseGenerator
import com.example.caminata20.data.remote.IdTokenProvider
import com.example.caminata20.domain.model.Outcome
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GroqExerciseGeneratorTest {
    private var lastRequest: Request? = null

    private fun client(content: String, code: Int = 200) = OkHttpClient.Builder().addInterceptor(Interceptor { chain ->
        lastRequest = chain.request()
        val body = JSONObject().put("choices", JSONArray().put(JSONObject().put("message", JSONObject().put("content", content)))).toString()
        Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(code).message("x")
            .body(body.toResponseBody("application/json".toMediaType())).build()
    }).build()

    private val routine = """```json
        [{"name":"Giro de cuello","category":"cuello","sets":2,"reps":10,"instructions":"Gira suave"}]
        ```"""

    @Test
    fun `parsea la respuesta y llama directo a Groq con la clave`() = runTest {
        val gen = GroqExerciseGenerator(client(routine), GroqConfig("KEY", "https://api.groq.com/x"), { null }, StandardTestDispatcher(testScheduler))
        val r = gen.generate()
        assertTrue(r is Outcome.Success)
        assertEquals("Giro de cuello", (r as Outcome.Success).value.single().name)
        assertEquals("Bearer KEY", lastRequest!!.header("Authorization"))
        assertEquals("api.groq.com", lastRequest!!.url.host)
    }

    @Test
    fun `con proxy usa el ID token y nunca envia la clave`() = runTest {
        val cfg = GroqConfig("KEY", "https://api.groq.com/x", proxyUrl = "https://proxy.example.com/groq")
        val gen = GroqExerciseGenerator(client(routine), cfg, IdTokenProvider { "ID_TOKEN" }, StandardTestDispatcher(testScheduler))
        gen.generate()
        assertEquals("Bearer ID_TOKEN", lastRequest!!.header("Authorization"))
        assertEquals("proxy.example.com", lastRequest!!.url.host)
    }

    @Test
    fun `error HTTP o contenido invalido devuelven Failure`() = runTest {
        val cfg = GroqConfig("KEY", "https://api.groq.com/x")
        val d = StandardTestDispatcher(testScheduler)
        assertTrue(GroqExerciseGenerator(client(routine, 500), cfg, { null }, d).generate() is Outcome.Failure)
        assertTrue(GroqExerciseGenerator(client("no es json"), cfg, { null }, d).generate() is Outcome.Failure)
        assertTrue(GroqExerciseGenerator(client("[]"), cfg, { null }, d).generate() is Outcome.Failure)
    }
}
