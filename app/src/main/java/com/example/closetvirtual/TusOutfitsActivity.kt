package com.example.closetvirtual

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class TusOutfitsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tus_outfits)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_tus_outfits)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        findViewById<Button>(R.id.btnCrearOutfit).setOnClickListener {
            val intent = Intent(this, CrearOutfit::class.java)
            startActivity(intent)
        }
        findViewById<TextView>(R.id.tvOutfitCasual).setOnClickListener {
            val intent = Intent(this, OutfitSeleccionado::class.java)
            startActivity(intent)
        }
        findViewById<TextView>(R.id.tvOutfitInvierno).setOnClickListener {
            val intent = Intent(this, OutfitSeleccionado::class.java)
            startActivity(intent)
        }
        findViewById<TextView>(R.id.tvOutfitElegante).setOnClickListener {
            val intent = Intent(this, OutfitSeleccionado::class.java)
            startActivity(intent)
        }
        findViewById<TextView>(R.id.tvOutfitFormalComodo).setOnClickListener {
            val intent = Intent(this, OutfitSeleccionado::class.java)
            startActivity(intent)
        }
    }
}