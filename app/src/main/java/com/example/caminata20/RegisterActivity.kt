package com.example.caminata20

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val ivTogglePassword = findViewById<ImageView>(R.id.ivTogglePassword)
        val ivToggleConfirmPassword = findViewById<ImageView>(R.id.ivToggleConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvGoToLogin = findViewById<TextView>(R.id.tvGoToLogin)

        // Restricción: solo letras, tildes, ñ y espacios en el nombre
        etName.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
            val regex = Regex("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]*")
            if (regex.matches(source)) source else ""
        })

        // Mostrar/Ocultar contraseña
        etPassword.transformationMethod = AsteriskPasswordTransformationMethod()
        var isPasswordVisible = false
        ivTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.transformationMethod = null
                ivTogglePassword.setImageResource(R.drawable.ic_eye_visible)
            } else {
                etPassword.transformationMethod = AsteriskPasswordTransformationMethod()
                ivTogglePassword.setImageResource(R.drawable.ic_eye_hidden)
            }
            etPassword.setSelection(etPassword.text.length)
        }

        // Mostrar/Ocultar confirmar contraseña
        etConfirmPassword.transformationMethod = AsteriskPasswordTransformationMethod()
        var isConfirmPasswordVisible = false
        ivToggleConfirmPassword.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            if (isConfirmPasswordVisible) {
                etConfirmPassword.transformationMethod = null
                ivToggleConfirmPassword.setImageResource(R.drawable.ic_eye_visible)
            } else {
                etConfirmPassword.transformationMethod = AsteriskPasswordTransformationMethod()
                ivToggleConfirmPassword.setImageResource(R.drawable.ic_eye_hidden)
            }
            etConfirmPassword.setSelection(etConfirmPassword.text.length)
        }

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Completá todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()
                        user?.updateProfile(profileUpdates)

                        Toast.makeText(this, "Cuenta creada con éxito", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            "Error: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        tvGoToLogin.setOnClickListener {
            finish()
        }
    }
}