package com.example.closetvirtual

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PrincipalActivity : AppCompatActivity() {
    private val prendas = ArrayList<Prenda>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_principal)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val btnAddItem = findViewById<Button>(R.id.btnAddItem)
        btnAddItem.setOnClickListener {
            val intent = Intent(this, RegisrarPrendaActivity::class.java)
            startActivity(intent)
        }
        val btnUser = findViewById<ImageButton>(R.id.btnUser)
        btnUser.setOnClickListener {
            val intent = Intent(this, ConfiguracionUsuarioActivity::class.java)
            startActivity(intent)
        }
        cargarPrendasEjemplo()
        mostrarPrendasEnUI()
    }
    private fun cargarPrendasEjemplo() {

        val topPrendas = listOf(
            Prenda(R.drawable.camisa_azul, "CAMISA AZUL", "TOP", "Azul", false, listOf("casual", "trabajo")),
            Prenda(R.drawable.blusa_rosa, "BLUSA ROSA", "TOP", "Rosa", false, listOf("fiesta", "elegante")),
            Prenda(R.drawable.camisa_cafe, "CAMISA CAFE", "TOP", "Cafe", true, listOf("casual", "otoño")),
            Prenda(R.drawable.camisa_verde, "CAMISA VERDE", "TOP", "verde", true, listOf("casual", "otoño")),
            Prenda(R.drawable.camisa_roja, "CAMISA ROJA", "TOP", "Rojo", true, listOf("casual", "otoño")),
            Prenda(R.drawable.camisa_gris, "CAMISA GRIS", "TOP", "gRIS", true, listOf("elegante", "invierno"))

        )

        val bottomPrendas = listOf(
            Prenda(R.drawable.cargo_pants, "CARGO PANTS", "BOTTOM", "Beige", false, listOf("casual", "aventura")),
            Prenda(R.drawable.pants, "PANTS", "BOTTOM", "Azul", false, listOf("casual", "diario")),
            Prenda(R.drawable.pantalon_negro, "PANTALON NEGRO", "BOTTOM", "Negro", false, listOf("formal", "trabajo")),
            Prenda(R.drawable.shortsito, "short moderno", "BOTTOM", "Negro", false, listOf("casual", "invierno")),
            Prenda(R.drawable.jogger, "jogger gris", "BOTTOM", "Gris", false, listOf("fiesta", "invierno")),


        )

        val zapatosPrendas = listOf(
            Prenda(R.drawable.coloridos, "COLORIDOS", "ZAPATOS", "Multicolor", false, listOf("casual", "deporte")),
            Prenda(R.drawable.converse, "CONVERSE", "ZAPATOS", "Rojo", false, listOf("casual", "juvenil")),
            Prenda(R.drawable.nike_rojos, "NIKE ROJOS", "ZAPATOS", "Rojo", false, listOf("deporte", "running"))
        )

        val bodysuitPrendas = listOf(
            Prenda(R.drawable.ic_launcher_background, "BODYSUIT NEGRO", "BODYSUIT", "Negro", false, listOf("elegante", "formal"))
        )

        val accesoriosPrendas = listOf(
            Prenda(R.drawable.ic_launcher_background, "COLLAR", "ACCESORIOS", "Plateado", false, listOf("elegante", "formal"))
        )

        prendas.addAll(topPrendas)
        prendas.addAll(bottomPrendas)
        prendas.addAll(zapatosPrendas)
        prendas.addAll(bodysuitPrendas)
        prendas.addAll(accesoriosPrendas)

    }

    private fun mostrarPrendasEnUI() {
        val topContainer = findViewById<LinearLayout>(R.id.topItemsContainer)
        val bottomContainer = findViewById<LinearLayout>(R.id.bottomItemsContainer)
        val zapatosContainer = findViewById<LinearLayout>(R.id.zapatosItemsContainer)

        topContainer.removeAllViews()
        bottomContainer.removeAllViews()
        zapatosContainer.removeAllViews()

        for (prenda in prendas) {
            when (prenda.categoria) {
                "TOP" -> addItemView(topContainer, prenda)
                "BOTTOM" -> addItemView(bottomContainer, prenda)
                "ZAPATOS" -> addItemView(zapatosContainer, prenda)
            }
        }
    }

    private fun addItemView(container: LinearLayout, prenda: Prenda) {
        val itemView = layoutInflater.inflate(R.layout.item_prenda, container, false)

        val imageView = itemView.findViewById<ImageView>(R.id.ivPrenda)
        val textView = itemView.findViewById<TextView>(R.id.tvPrendaNombre)

        imageView.setImageResource(prenda.imagen)
        textView.text = prenda.nombre

        itemView.setOnClickListener {
            val intent = Intent(this, DetalleActivity::class.java).apply {
                putExtra("imagen", prenda.imagen)
                putExtra("nombre", prenda.nombre)
                putExtra("categoria", prenda.categoria)
                putExtra("color", prenda.color)
                putExtra("estampada", prenda.estampada)
                putStringArrayListExtra("tags", ArrayList(prenda.tags))
            }
            startActivity(intent)
        }

        container.addView(itemView)
    }

}