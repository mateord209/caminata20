package com.example.caminata20

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val tvUserName = findViewById<TextView>(R.id.tvUserName)
        val user = auth.currentUser

        tvUserName.text = "Usuario: " + (user?.displayName?.takeIf { it.isNotBlank() }
            ?: user?.email
            ?: "Invitado")

        // Habilita el efecto marquesina (texto se mueve si es muy largo)
        tvUserName.isSelected = true

        // Mostrar totales reales de caminata guardados
        val walkPrefs = getSharedPreferences("walk_stats", Context.MODE_PRIVATE)
        findViewById<TextView>(R.id.tvTotalSteps).text = walkPrefs.getInt("total_steps", 0).toString()
        findViewById<TextView>(R.id.tvTotalKm).text = String.format("%.2f km", walkPrefs.getFloat("total_km", 0f))
        findViewById<TextView>(R.id.tvWeekTotal).text = String.format("%.2f km", walkPrefs.getFloat("week_km", 0f))

        // Botón cerrar sesión
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            val googleSignInClient = GoogleSignIn.getClient(this, gso)
            googleSignInClient.signOut()
            auth.signOut()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Botones de contenido
        findViewById<Button>(R.id.btnStartWalk).setOnClickListener {
            startActivity(Intent(this, WalkActivity::class.java))
        }
        findViewById<Button>(R.id.btnWalkHistory).setOnClickListener {
            startActivity(Intent(this, WalkHistoryActivity::class.java))
        }
        findViewById<Button>(R.id.btnExercises).setOnClickListener {
            Toast.makeText(this, "Ejercicios (próximamente)", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.btnExercisesAI).setOnClickListener {
            Toast.makeText(this, "Ejercicios con IA (próximamente)", Toast.LENGTH_SHORT).show()
        }

        // Barra de navegación inferior
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            // Ya estamos en Home
        }
        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            Toast.makeText(this, "Perfil (próximamente)", Toast.LENGTH_SHORT).show()
        }
        findViewById<LinearLayout>(R.id.navMap).setOnClickListener {
            Toast.makeText(this, "Mapa (próximamente)", Toast.LENGTH_SHORT).show()
        }
        findViewById<LinearLayout>(R.id.navProgress).setOnClickListener {
            Toast.makeText(this, "Progreso (próximamente)", Toast.LENGTH_SHORT).show()
        }
        findViewById<LinearLayout>(R.id.navAI).setOnClickListener {
            Toast.makeText(this, "IA (próximamente)", Toast.LENGTH_SHORT).show()
        }
        findViewById<LinearLayout>(R.id.navSettings).setOnClickListener {
            startActivity(Intent(this, ReminderActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresca los totales cada vez que volvemos a esta pantalla (por si se actualizaron)
        val walkPrefs = getSharedPreferences("walk_stats", Context.MODE_PRIVATE)
        findViewById<TextView>(R.id.tvTotalSteps).text = walkPrefs.getInt("total_steps", 0).toString()
        findViewById<TextView>(R.id.tvTotalKm).text = String.format("%.2f km", walkPrefs.getFloat("total_km", 0f))
        findViewById<TextView>(R.id.tvWeekTotal).text = String.format("%.2f km", walkPrefs.getFloat("week_km", 0f))
    }
}