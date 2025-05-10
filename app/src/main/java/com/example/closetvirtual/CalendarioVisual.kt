package com.example.closetvirtual

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*
class CalendarioVisual : AppCompatActivity() {
    private lateinit var viewModel: OutfitsViewModel

    // UI Components
    private lateinit var calendarView: CalendarView
    private lateinit var tvMesActual: TextView
    private lateinit var btnMesAnterior: ImageButton
    private lateinit var btnMesSiguiente: ImageButton
    private lateinit var containerNoOutfit: LinearLayout
    private lateinit var containerOutfitSeleccionado: LinearLayout
    private lateinit var btnCrearOutfitFecha: Button
    private lateinit var tvFechaSeleccionada: TextView
    private lateinit var tvNombreOutfit: TextView
    private lateinit var tvNumPrendas: TextView
    private lateinit var ivOutfitPreview: ImageView
    private lateinit var btnVerDetalles: Button
    private lateinit var rvPrendasOutfit: RecyclerView
    private lateinit var btnVolverAtras: Button
    private lateinit var btnCrearNuevoOutfit: Button
    private lateinit var btnUser: ImageButton

    // Calendar variables
    private val calendar = Calendar.getInstance()
    private var selectedDate: Long = 0
    private var currentOutfitId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_calendario_visual)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(OutfitsViewModel::class.java)

        // Initialize UI components
        initializeViews()
        setupCalendar()
        setupListeners()
        setupObservers()

        // Set initial month
        updateMonthLabel()
        loadCurrentMonthOutfits()
    }

    private fun initializeViews() {
        // Calendar elements
        calendarView = findViewById(R.id.calendarView)
        tvMesActual = findViewById(R.id.tvMesActual)
        btnMesAnterior = findViewById(R.id.btnMesAnterior)
        btnMesSiguiente = findViewById(R.id.btnMesSiguiente)

        // No outfit container
        containerNoOutfit = findViewById(R.id.containerNoOutfit)
        btnCrearOutfitFecha = findViewById(R.id.btnCrearOutfitFecha)

        // Outfit selected container
        containerOutfitSeleccionado = findViewById(R.id.containerOutfitSeleccionado)
        tvFechaSeleccionada = findViewById(R.id.tvFechaSeleccionada)
        tvNombreOutfit = findViewById(R.id.tvNombreOutfit)
        tvNumPrendas = findViewById(R.id.tvNumPrendas)
        ivOutfitPreview = findViewById(R.id.ivOutfitPreview)
        btnVerDetalles = findViewById(R.id.btnVerDetalles)

        // RecyclerView for outfit items
        rvPrendasOutfit = findViewById(R.id.rvPrendasOutfit)
        rvPrendasOutfit.layoutManager = GridLayoutManager(this, 3)
        rvPrendasOutfit.adapter = PrendaMiniAdapter()

        // Bottom buttons
        btnVolverAtras = findViewById(R.id.btnVolverAtras)
        btnCrearNuevoOutfit = findViewById(R.id.btnCrearNuevoOutfit)
        btnUser = findViewById(R.id.btnUser)
    }

    private fun setupCalendar() {
        // Set initial date to today
        selectedDate = calendar.timeInMillis
        calendarView.date = selectedDate
    }

    private fun setupListeners() {
        // Month navigation buttons
        btnMesAnterior.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateMonthLabel()
            loadCurrentMonthOutfits()
        }

        btnMesSiguiente.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateMonthLabel()
            loadCurrentMonthOutfits()
        }

        // Calendar date selection
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            selectedDate = calendar.timeInMillis

            // Format the selected date for display
            val dateFormat = SimpleDateFormat("d MMMM, yyyy", Locale.getDefault())
            tvFechaSeleccionada.text = "Fecha: ${dateFormat.format(Date(selectedDate))}"

            // Check if there's an outfit for this date
            viewModel.seleccionarOutfitPorFecha(selectedDate)
        }

        // Button to create outfit for selected date
        btnCrearOutfitFecha.setOnClickListener {
            val intent = Intent(this, CrearOutfit::class.java)
            intent.putExtra("fecha", selectedDate)
            startActivity(intent)
        }

        // Button to view outfit details
        btnVerDetalles.setOnClickListener {
            if (currentOutfitId != null) {
                val intent = Intent(this, DetalleOutfitActivity::class.java)
                intent.putExtra("outfitId", currentOutfitId)
                startActivity(intent)
            }
        }

        // Bottom navigation buttons
        btnVolverAtras.setOnClickListener {
            finish()
        }

        btnCrearNuevoOutfit.setOnClickListener {
            val intent = Intent(this, CrearOutfit::class.java)
            startActivity(intent)
        }

        btnUser.setOnClickListener {
            val intent = Intent(this, ConfiguracionUsuarioActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupObservers() {
        // Observe selected outfit
        viewModel.outfitSeleccionado.observe(this, Observer { outfit ->
            if (outfit != null) {
                // Store the current outfit ID
                currentOutfitId = outfit.id

                // Show outfit details container and hide "no outfit" container
                containerOutfitSeleccionado.visibility = View.VISIBLE
                containerNoOutfit.visibility = View.GONE

                // Set outfit details
                tvNombreOutfit.text = outfit.nombre
                tvNumPrendas.text = "${outfit.items.size} prendas"

                // Load preview image (using first item's image)
                if (outfit.items.isNotEmpty() && outfit.items[0].imagen.isNotEmpty()) {
                    Glide.with(this)
                        .load(outfit.items[0].imagen)
                        .centerCrop()
                        .into(ivOutfitPreview)
                }

                // Update RecyclerView with outfit items
                (rvPrendasOutfit.adapter as PrendaMiniAdapter).setPrendas(outfit.items)
            } else {
                // No outfit for this date, show "no outfit" container
                containerOutfitSeleccionado.visibility = View.GONE
                containerNoOutfit.visibility = View.VISIBLE
                currentOutfitId = null
            }
        })

        // Observe error messages
        viewModel.errorMessage.observe(this, Observer { errorMsg ->
            if (!errorMsg.isNullOrEmpty()) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateMonthLabel() {
        val format = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        tvMesActual.text = format.format(calendar.time).capitalize(Locale.getDefault())
    }

    private fun loadCurrentMonthOutfits() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        viewModel.obtenerOutfitsPorMes(year, month)
    }

    override fun onResume() {
        super.onResume()
        // Reload data when returning to this activity
        loadCurrentMonthOutfits()
        // Re-check if there's an outfit for the selected date
        if (selectedDate > 0) {
            viewModel.seleccionarOutfitPorFecha(selectedDate)
        }
    }

    // Adapter for mini prenda items
    inner class PrendaMiniAdapter : RecyclerView.Adapter<PrendaMiniAdapter.PrendaMiniViewHolder>() {
        private var prendas: List<Prenda> = emptyList()

        fun setPrendas(newPrendas: List<Prenda>) {
            prendas = newPrendas
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrendaMiniViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_prenda_mini, parent, false)
            return PrendaMiniViewHolder(view)
        }

        override fun getItemCount(): Int = prendas.size

        override fun onBindViewHolder(holder: PrendaMiniViewHolder, position: Int) {
            val prenda = prendas[position]
            holder.bind(prenda)
        }

        inner class PrendaMiniViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val ivPrendaMini: ImageView = itemView.findViewById(R.id.ivPrendaMini)
            private val tvNombrePrendaMini: TextView = itemView.findViewById(R.id.tvNombrePrendaMini)

            fun bind(prenda: Prenda) {
                tvNombrePrendaMini.text = prenda.nombre

                if (prenda.imagen.isNotEmpty()) {
                    Glide.with(itemView.context)
                        .load(prenda.imagen)
                        .centerCrop()
                        .into(ivPrendaMini)
                }
            }
        }
    }
}