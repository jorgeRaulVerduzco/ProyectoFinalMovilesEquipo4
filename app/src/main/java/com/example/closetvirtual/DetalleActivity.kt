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
import com.bumptech.glide.Glide

class DetalleActivity : AppCompatActivity() {
    private lateinit var vm: PrendaViewModel
    private var currentPrenda: Prenda? = null

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
        val btnEditar = findViewById<Button>(R.id.btnEditar)
        val btnEliminar = findViewById<Button>(R.id.btnEliminar)

        val prendaId = intent.getStringExtra("prendaId")
        prendaId?.let { id ->
            vm.obtenerPrendaPorId(id) { prenda ->
                prenda?.let {
                    currentPrenda = it
                    updateUIWithPrenda(it)
                }
            }
        }

        btnEditar.setOnClickListener {
            currentPrenda?.let { prenda ->
                startActivity(Intent(this, EditarPrendaActivity::class.java).apply {
                    putExtra("prendaId", prenda.id)
                })
            }
        }

        btnEliminar.setOnClickListener {
            currentPrenda?.let { prenda ->
                vm.eliminarPrenda(prenda.id) {
                    runOnUiThread {
                        Toast.makeText(this, "Prenda eliminada", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refrescar datos si venimos de edición
        currentPrenda?.id?.let { id ->
            vm.obtenerPrendaPorId(id) { prenda ->
                prenda?.let { updateUIWithPrenda(it) }
            }
        }
    }

    private fun updateUIWithPrenda(prenda: Prenda) {
        runOnUiThread {
            val ivPrenda    = findViewById<ImageView>(R.id.ivPrenda)
            Glide.with(this)
                .load(prenda.imagen)
                .centerCrop()
                .into(ivPrenda)

            findViewById<TextView>(R.id.tvPrendaNombre).text    = prenda.nombre
            findViewById<TextView>(R.id.tvCategoria).text      = "CATEGORÍA: ${prenda.categoria}"
            findViewById<TextView>(R.id.tvColor).text          = "COLOR: ${prenda.color}"
            findViewById<TextView>(R.id.tvEstampado).text      = "ESTAMPADO: ${if (prenda.estampada) "SÍ" else "N/A"}"
            findViewById<TextView>(R.id.tvTags).text           = prenda.tags.joinToString(" ") { "#${it.uppercase()}" }
            findViewById<TextView>(R.id.tvTotalUsos).text      = "TOTAL VECES USADAS: 34"
            findViewById<ImageView>(R.id.ivBarChart)
                .setImageResource(R.drawable.grafica)
        }
    }
    }
