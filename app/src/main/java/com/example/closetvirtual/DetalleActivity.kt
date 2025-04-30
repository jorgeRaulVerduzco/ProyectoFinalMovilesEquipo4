package com.example.closetvirtual


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider

class DetalleActivity : AppCompatActivity() {
    private lateinit var vm: PrendaViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalle)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        vm = ViewModelProvider(this).get(PrendaViewModel::class.java)
        val prendaId = intent.getStringExtra("prendaId")
        if (prendaId != null) {
            vm.obtenerPrendaPorId(prendaId) { prenda: Prenda? ->
                prenda?.let { updateUIWithPrenda(it) }
            }
        }

        findViewById<Button>(R.id.btnEditar).setOnClickListener {
            // Implementar edición...
        }
        findViewById<Button>(R.id.btnEliminar).setOnClickListener {
            Toast.makeText(this, "Eliminar prenda", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUIWithPrenda(prenda: Prenda) {
        runOnUiThread {
            val ivPrenda    = findViewById<ImageView>(R.id.ivPrenda)
            val tvNombre    = findViewById<TextView>(R.id.tvPrendaNombre)
            val tvCategoria = findViewById<TextView>(R.id.tvCategoria)
            val tvColor     = findViewById<TextView>(R.id.tvColor)
            val tvEstampado = findViewById<TextView>(R.id.tvEstampado)
            val tvTags      = findViewById<TextView>(R.id.tvTags)
            val tvUsos      = findViewById<TextView>(R.id.tvTotalUsos)
            val ivGrafica   = findViewById<ImageView>(R.id.ivBarChart)

            if (prenda.imagen.isNotEmpty()) {
                ivPrenda.setImageURI(Uri.parse(prenda.imagen))
            }
            tvNombre.text    = prenda.nombre
            tvCategoria.text = "CATEGORÍA: ${prenda.categoria}"
            tvColor.text     = "COLOR: ${prenda.color}"
            tvEstampado.text = "ESTAMPADO: ${if (prenda.estampada) "SÍ" else "N/A"}"
            tvTags.text      = prenda.tags.joinToString(" ") { "#${it.uppercase()}" }
            tvUsos.text      = "TOTAL VECES USADAS: 34"
            // Mantén la gráfica de ejemplo
            ivGrafica.setImageResource(R.drawable.grafica)
        }
    }
    }
