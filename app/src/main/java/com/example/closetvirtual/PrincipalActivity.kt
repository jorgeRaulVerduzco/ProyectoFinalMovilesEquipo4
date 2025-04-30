package com.example.closetvirtual
import android.content.Intent
import android.net.Uri
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

class PrincipalActivity : AppCompatActivity() {
    private lateinit var vm: PrendaViewModel

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

    private fun mostrarPrendasEnUI(prendas: List<Prenda>) {
        val topContainer       = findViewById<LinearLayout>(R.id.topItemsContainer)
        val bottomContainer    = findViewById<LinearLayout>(R.id.bottomItemsContainer)
        val zapatosContainer   = findViewById<LinearLayout>(R.id.zapatosItemsContainer)
        val bodysuitContainer  = findViewById<LinearLayout>(R.id.bodysuitItemsContainer)
        val accesoriosContainer= findViewById<LinearLayout>(R.id.accesoriosItemsContainer)

        listOf(topContainer, bottomContainer, zapatosContainer, bodysuitContainer, accesoriosContainer)
            .forEach { it.removeAllViews() }

        prendas.forEach { prenda ->
            when (prenda.categoria.uppercase()) {
                "TOP"       -> addItemView(topContainer, prenda)
                "BOTTOM"    -> addItemView(bottomContainer, prenda)
                "ZAPATOS"   -> addItemView(zapatosContainer, prenda)
                "BODYSUIT"  -> addItemView(bodysuitContainer, prenda)
                "ACCESORIOS"-> addItemView(accesoriosContainer, prenda)
            }
        }
    }

    private fun addItemView(container: LinearLayout, prenda: Prenda) {
        val itemView = layoutInflater.inflate(R.layout.item_prenda, container, false)
        val imageView = itemView.findViewById<ImageView>(R.id.ivPrenda)
        val textView  = itemView.findViewById<TextView>(R.id.tvPrendaNombre)

        // Carga la imagen desde el URI almacenado en Firestore
        if (prenda.imagen.isNotEmpty()) {
            imageView.setImageURI(Uri.parse(prenda.imagen))
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
}