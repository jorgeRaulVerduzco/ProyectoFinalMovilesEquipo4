package com.example.closetvirtual

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide

class DetalleOutfitActivity : AppCompatActivity() {

    private lateinit var vm: OutfitsViewModel
    private var progressDialog: ProgressDialog? = null
    private lateinit var tvNombreOutfit: TextView
    private lateinit var llTopContainer: LinearLayout
    private lateinit var llBottomContainer: LinearLayout
    private lateinit var llZapatosContainer: LinearLayout
    private lateinit var llBodysuitContainer: LinearLayout
    private lateinit var llAccesoriosContainer: LinearLayout
    private lateinit var btnVolver: View
    private lateinit var btnUser: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalle_outfit)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detalle_outfit_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar ViewModel
        vm = ViewModelProvider(this).get(OutfitsViewModel::class.java)

        // Referencias UI
        tvNombreOutfit = findViewById(R.id.tvNombreOutfit)
        llTopContainer = findViewById(R.id.llTopContainer)
        llBottomContainer = findViewById(R.id.llBottomContainer)
        llZapatosContainer = findViewById(R.id.llZapatosContainer)
        llBodysuitContainer = findViewById(R.id.llBodysuitContainer)
        llAccesoriosContainer = findViewById(R.id.llAccesoriosContainer)
        btnVolver = findViewById(R.id.btnVolver)
        btnUser = findViewById(R.id.btnUser)

        // Configurar botón volver
        btnVolver.setOnClickListener {
            finish()
        }

        // Configurar botón usuario
        btnUser.setOnClickListener {
            val intent = Intent(this, ConfiguracionUsuarioActivity::class.java)
            startActivity(intent)
        }

        // Observar cambios en la carga
        vm.isLoading.observe(this, Observer { isLoading ->
            if (isLoading) {
                mostrarProgressDialog()
            } else {
                ocultarProgressDialog()
            }
        })

        // Observar mensajes de error
        vm.errorMessage.observe(this, Observer { mensaje ->
            if (!mensaje.isNullOrEmpty()) {
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
            }
        })

        // Obtener el ID del outfit pasado como extra
        val outfitId = intent.getStringExtra("outfitId")
        if (outfitId != null) {
            cargarDetalleOutfit(outfitId)
        } else {
            Toast.makeText(this, "Error: No se pudo obtener el ID del outfit", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun cargarDetalleOutfit(outfitId: String) {
        vm.obtenerOutfitPorId(outfitId) { outfit ->
            if (outfit != null) {
                mostrarOutfitEnUI(outfit)
            } else {
                Toast.makeText(this, "No se pudo cargar el outfit", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun mostrarProgressDialog() {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(this).apply {
                setMessage("Cargando...")
                setCancelable(false)
            }
        }
        progressDialog?.show()
    }

    private fun ocultarProgressDialog() {
        progressDialog?.dismiss()
    }

    private fun mostrarOutfitEnUI(outfit: Outfits) {
        // Mostrar nombre del outfit
        tvNombreOutfit.text = outfit.nombre

        // Agrupar prendas por categoría
        val prendasPorCategoria = outfit.items.groupBy { it.categoria.uppercase() }

        // Limpiar contenedores
        limpiarContenedores()

        // Mostrar prendas por categoría
        mostrarPrendasPorCategoria(prendasPorCategoria, "TOP", llTopContainer)
        mostrarPrendasPorCategoria(prendasPorCategoria, "BOTTOM", llBottomContainer)
        mostrarPrendasPorCategoria(prendasPorCategoria, "ZAPATOS", llZapatosContainer)
        mostrarPrendasPorCategoria(prendasPorCategoria, "BODYSUIT", llBodysuitContainer)
        mostrarPrendasPorCategoria(prendasPorCategoria, "ACCESORIOS", llAccesoriosContainer)
    }

    private fun mostrarPrendasPorCategoria(prendasPorCategoria: Map<String, List<Prenda>>, categoria: String, container: LinearLayout) {
        prendasPorCategoria[categoria]?.let { prendas ->
            if (prendas.isNotEmpty()) {
                mostrarPrendasEnCategoria(container, prendas)
            } else {
                mostrarMensajeVacio(container)
            }
        } ?: mostrarMensajeVacio(container)
    }

    private fun limpiarContenedores() {
        llTopContainer.removeAllViews()
        llBottomContainer.removeAllViews()
        llZapatosContainer.removeAllViews()
        llBodysuitContainer.removeAllViews()
        llAccesoriosContainer.removeAllViews()
    }

    private fun mostrarPrendasEnCategoria(container: LinearLayout, prendas: List<Prenda>) {
        for (prenda in prendas) {
            val itemView = layoutInflater.inflate(R.layout.item_prenda_detalle, container, false)
            val ivPrenda = itemView.findViewById<ImageView>(R.id.ivPrenda)
            val tvNombre = itemView.findViewById<TextView>(R.id.tvPrendaNombre)

            // Cargar imagen
            if (prenda.imagen.isNotEmpty()) {
                Glide.with(this)
                    .load(prenda.imagen)
                    .centerCrop()
                    .into(ivPrenda)
            }

            // Mostrar nombre
            tvNombre.text = prenda.nombre

            container.addView(itemView)
        }
    }

    private fun mostrarMensajeVacio(container: LinearLayout) {
        val textView = TextView(this).apply {
            text = "No hay prendas seleccionadas"
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            setPadding(0, 16, 0, 16)
        }
        container.addView(textView)
    }
}