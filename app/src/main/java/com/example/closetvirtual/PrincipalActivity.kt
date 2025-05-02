package com.example.closetvirtual
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide

class PrincipalActivity : AppCompatActivity() {
    private lateinit var vm: PrendaViewModel
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_principal)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        vm = ViewModelProvider(this).get(PrendaViewModel::class.java)

        // Observar cambios en la carga
        vm.isLoading.observe(this, Observer { isLoading ->
            if (isLoading) {
                mostrarProgressDialog()
            } else {
                ocultarProgressDialog()
            }
        })

        // Observar cambios en las prendas
        vm.prendas.observe(this, Observer { prendas ->
            mostrarPrendasEnUI(prendas)
        })

        findViewById<Button>(R.id.btnAddItem).setOnClickListener {
            startActivity(Intent(this, RegisrarPrendaActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnUser).setOnClickListener {
            startActivity(Intent(this, ConfiguracionUsuarioActivity::class.java))
        }
        findViewById<Button>(R.id.btnRegistroDiario).setOnClickListener {
            startActivity(Intent(this, CrearOutfit::class.java))
        }
        findViewById<Button>(R.id.btnOutfits).setOnClickListener {
            startActivity(Intent(this, TusOutfitsActivity::class.java))
        }
        findViewById<Button>(R.id.btnCalendario).setOnClickListener {
            startActivity(Intent(this, CalendarioVisual::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // Refrescar la lista de prendas cada vez que se vuelve a la actividad
        vm.obtenerPrendas()
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
        val topContainer       = findViewById<LinearLayout>(R.id.topItemsContainer)
        val bottomContainer    = findViewById<LinearLayout>(R.id.bottomItemsContainer)
        val zapatosContainer   = findViewById<LinearLayout>(R.id.zapatosItemsContainer)
        val bodysuitContainer  = findViewById<LinearLayout>(R.id.bodysuitItemsContainer)
        val accesoriosContainer= findViewById<LinearLayout>(R.id.accesoriosItemsContainer)

        // Limpiar contenedores
        listOf(topContainer, bottomContainer, zapatosContainer, bodysuitContainer, accesoriosContainer)
            .forEach { it.removeAllViews() }

        // Agrupar prendas por categoría
        val prendasAgrupadas = prendas.groupBy { it.categoria.uppercase() }

        // Añadir mensaje si no hay prendas
        if (prendas.isEmpty()) {
            Toast.makeText(this, "No hay prendas registradas. ¡Agrega tu primera prenda!", Toast.LENGTH_LONG).show()
        }

        // Añadir prendas a sus respectivos contenedores
        prendasAgrupadas["TOP"]?.forEach { prenda -> addItemView(topContainer, prenda) }
        prendasAgrupadas["BOTTOM"]?.forEach { prenda -> addItemView(bottomContainer, prenda) }
        prendasAgrupadas["ZAPATOS"]?.forEach { prenda -> addItemView(zapatosContainer, prenda) }
        prendasAgrupadas["BODYSUIT"]?.forEach { prenda -> addItemView(bodysuitContainer, prenda) }
        prendasAgrupadas["ACCESORIOS"]?.forEach { prenda -> addItemView(accesoriosContainer, prenda) }

        // Añadir mensaje si alguna categoría está vacía
        if (topContainer.childCount == 0) addEmptyMessage(topContainer)
        if (bottomContainer.childCount == 0) addEmptyMessage(bottomContainer)
        if (zapatosContainer.childCount == 0) addEmptyMessage(zapatosContainer)
        if (bodysuitContainer.childCount == 0) addEmptyMessage(bodysuitContainer)
        if (accesoriosContainer.childCount == 0) addEmptyMessage(accesoriosContainer)
    }

    private fun addItemView(container: LinearLayout, prenda: Prenda) {
        val itemView = layoutInflater.inflate(R.layout.item_prenda, container, false)
        val imageView = itemView.findViewById<ImageView>(R.id.ivPrenda)
        val textView  = itemView.findViewById<TextView>(R.id.tvPrendaNombre)

        // Carga la imagen desde la URL usando Glide
        if (prenda.imagen.isNotEmpty()) {
            Glide.with(this)
                .load(prenda.imagen)
                .centerCrop()
                .into(imageView)
        }
        textView.text = prenda.nombre

        itemView.setOnClickListener {
            Intent(this, DetalleActivity::class.java).apply {
                putExtra("prendaId", prenda.id)
                startActivity(this)
            }
        }
        container.addView(itemView)
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