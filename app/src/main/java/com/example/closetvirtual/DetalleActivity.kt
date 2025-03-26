package com.example.closetvirtual

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DetalleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalle)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val imagen = intent.getIntExtra("imagen", R.drawable.ic_launcher_background)
        val nombre = intent.getStringExtra("nombre") ?: "Nombre no disponible"
        val categoria = intent.getStringExtra("categoria") ?: "Categoría no disponible"
        val color = intent.getStringExtra("color") ?: "Color no disponible"
        val estampada = intent.getBooleanExtra("estampada", false)
        val tags = intent.getStringArrayListExtra("tags") ?: arrayListOf()

        val ivPrenda = findViewById<ImageView>(R.id.ivPrenda)
        val tvPrendaNombre = findViewById<TextView>(R.id.tvPrendaNombre)
        val detailContainer = findViewById<LinearLayout>(R.id.detailContainer)
        val tvCategoria = findViewById<TextView>(R.id.tvCategoria)
        val tvColor = findViewById<TextView>(R.id.tvColor)
        val tvEstampado = findViewById<TextView>(R.id.tvEstampado)
        val tvTags = findViewById<TextView>(R.id.tvTags)
        val tvTotalUsos = findViewById<TextView>(R.id.tvTotalUsos)
        val btnEditar = findViewById<Button>(R.id.btnEditar)
        val btnEliminar = findViewById<Button>(R.id.btnEliminar)

        detailContainer.visibility = View.VISIBLE

        ivPrenda.setImageResource(imagen)
        tvPrendaNombre.text = nombre
        tvCategoria.text = "CATEGORIA: $categoria"
        tvColor.text = "COLOR: $color"
        tvEstampado.text = "ESTAMPADO: ${if (estampada) "SÍ" else "N/A"}"

        // aqui es pa que se vea como tagas asi con hashatags
        val formattedTags = tags.joinToString(" ") { "#${it.uppercase()}" }
        tvTags.text = "TAGS: $formattedTags"

        tvTotalUsos.text = "TOTAL VECES USADAS: 34"

        // Aquí sera pa la grafica en la cual sera una imagen provicional nomas para el diseño
        val ivBarChart = findViewById<ImageView>(R.id.ivBarChart)
        ivBarChart.setImageResource(R.drawable.grafica)

        btnEditar.setOnClickListener {
            val intent = Intent(this, EditarPrendaActivity::class.java).apply {
                putExtra("imagen", imagen)
                putExtra("nombre", nombre)
                putExtra("categoria", categoria)
                putExtra("color", color)
                putExtra("estampada", estampada)
                putStringArrayListExtra("tags", ArrayList(tags))
            }
            startActivity(intent)
        }

        btnEliminar.setOnClickListener {
            Toast.makeText(this, "Eliminar: $nombre", Toast.LENGTH_SHORT).show()
        }

    }

    //pa que se mire bonito
        private fun Int.dpToPx(context: Context): Int {
            return (this * context.resources.displayMetrics.density).toInt()
        }

        private fun DisplayMetrics.widthDimen(dp: Int): Int {
            return (widthPixels * dp / 360)
        }

        private fun DisplayMetrics.heightDimen(dp: Int): Int {
            return (heightPixels * dp / 640)
        }
    }
