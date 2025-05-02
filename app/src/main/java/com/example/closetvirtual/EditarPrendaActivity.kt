package com.example.closetvirtual

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback

class EditarPrendaActivity : AppCompatActivity() {
    private val PICK_IMAGE = 1001
    private var selectedImageUri: Uri? = null
    private lateinit var vm: PrendaViewModel
    private var progressDialog: ProgressDialog? = null
    private var currentPrenda: Prenda? = null

    private val CLOUD_NAME = "djgkddidp"
    private val UPLOAD_PRESET = "proyecto-preset"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editar_prenda)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        initCloudinary()
        vm = ViewModelProvider(this).get(PrendaViewModel::class.java)

        val etNombre      = findViewById<EditText>(R.id.etNombrePrenda)
        val etColor       = findViewById<EditText>(R.id.etColor)
        val etTags        = findViewById<EditText>(R.id.etTags)
        val spinner       = findViewById<Spinner>(R.id.ListaDeCategoria)
        val cbEstampada   = findViewById<CheckBox>(R.id.cbEstampadaSi)
        val etImagePath   = findViewById<EditText>(R.id.etImagePath)
        val ibSelectImage = findViewById<ImageButton>(R.id.ibSelectImage)
        val btnEditar     = findViewById<Button>(R.id.btnEditarPrenda)

        // Spinner setup
        val categorias = resources.getStringArray(R.array.categorias_array)
        val adapter    = ArrayAdapter(this, android.R.layout.simple_spinner_item, categorias)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Prefill datos
        intent.getStringExtra("prendaId")?.let { id ->
            vm.obtenerPrendaPorId(id) { p ->
                p?.let {
                    currentPrenda = it
                    runOnUiThread {
                        etNombre.setText(it.nombre)
                        etColor.setText(it.color)
                        etTags.setText(it.tags.joinToString(","))
                        spinner.setSelection(categorias.indexOf(it.categoria))
                        cbEstampada.isChecked = it.estampada
                        etImagePath.setText(it.imagen)
                    }
                }
            }
        }

        // Selección de imagen
        ibSelectImage.setOnClickListener {
            startActivityForResult(
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
                PICK_IMAGE
            )
        }

        btnEditar.setOnClickListener {
            val nombre    = etNombre.text.toString().trim()
            val color     = etColor.text.toString().trim()
            val tags      = etTags.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val categoria = spinner.selectedItem.toString()
            val estampada = cbEstampada.isChecked

            if (nombre.isEmpty()) {
                Toast.makeText(this, "Completa el nombre", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            currentPrenda?.let { prenda ->
                showProgress("Actualizando prenda...")
                fun finishUpdate() {
                    hideProgress()
                    Toast.makeText(this, "Prenda actualizada", Toast.LENGTH_SHORT).show()
                    finish()
                }

                prenda.nombre    = nombre
                prenda.color     = color
                prenda.tags      = tags
                prenda.categoria = categoria
                prenda.estampada = estampada

                if (selectedImageUri != null) {
                    MediaManager.get().upload(selectedImageUri)
                        .unsigned(UPLOAD_PRESET)
                        .callback(object : UploadCallback {
                            override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                                prenda.imagen = resultData["secure_url"] as String
                                vm.actualizarPrenda(prenda)
                                finishUpdate()
                            }
                            override fun onError(requestId: String, error: ErrorInfo) {
                                hideProgress()
                                Toast.makeText(this@EditarPrendaActivity, "Error al subir imagen: ${'$'}{error.description}", Toast.LENGTH_LONG).show()
                            }
                            override fun onStart(requestId: String) {}
                            override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                            override fun onReschedule(requestId: String, error: ErrorInfo?) {}
                        })
                        .dispatch()
                } else {
                    vm.actualizarPrenda(prenda)
                    finishUpdate()
                }
            }
        }
    }

    private fun initCloudinary() {
        val config = mapOf("cloud_name" to CLOUD_NAME)
        MediaManager.init(applicationContext, config)
    }

    private fun showProgress(msg: String) {
        if (progressDialog == null) progressDialog = ProgressDialog(this).apply {
            isIndeterminate = true; setCancelable(false)
        }
        progressDialog?.setMessage(msg)
        progressDialog?.show()
    }

    private fun hideProgress() {
        progressDialog?.dismiss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            data?.data?.let {
                selectedImageUri = it
                findViewById<EditText>(R.id.etImagePath).setText(it.toString())
            }
        }
    }
}