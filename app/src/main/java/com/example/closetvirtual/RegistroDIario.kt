package com.example.closetvirtual

import android.animation.ValueAnimator
import android.app.ProgressDialog
import android.content.Intent
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import androidx.cardview.widget.CardView

class RegistroDIario : AppCompatActivity() {
    private lateinit var registroDiarioViewModel: RegistroDiarioViewModel
    private lateinit var prendaViewModel: PrendaViewModel
    private var progressDialog: ProgressDialog? = null

    // Referencias a contenedores UI
    private lateinit var topItemsContainer: LinearLayout
    private lateinit var bottomItemsContainer: LinearLayout
    private lateinit var zapatosItemsContainer: LinearLayout
    private lateinit var bodysuitItemsContainer: LinearLayout
    private lateinit var accesoriosItemsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_diario)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar ViewModels
        registroDiarioViewModel = ViewModelProvider(this).get(RegistroDiarioViewModel::class.java)
        prendaViewModel = ViewModelProvider(this).get(PrendaViewModel::class.java)

        // Referencias UI
        topItemsContainer = findViewById(R.id.topItemsContainer)
        bottomItemsContainer = findViewById(R.id.bottomItemsContainer)
        zapatosItemsContainer = findViewById(R.id.zapatosItemsContainer)
        bodysuitItemsContainer = findViewById(R.id.bodysuitItemsContainer)
        accesoriosItemsContainer = findViewById(R.id.accesoriosItemsContainer)

        val btnCrear = findViewById<Button>(R.id.btnCrearRegistroDiario)
        btnCrear.setOnClickListener {
            registroDiarioViewModel.agregarRegistroDiario()
        }

        findViewById<TextView>(R.id.btnRegistrarNuevaPrenda).setOnClickListener {
            val intent = Intent(this, RegisrarPrendaActivity::class.java)
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.btnUser).setOnClickListener {
            val intent = Intent(this, ConfiguracionUsuarioActivity::class.java)
            startActivity(intent)
        }

        // Observar cambios en la carga
        registroDiarioViewModel.isLoading.observe(this, Observer { isLoading ->
            if (isLoading) {
                mostrarProgressDialog()
            } else {
                ocultarProgressDialog()
            }
        })

        // Observar mensajes de error
        registroDiarioViewModel.errorMessage.observe(this, Observer { mensaje ->
            if (!mensaje.isNullOrEmpty()) {
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
            }
        })

        // Observar prendas del ViewModel de prendas
        prendaViewModel.prendas.observe(this, Observer { prendas ->
            mostrarPrendasEnUI(prendas)
        })

        // Observar si el registro se guardó exitosamente
        registroDiarioViewModel.registrosDiarios.observe(this, Observer {
            // Esta observación se activa cuando se actualiza la lista de registros,
            // lo que ocurre después de guardar un nuevo registro
            if (registroDiarioViewModel.isLoading.value == false) {
                Toast.makeText(this, "Registro guardado exitosamente", Toast.LENGTH_SHORT).show()
                // Redirigir a la actividad de registros diarios
                val intent = Intent(this, TusRegistrosDiariosActivity::class.java)
                startActivity(intent)
                finish() // Cerrar esta actividad
            }
        })

        // Cargar las prendas
        prendaViewModel.obtenerPrendas()
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

    private fun mostrarPrendasEnUI(prendas: List<Prenda>) {
        // Limpiar contenedores
        listOf(topItemsContainer, bottomItemsContainer, zapatosItemsContainer,
            bodysuitItemsContainer, accesoriosItemsContainer).forEach { it.removeAllViews() }

        // Agrupar prendas por categoría
        val prendasAgrupadas = prendas.groupBy { it.categoria.uppercase() }

        // Añadir prendas a sus respectivos contenedores
        prendasAgrupadas["TOP"]?.forEach { prenda -> addItemView(topItemsContainer, prenda) }
        prendasAgrupadas["BOTTOM"]?.forEach { prenda -> addItemView(bottomItemsContainer, prenda) }
        prendasAgrupadas["ZAPATOS"]?.forEach { prenda -> addItemView(zapatosItemsContainer, prenda) }
        prendasAgrupadas["BODYSUIT"]?.forEach { prenda -> addItemView(bodysuitItemsContainer, prenda) }
        prendasAgrupadas["ACCESORIOS"]?.forEach { prenda -> addItemView(accesoriosItemsContainer, prenda) }

        // Añadir mensaje si alguna categoría está vacía
        if (topItemsContainer.childCount == 0) addEmptyMessage(topItemsContainer)
        if (bottomItemsContainer.childCount == 0) addEmptyMessage(bottomItemsContainer)
        if (zapatosItemsContainer.childCount == 0) addEmptyMessage(zapatosItemsContainer)
        if (bodysuitItemsContainer.childCount == 0) addEmptyMessage(bodysuitItemsContainer)
        if (accesoriosItemsContainer.childCount == 0) addEmptyMessage(accesoriosItemsContainer)
    }

    private fun addItemView(container: LinearLayout, prenda: Prenda) {
        //falta eso
        val itemView = layoutInflater.inflate(R.layout.item_prenda_seleccionable, container, false)
        val imageView = itemView.findViewById<ImageView>(R.id.ivPrenda)
        val textView = itemView.findViewById<TextView>(R.id.tvPrendaNombre)
        val cardView = itemView.findViewById<CardView>(R.id.cardPrenda)

        // Carga la imagen desde la URL usando Glide
        if (prenda.imagen.isNotEmpty()) {
            Glide.with(this)
                .load(prenda.imagen)
                .centerCrop()
                .into(imageView)
        }
        textView.text = prenda.nombre

        // Verificar si la prenda está seleccionada y actualizar el estilo
        actualizarEstiloSeleccion(cardView, registroDiarioViewModel.estaSeleccionada(prenda))

        // Configurar evento de clic para seleccionar/deseleccionar
        itemView.setOnClickListener {
            val estaSeleccionada = registroDiarioViewModel.estaSeleccionada(prenda)
            if (estaSeleccionada) {
                registroDiarioViewModel.removerPrendaSeleccionada(prenda)
            } else {
                registroDiarioViewModel.agregarPrendaSeleccionada(prenda)
            }

            // Animar la selección/deselección
            animarSeleccion(cardView, !estaSeleccionada)
        }

        container.addView(itemView)
    }

    private fun actualizarEstiloSeleccion(cardView: CardView, seleccionada: Boolean) {
        if (seleccionada) {
            cardView.setCardBackgroundColor(resources.getColor(android.R.color.holo_blue_light))
            cardView.cardElevation = 12f
            cardView.radius = 16f
        } else {
            cardView.setCardBackgroundColor(resources.getColor(android.R.color.white))
            cardView.cardElevation = 4f
            cardView.radius = 8f
        }
    }

    private fun animarSeleccion(cardView: CardView, seleccionar: Boolean) {
        // Animar elevación
        val elevacionInicial = cardView.cardElevation
        val elevacionFinal = if (seleccionar) 12f else 4f
        ValueAnimator.ofFloat(elevacionInicial, elevacionFinal).apply {
            duration = 200
            addUpdateListener { animator ->
                cardView.cardElevation = animator.animatedValue as Float
            }
            start()
        }

        // Animar radio de esquinas
        val radioInicial = cardView.radius
        val radioFinal = if (seleccionar) 16f else 8f
        ValueAnimator.ofFloat(radioInicial, radioFinal).apply {
            duration = 200
            addUpdateListener { animator ->
                cardView.radius = animator.animatedValue as Float
            }
            start()
        }

        // Animar color de fondo
        val colorInicial = if (seleccionar)
            resources.getColor(android.R.color.white)
        else resources.getColor(android.R.color.holo_blue_light)
        val colorFinal = if (seleccionar)
            resources.getColor(android.R.color.holo_blue_light)
        else resources.getColor(android.R.color.white)

        ValueAnimator.ofArgb(colorInicial, colorFinal).apply {
            duration = 200
            addUpdateListener { animator ->
                cardView.setCardBackgroundColor(animator.animatedValue as Int)
            }
            start()
        }
    }

    private fun addEmptyMessage(container: LinearLayout) {
        val textView = TextView(this).apply {
            text = "No hay prendas en esta categoría"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            setPadding(0, 20, 0, 20)
        }
        container.addView(textView)
    }
}