package com.example.closetvirtual


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
class LoginActivity : AppCompatActivity() {
    // Vistas
    private lateinit var etIdentifier: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvNoAccount: TextView

    // ViewModel
    private lateinit var vm: UsuarioViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etIdentifier = findViewById(R.id.etIdentifier)
        etPassword   = findViewById(R.id.etPassword)
        btnLogin     = findViewById(R.id.btnLogin)
        tvNoAccount  = findViewById(R.id.tvNoAccount)

        vm = ViewModelProvider(this).get(UsuarioViewModel::class.java)

        btnLogin.setOnClickListener {
            val identifier = etIdentifier.text.toString().trim()
            val pass       = etPassword.text.toString()
            vm.loginUser(identifier, pass)
        }

        vm.loginResult.observe(this) { result ->
            when (result) {
                is AuthResult.Loading -> btnLogin.isEnabled = false
                is AuthResult.Success -> {
                    startActivity(Intent(this, PrincipalActivity::class.java))
                    finish()
                }
                is AuthResult.Error -> {
                    btnLogin.isEnabled = true
                    Toast.makeText(this, "Login falló: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        tvNoAccount.setOnClickListener {
            startActivity(Intent(this, RegistrarActivity::class.java))
        }
    }
}