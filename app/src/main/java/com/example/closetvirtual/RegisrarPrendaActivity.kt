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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
class RegisrarPrendaActivity : AppCompatActivity() {
    private val PICK_IMAGE = 1001
    private var selectedImageUri: Uri? = null
    private lateinit var vm: PrendaViewModel
    private var progressDialog: ProgressDialog? = null
    private var isUploading = false // Bandera para evitar múltiples subidas

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

        try {
            MediaManager.get()
        } catch (e: Exception) {
            initCloudinary()
        }

        vm = ViewModelProvider(this).get(PrendaViewModel::class.java)

        // Selector de imagen
        findViewById<ImageButton>(R.id.ibSelectImage).setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, PICK_IMAGE)
            } catch (e: Exception) {
                Toast.makeText(this, "Error al abrir la galería: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnRegistrarPrenda).setOnClickListener {
            if (isUploading) {
                Toast.makeText(this, "Procesando una subida, por favor espera...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // validar campos básicos
            val nombre = findViewById<EditText>(R.id.etNombrePrenda).text.toString().trim()
            val color = findViewById<EditText>(R.id.etColor).text.toString().trim()
            val tags = findViewById<EditText>(R.id.etTags).text.toString()
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            val categoria = findViewById<Spinner>(R.id.ListaDeCategoria).selectedItem.toString()
            val estampada = findViewById<CheckBox>(R.id.cbEstampadaSi).isChecked

            if (nombre.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa un nombre para la prenda", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedImageUri == null) {
                Toast.makeText(this, "Por favor selecciona una imagen", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Subir imagen a Cloudinary y luego guardar prenda
            isUploading = true
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
        try {
            val config = mutableMapOf<String, String>()
            config["cloud_name"] = CLOUD_NAME
            MediaManager.init(applicationContext, config)
        } catch (e: Exception) {
            throw e
        }
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

        try {
            // Usar un scope de corrutina para manejar posibles excepciones
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    MediaManager.get().upload(uri)
                        .unsigned(UPLOAD_PRESET)
                        .callback(object : UploadCallback {
                            override fun onStart(requestId: String) { /* opcional */ }
                            override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) { /* opcional */ }

                            override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                                try {
                                    val urlImagen = (resultData["secure_url"] as? String).orEmpty()
                                    if (urlImagen.isEmpty()) {
                                        hideProgress()
                                        isUploading = false
                                        Toast.makeText(this@RegisrarPrendaActivity, "URL vacía", Toast.LENGTH_LONG).show()
                                        return
                                    }
                                    savePrendaEnFirestore(
                                        imagenUrl = urlImagen,
                                        nombre = nombre,
                                        categoria = categoria,
                                        color = color,
                                        estampada = estampada,
                                        tags = tags
                                    )
                                } catch (e: Exception) {
                                    hideProgress()
                                    isUploading = false
                                    Toast.makeText(this@RegisrarPrendaActivity, "Error al procesar datos: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }

                            override fun onError(requestId: String, error: ErrorInfo) {
                                hideProgress()
                                isUploading = false
                                Toast.makeText(this@RegisrarPrendaActivity, "Error al subir imagen: ${error.description}", Toast.LENGTH_LONG).show()
                            }

                            override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                                // opcional
                            }
                        })
                        .dispatch()
                } catch (e: Exception) {
                    hideProgress()
                    isUploading = false
                    Toast.makeText(this@RegisrarPrendaActivity, "Error en la subida: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            hideProgress()
            isUploading = false
            Toast.makeText(this, "Error al iniciar la subida: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun savePrendaEnFirestore(
        imagenUrl: String,
        nombre: String,
        categoria: String,
        color: String,
        estampada: Boolean,
        tags: List<String>
    ) {
        try {
            // Crear objeto Prenda
            val prenda = Prenda(
                nombre = nombre,
                categoria = categoria,
                color = color,
                estampada = estampada,
                tags = tags,
                imagen = imagenUrl
            )
            // Usar el ViewModel para agregarla
            vm.agregarPrenda(prenda)

            // Esperar brevemente y luego finalizar
            CoroutineScope(Dispatchers.Main).launch {
                hideProgress()
                Toast.makeText(this@RegisrarPrendaActivity, "Prenda registrada correctamente", Toast.LENGTH_SHORT).show()

                isUploading = false
                // Usar un pequeño retraso antes de finalizar para asegurar que los Toast se muestren
                kotlinx.coroutines.delay(500)
                finish()
            }
        } catch (e: Exception) {
            hideProgress()
            isUploading = false
            Toast.makeText(this@RegisrarPrendaActivity, "Error al guardar en Firestore: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showProgress(mensaje: String) {
        try {
            if (progressDialog == null) {
                progressDialog = ProgressDialog(this).apply {
                    isIndeterminate = true
                    setCancelable(false)
                }
            }
            progressDialog?.setMessage(mensaje)
            progressDialog?.show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al mostrar progreso: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideProgress() {
        try {
            progressDialog?.dismiss()
        } catch (e: Exception) {
            // Ignorar errores al ocultar el diálogo
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            try {
                selectedImageUri = data?.data
                // mostrar preview
                findViewById<ImageView>(R.id.previewImage).apply {
                    setImageURI(selectedImageUri)
                    visibility = ImageView.VISIBLE
                }
                // escribir ruta en el EditText
                findViewById<EditText>(R.id.etImagePath).setText(selectedImageUri.toString())
            } catch (e: Exception) {
                Toast.makeText(this, "Error al procesar la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Asegurarse de liberar recursos
        hideProgress()
    }
}