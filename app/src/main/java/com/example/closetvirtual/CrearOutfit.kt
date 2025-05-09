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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class CrearOutfit : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_crear_outfit)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.crear_outfit_root)) { v, insets ->
            val sb = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom)
            insets
        }

        val etNombre: EditText = findViewById(R.id.etNombreOutfit)
        val rvPrendas: RecyclerView = findViewById(R.id.rvPrendasSeleccionables)
        val btnAgregar: Button = findViewById(R.id.btnRegistrarNuevaPrenda)
        val btnGuardar: Button = findViewById(R.id.btnGuardarOutfit)

        val adapter = PrendaSelectionAdapter()
        rvPrendas.layoutManager = GridLayoutManager(this, 3)
        rvPrendas.adapter = adapter

        db.collection("prendas").get().addOnSuccessListener { snap ->
            val prendas = snap.documents.mapNotNull { it.toObject(Prenda::class.java) }
            adapter.submitList(prendas)
        }

        btnAgregar.setOnClickListener {
            startActivity(Intent(this, RegisrarPrendaActivity::class.java))
        }

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val seleccionadas = adapter.getSelectedItems()
            if (nombre.isEmpty() || seleccionadas.isEmpty()) {
                Toast.makeText(this, "Nombre y al menos 1 prenda requeridos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val outfit = Outfits(nombre, seleccionadas)
            db.collection("outfits").add(outfit)
                .addOnSuccessListener {
                    Toast.makeText(this, "Outfit guardado", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, TusOutfitsActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error guardando outfit", Toast.LENGTH_SHORT).show()
                }
        }
    }
}