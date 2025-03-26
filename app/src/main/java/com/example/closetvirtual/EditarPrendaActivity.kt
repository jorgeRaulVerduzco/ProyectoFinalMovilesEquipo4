package com.example.closetvirtual

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class EditarPrendaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editar_prenda)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etNombrePrenda = findViewById<EditText>(R.id.etNombrePrenda)
        val etColor = findViewById<EditText>(R.id.etColor)
        val etTags = findViewById<EditText>(R.id.etTags)
        val spinner = findViewById<Spinner>(R.id.ListaDeCategoria)
        val btnEditarPrenda = findViewById<Button>(R.id.btnEditarPrenda)

        val categorias = arrayOf("Top", "Bottom", "Bodysuit", "Zapatos", "Accesorios")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categorias)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        val nombre = intent.getStringExtra("nombre") ?: ""
        val color = intent.getStringExtra("color") ?: ""
        val categoria = intent.getStringExtra("categoria") ?: ""
        val tags = intent.getStringArrayListExtra("tags") ?: arrayListOf()

        etNombrePrenda.setText(nombre)
        etColor.setText(color)

        val formattedTags = tags.joinToString(" ") { it }
        etTags.setText(formattedTags)

        val categoriaIndex = categorias.indexOf(categoria)
        if (categoriaIndex != -1) {
            spinner.setSelection(categoriaIndex)
        }

        btnEditarPrenda.setOnClickListener {

            val intent = Intent(this, DetalleActivity::class.java)
            startActivity(intent)
        }
    }
}