package com.example.closetvirtual

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
class TusRegistrosDiariosActivity : AppCompatActivity() {


    private lateinit var vm: RegistroDiarioViewModel
    private var progressDialog: ProgressDialog? = null
    private lateinit var llListaRegistros: LinearLayout
    private lateinit var tvTusRegistros: TextView
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tus_registros_diarios)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Verificar si el usuario está autenticado
        if (auth.currentUser == null) {
            // Redirigir al login si no hay usuario autenticado
            Toast.makeText(this, "Debes iniciar sesión para crear un registro", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // Inicializar ViewModel
        vm = ViewModelProvider(this).get(RegistroDiarioViewModel::class.java)

        // Referencias UI
        llListaRegistros = findViewById(R.id.llListaRegistros)
        tvTusRegistros = findViewById(R.id.tvTusRegistros)

        // Personalizar título con el nombre del usuario
        personalizarTitulo()

        // Configurar botón de crear registro
        findViewById<Button>(R.id.btnCrearRegistro).setOnClickListener {
            val intent = Intent(this, RegistroDIario::class.java)
            startActivity(intent)
        }

        // Configurar botón de usuario
        findViewById<ImageButton>(R.id.btnUser).setOnClickListener {
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

        // Observar cambios en los registros diarios
        vm.registrosDiarios.observe(this, Observer { registros ->
            mostrarRegistrosEnUI(registros)
        })
    }

    override fun onResume() {
        super.onResume()
        // Refrescar la lista de registros cada vez que se vuelve a la actividad
        vm.obtenerRegistrosDiarios()
    }

    private fun personalizarTitulo() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Obtener información adicional del usuario desde Firestore
            Firebase.firestore.collection("usuarios")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val nombre = document.getString("nombres") ?: ""
                        if (nombre.isNotEmpty()) {
                            tvTusRegistros.text = "REGISTROS DIARIOS DE ${nombre.uppercase()}"
                        }
                    }
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

    private fun mostrarRegistrosEnUI(registros: List<RegistrosDiarios>) {
        // Limpiar el contenedor
        llListaRegistros.removeAllViews()

        if (registros.isEmpty()) {
            // Mostrar mensaje si no hay registros
            val textView = TextView(this).apply {
                text = "No hay registros diarios. ¡Crea tu primer registro!"
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                textSize = 18f
                setPadding(0, 20, 0, 20)
            }
            llListaRegistros.addView(textView)
            return
        }

        // Añadir cada registro a la UI
        for (registro in registros) {
            val itemView = layoutInflater.inflate(R.layout.item_registro_diario, llListaRegistros, false)
            val textView = itemView.findViewById<TextView>(R.id.tvFechaRegistro)
            textView.text = registro.fecha

            // Configurar el onClick para ver detalles del registro
            itemView.setOnClickListener {
                val intent = Intent(this, DetalleRegistroDiarioActivity::class.java)
                intent.putExtra("registroId", registro.id)
                startActivity(intent)
            }

            // Configurar long click para eliminar
            itemView.setOnLongClickListener {
                // Confirmar eliminación
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Eliminar registro")
                    .setMessage("¿Estás seguro de que deseas eliminar este registro del ${registro.fecha}?")
                    .setPositiveButton("Eliminar") { _, _ ->
                        vm.eliminarRegistroDiario(registro.id)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
                true
            }

            llListaRegistros.addView(itemView)
        }
    }
}