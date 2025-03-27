package com.example.closetvirtual

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RegistroDIario : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_diario)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val btnCrear = findViewById<Button>(R.id.btnCrearRegistroDiario)
        btnCrear.setOnClickListener {
            val intent = Intent(this, TusOutfitsActivity::class.java)
            startActivity(intent)
        }
        findViewById<TextView>(R.id.btnRegistrarNuevaPrenda).setOnClickListener {
            val intent = Intent(this, RegisrarPrendaActivity::class.java)
            startActivity(intent)
        }
    }
}