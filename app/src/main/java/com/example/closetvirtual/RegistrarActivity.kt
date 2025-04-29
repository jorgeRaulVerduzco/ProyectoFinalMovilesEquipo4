package com.example.closetvirtual
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider

class RegistrarActivity : AppCompatActivity() {
    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegistrar: Button

    // ViewModel
    private lateinit var vm: UsuarioViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registrar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar vistas
        etFirstName       = findViewById(R.id.etFirstName)
        etLastName        = findViewById(R.id.etLastName)
        etEmail           = findViewById(R.id.etEmail)
        etUsername        = findViewById(R.id.etUsername)
        etPassword        = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegistrar      = findViewById(R.id.btnRegistrar)

        // Inicializar ViewModel
        vm = ViewModelProvider(this).get(UsuarioViewModel::class.java)

        btnRegistrar.setOnClickListener {
            val u = Usuario(
                nombres    = etFirstName.text.toString().trim(),
                apellidos  = etLastName.text.toString().trim(),
                email      = etEmail.text.toString().trim(),
                usuario    = etUsername.text.toString().trim(),
                contraseña = etPassword.text.toString()
            )
            vm.registerUser(u, etConfirmPassword.text.toString())
        }

        vm.registerResult.observe(this) { result ->
            when (result) {
                is AuthResult.Loading -> {
                    btnRegistrar.isEnabled = false
                }
                is AuthResult.Success -> {
                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                is AuthResult.Error -> {
                    btnRegistrar.isEnabled = true
                    Toast.makeText(this, "Error: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}