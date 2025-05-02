package com.example.closetvirtual

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
class ConfiguracionUsuarioActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText

    private lateinit var btnConfigurar: Button
    private lateinit var btnCerrarSesion: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_configuracion_usuario)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // 1) Instancias de Firebase
        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()

        // 2) Inicializar vistas
        etFirstName      = findViewById(R.id.etFirstName)
        etLastName       = findViewById(R.id.etLastName)
        etEmail          = findViewById(R.id.etEmail)
        etUsername       = findViewById(R.id.etUsername)
        etPassword       = findViewById(R.id.etPassword)
        etConfirmPassword= findViewById(R.id.etConfirmPassword)

        btnConfigurar    = findViewById(R.id.btnConfigurar)
        btnCerrarSesion  = findViewById(R.id.btnCerrarSesion)

        // 3) Cargar datos al iniciar
        loadUserData()

        // 4) Actualizar datos
        btnConfigurar.setOnClickListener {
            updateUserData()
        }

        // 5) Cerrar sesión
        btnCerrarSesion.setOnClickListener {
            auth.signOut()
            // Limpiar back‑stack para que no regrese al Principal
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "No hay usuario con sesión activa", Toast.LENGTH_LONG).show()
            finish()
            return
        }


        db.collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    etFirstName.setText(doc.getString("nombres"))
                    etLastName.setText(doc.getString("apellidos"))
                    etEmail.setText(doc.getString("email"))
                    etUsername.setText(doc.getString("usuario"))
                    
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar datos: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateUserData() {
        val uid = auth.currentUser?.uid ?: return

        val pass = etPassword.text.toString()
        val confirm = etConfirmPassword.text.toString()
        if (pass.isNotEmpty() && pass != confirm) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = mutableMapOf<String, Any>(
            "nombres" to etFirstName.text.toString().trim(),
            "apellidos" to etLastName.text.toString().trim(),
            "email" to etEmail.text.toString().trim(),
            "usuario" to etUsername.text.toString().trim()
        )
        if (pass.isNotEmpty()) {
            updates["contraseña"] = pass
            auth.currentUser?.updatePassword(pass)
        }

        db.collection("usuarios")
            .document(uid)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Datos actualizados", Toast.LENGTH_SHORT).show()

                // Regresar a la pantalla principal
                val intent = Intent(this, PrincipalActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish() // Opcional: cerrar esta actividad
            }
            .addOnFailureListener {
                Toast.makeText(this, "Fallo al actualizar: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}