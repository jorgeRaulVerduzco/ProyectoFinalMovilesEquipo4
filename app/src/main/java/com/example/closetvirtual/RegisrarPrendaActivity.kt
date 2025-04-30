package com.example.closetvirtual

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider

class RegisrarPrendaActivity : AppCompatActivity() {
    private val PICK_IMAGE = 1001
    private var selectedImageUri: Uri? = null
    private lateinit var vm: PrendaViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_regisrar_prenda)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        vm = ViewModelProvider(this).get(PrendaViewModel::class.java)

        findViewById<ImageButton>(R.id.ibSelectImage).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE)
        }

        findViewById<Button>(R.id.btnRegistrarPrenda).setOnClickListener {
            val nombre    = findViewById<EditText>(R.id.etNombrePrenda).text.toString()
            val color     = findViewById<EditText>(R.id.etColor).text.toString()
            val tags      = findViewById<EditText>(R.id.etTags).text
                .split(",")
                .map { it.trim() }
            val categoria = findViewById<Spinner>(R.id.ListaDeCategoria).selectedItem.toString()
            val estampada = findViewById<CheckBox>(R.id.cbEstampadaSi).isChecked
            val imagenUri = selectedImageUri?.toString() ?: ""

            val prenda = Prenda(
                nombre    = nombre,
                categoria = categoria,
                color     = color,
                estampada = estampada,
                tags      = tags,
                imagen    = imagenUri
            )
            vm.agregarPrenda(prenda)
            finish()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            selectedImageUri = data?.data
            // opcional: mostrar preview
            findViewById<EditText>(R.id.etImagePath).setText(selectedImageUri.toString())
        }
    }
}