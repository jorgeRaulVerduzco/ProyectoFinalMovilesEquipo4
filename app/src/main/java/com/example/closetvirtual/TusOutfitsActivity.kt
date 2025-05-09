package com.example.closetvirtual

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TusOutfitsActivity : AppCompatActivity() {

    private lateinit var vm: OutfitsViewModel
    private var progressDialog: ProgressDialog? = null
    private lateinit var rvOutfits: RecyclerView
    private lateinit var outfitsAdapter: OutfitsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tus_outfits)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_tus_outfits)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar ViewModel
        vm = ViewModelProvider(this).get(OutfitsViewModel::class.java)

        // Referencias UI
        rvOutfits = findViewById(R.id.rvOutfits)

        // Configurar RecyclerView
        rvOutfits.layoutManager = LinearLayoutManager(this)
        outfitsAdapter = OutfitsAdapter()
        rvOutfits.adapter = outfitsAdapter

        // Configurar botón de crear outfit
        findViewById<Button>(R.id.btnCrearOutfit).setOnClickListener {
            val intent = Intent(this, CrearOutfit::class.java)
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

        // Observar cambios en los outfits
        vm.outfits.observe(this, Observer { outfits ->
            outfitsAdapter.setOutfits(outfits)
        })
    }

    override fun onResume() {
        super.onResume()
        // Refrescar la lista de outfits cada vez que se vuelve a la actividad
        vm.obtenerOutfits()
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

    // Adaptador para el RecyclerView de outfits
    inner class OutfitsAdapter : RecyclerView.Adapter<OutfitsAdapter.OutfitViewHolder>() {
        private var outfits: List<Outfits> = emptyList()

        fun setOutfits(outfits: List<Outfits>) {
            this.outfits = outfits
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OutfitViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_outfit, parent, false)
            return OutfitViewHolder(view)
        }

        override fun onBindViewHolder(holder: OutfitViewHolder, position: Int) {
            val outfit = outfits[position]
            holder.tvNombreOutfit.text = outfit.nombre

            // Configurar el onClick para ver detalles del outfit
            holder.itemView.setOnClickListener {
                val intent = Intent(this@TusOutfitsActivity, DetalleOutfitActivity::class.java)
                intent.putExtra("outfitId", outfit.id)
                startActivity(intent)
            }

            // Configurar long click para eliminar
            holder.itemView.setOnLongClickListener {
                // Confirmar eliminación
                androidx.appcompat.app.AlertDialog.Builder(this@TusOutfitsActivity)
                    .setTitle("Eliminar outfit")
                    .setMessage("¿Estás seguro de que deseas eliminar este outfit \"${outfit.nombre}\"?")
                    .setPositiveButton("Eliminar") { _, _ ->
                        vm.eliminarOutfit(outfit.id)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
                true
            }
        }

        override fun getItemCount(): Int = outfits.size

        inner class OutfitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvNombreOutfit: TextView = itemView.findViewById(R.id.tvNombreOutfit)
        }
    }
}
