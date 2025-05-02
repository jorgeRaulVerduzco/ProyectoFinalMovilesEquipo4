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
import java.util.*
class RegisrarPrendaActivity : AppCompatActivity() {
    private val PICK_IMAGE = 1001
    private var selectedImageUri: Uri? = null
    private lateinit var vm: PrendaViewModel
    private var progressDialog: ProgressDialog? = null

    // Tus credenciales de Cloudinary
    private val CLOUD_NAME = "djgkddidp"
    private val UPLOAD_PRESET = "proyecto-preset"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_regisrar_prenda)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // Inicializar Cloudinary
        initCloudinary()

        vm = ViewModelProvider(this).get(PrendaViewModel::class.java)

        // Selector de imagen
        findViewById<ImageButton>(R.id.ibSelectImage).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE)
        }

        findViewById<Button>(R.id.btnRegistrarPrenda).setOnClickListener {
            // validar campos básicos
            val nombre    = findViewById<EditText>(R.id.etNombrePrenda).text.toString().trim()
            val color     = findViewById<EditText>(R.id.etColor).text.toString().trim()
            val tags      = findViewById<EditText>(R.id.etTags).text
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            val categoria = findViewById<Spinner>(R.id.ListaDeCategoria).selectedItem.toString()
            val estampada = findViewById<CheckBox>(R.id.cbEstampadaSi).isChecked

            if (nombre.isEmpty() || selectedImageUri == null) {
                Toast.makeText(this, "Completa nombre y selecciona una imagen", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Subir imagen a Cloudinary y luego guardar prenda
            uploadImageAndSavePrenda(
                uri = selectedImageUri!!,
                nombre = nombre,
                categoria = categoria,
                color = color,
                estampada = estampada,
                tags = tags
            )
        }
    }

    private fun initCloudinary() {
        val config = mutableMapOf<String,String>()
        config["cloud_name"] = CLOUD_NAME
        MediaManager.init(applicationContext, config)
    }

    private fun uploadImageAndSavePrenda(
        uri: Uri,
        nombre: String,
        categoria: String,
        color: String,
        estampada: Boolean,
        tags: List<String>
    ) {
        showProgress("Subiendo imagen...")
        MediaManager.get().upload(uri)
            .unsigned(UPLOAD_PRESET)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) { /* opcional */ }
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) { /* opcional */ }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val urlImagen = (resultData["secure_url"] as? String).orEmpty()
                    savePrendaEnFirestore(
                        imagenUrl = urlImagen,
                        nombre = nombre,
                        categoria = categoria,
                        color = color,
                        estampada = estampada,
                        tags = tags
                    )
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    hideProgress()
                    Toast.makeText(this@RegisrarPrendaActivity, "Error al subir imagen: ${error.description}", Toast.LENGTH_LONG).show()
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    // opcional
                }
            })
            .dispatch()
    }

    private fun savePrendaEnFirestore(
        imagenUrl: String,
        nombre: String,
        categoria: String,
        color: String,
        estampada: Boolean,
        tags: List<String>
    ) {
        // Crear objeto Prenda
        val prenda = Prenda(
            nombre    = nombre,
            categoria = categoria,
            color     = color,
            estampada = estampada,
            tags      = tags,
            imagen    = imagenUrl
        )
        // Usar el ViewModel para agregarla
        vm.agregarPrenda(prenda)

        hideProgress()
        Toast.makeText(this, "Prenda registrada correctamente", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun showProgress(mensaje: String) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(this).apply {
                isIndeterminate = true
                setCancelable(false)
            }
        }
        progressDialog?.setMessage(mensaje)
        progressDialog?.show()
    }

    private fun hideProgress() {
        progressDialog?.dismiss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            selectedImageUri = data?.data
            // mostrar preview
            findViewById<ImageView>(R.id.previewImage).apply {
                setImageURI(selectedImageUri)
                visibility = ImageView.VISIBLE
            }
            // escribir ruta en el EditText
            findViewById<EditText>(R.id.etImagePath).setText(selectedImageUri.toString())
        }
    }
}