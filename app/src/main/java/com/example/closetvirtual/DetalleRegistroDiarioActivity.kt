package com.example.closetvirtual

import android.app.ProgressDialog
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
class DetalleRegistroDiarioActivity : AppCompatActivity() {

    private lateinit var vm: RegistroDiarioViewModel
    private var progressDialog: ProgressDialog? = null
    private lateinit var tvFecha: TextView
    private lateinit var llTopContainer: LinearLayout
    private lateinit var llBottomContainer: LinearLayout
    private lateinit var llZapatosContainer: LinearLayout
    private lateinit var llBodysuitContainer: LinearLayout
    private lateinit var llAccesoriosContainer: LinearLayout
    private lateinit var btnVolver: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalle_registro_diario)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar ViewModel
        vm = ViewModelProvider(this).get(RegistroDiarioViewModel::class.java)

        // Referencias UI
        tvFecha = findViewById(R.id.tvFechaDetalle)
        llTopContainer = findViewById(R.id.llTopContainer)
        llBottomContainer = findViewById(R.id.llBottomContainer)
        llZapatosContainer = findViewById(R.id.llZapatosContainer)
        llBodysuitContainer = findViewById(R.id.llBodysuitContainer)
        llAccesoriosContainer = findViewById(R.id.llAccesoriosContainer)
        btnVolver = findViewById(R.id.btnVolver)

        // Configurar botón volver
        btnVolver.setOnClickListener {
            finish()
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

        // Obtener el ID del registro pasado como extra
        val registroId = intent.getStringExtra("registroId")
        if (registroId != null) {
            cargarDetalleRegistro(registroId)
        } else {
            Toast.makeText(this, "Error: No se pudo obtener el ID del registro", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Configurar botón de usuario
        findViewById<ImageView>(R.id.btnUser).setOnClickListener {
            // Redirigir a configuración de usuario
            val intent = android.content.Intent(this, ConfiguracionUsuarioActivity::class.java)
            startActivity(intent)
        }
    }

    private fun cargarDetalleRegistro(registroId: String) {
        vm.obtenerRegistroPorId(registroId) { registro ->
            if (registro != null) {
                mostrarRegistroEnUI(registro)
            } else {
                Toast.makeText(this, "No se pudo cargar el registro", Toast.LENGTH_SHORT).show()
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

    private fun mostrarRegistroEnUI(registro: RegistrosDiarios) {
        // Mostrar fecha
        tvFecha.text = registro.fecha

        // Agrupar prendas por categoría
        val prendasPorCategoria = registro.prendas.groupBy { it.categoria.uppercase() }

        // Limpiar contenedores
        limpiarContenedores()

        // Mostrar prendas por categoría
        prendasPorCategoria["TOP"]?.let { prendas ->
            if (prendas.isNotEmpty()) {
                mostrarPrendasEnCategoria(llTopContainer, prendas)
            } else {
                mostrarMensajeVacio(llTopContainer)
            }
        } ?: mostrarMensajeVacio(llTopContainer)

        prendasPorCategoria["BOTTOM"]?.let { prendas ->
            if (prendas.isNotEmpty()) {
                mostrarPrendasEnCategoria(llBottomContainer, prendas)
            } else {
                mostrarMensajeVacio(llBottomContainer)
            }
        } ?: mostrarMensajeVacio(llBottomContainer)

        prendasPorCategoria["ZAPATOS"]?.let { prendas ->
            if (prendas.isNotEmpty()) {
                mostrarPrendasEnCategoria(llZapatosContainer, prendas)
            } else {
                mostrarMensajeVacio(llZapatosContainer)
            }
        } ?: mostrarMensajeVacio(llZapatosContainer)

        prendasPorCategoria["BODYSUIT"]?.let { prendas ->
            if (prendas.isNotEmpty()) {
                mostrarPrendasEnCategoria(llBodysuitContainer, prendas)
            } else {
                mostrarMensajeVacio(llBodysuitContainer)
            }
        } ?: mostrarMensajeVacio(llBodysuitContainer)

        prendasPorCategoria["ACCESORIOS"]?.let { prendas ->
            if (prendas.isNotEmpty()) {
                mostrarPrendasEnCategoria(llAccesoriosContainer, prendas)
            } else {
                mostrarMensajeVacio(llAccesoriosContainer)
            }
        } ?: mostrarMensajeVacio(llAccesoriosContainer)
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