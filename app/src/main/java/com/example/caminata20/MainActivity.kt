package com.example.caminata20

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

        // Botones de contenido (placeholders por ahora)
        findViewById<Button>(R.id.btnStartWalk).setOnClickListener {
            Toast.makeText(this, "Iniciar caminata (próximamente)", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.btnWalkHistory).setOnClickListener {
            Toast.makeText(this, "Historial de recorridos (próximamente)", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.btnExercises).setOnClickListener {
            Toast.makeText(this, "Ejercicios (próximamente)", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.btnExercisesAI).setOnClickListener {
            Toast.makeText(this, "Ejercicios con IA (próximamente)", Toast.LENGTH_SHORT).show()
        }

        // Barra de navegación inferior (placeholders)
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            // Ya estamos en Home
        }
        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            Toast.makeText(this, "Perfil (próximamente)", Toast.LENGTH_SHORT).show()
        }
        findViewById<LinearLayout>(R.id.navTimer).setOnClickListener {
            Toast.makeText(this, "Alarmas (próximamente)", Toast.LENGTH_SHORT).show()
        }
        findViewById<LinearLayout>(R.id.navProgress).setOnClickListener {
            Toast.makeText(this, "Progreso (próximamente)", Toast.LENGTH_SHORT).show()
        }
        findViewById<LinearLayout>(R.id.navAI).setOnClickListener {
            Toast.makeText(this, "IA (próximamente)", Toast.LENGTH_SHORT).show()
        }
        findViewById<LinearLayout>(R.id.navSettings).setOnClickListener {
            Toast.makeText(this, "Ajustes (próximamente)", Toast.LENGTH_SHORT).show()
        }
    }
}