package com.example.closetvirtual


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.util.Calendar

class DetalleActivity : AppCompatActivity() {
    private lateinit var vm: PrendaViewModel
    private var currentPrenda: Prenda? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalle)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        vm = ViewModelProvider(this).get(PrendaViewModel::class.java)
        val btnEditar = findViewById<Button>(R.id.btnEditar)
        val btnEliminar = findViewById<Button>(R.id.btnEliminar)

        val prendaId = intent.getStringExtra("prendaId")
        prendaId?.let { id ->
            vm.obtenerPrendaPorId(id) { prenda ->
                prenda?.let {
                    currentPrenda = it
                    updateUIWithPrenda(it)
                }
            }
        }

        btnEditar.setOnClickListener {
            currentPrenda?.let { prenda ->
                startActivity(Intent(this, EditarPrendaActivity::class.java).apply {
                    putExtra("prendaId", prenda.id)
                })
            }
        }

        btnEliminar.setOnClickListener {
            currentPrenda?.let { prenda ->
                vm.eliminarPrenda(prenda.id) {
                    runOnUiThread {
                        Toast.makeText(this, "Prenda eliminada", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refrescar datos si venimos de edición
        currentPrenda?.id?.let { id ->
            vm.obtenerPrendaPorId(id) { prenda ->
                prenda?.let { updateUIWithPrenda(it) }
            }
        }
    }

    private fun updateUIWithPrenda(prenda: Prenda) {
        runOnUiThread {
            val ivPrenda = findViewById<ImageView>(R.id.ivPrenda)
            Glide.with(this)
                .load(prenda.imagen)
                .centerCrop()
                .into(ivPrenda)

            findViewById<TextView>(R.id.tvPrendaNombre).text = prenda.nombre
            findViewById<TextView>(R.id.tvCategoria).text = "CATEGORÍA: ${prenda.categoria}"
            findViewById<TextView>(R.id.tvColor).text = "COLOR: ${prenda.color}"
            findViewById<TextView>(R.id.tvEstampado).text = "ESTAMPADO: ${if (prenda.estampada) "SÍ" else "N/A"}"
            findViewById<TextView>(R.id.tvTags).text = prenda.tags.joinToString(" ") { "#${it.uppercase()}" }
            findViewById<TextView>(R.id.tvTotalUsos).text = "TOTAL VECES USADAS: ${prenda.usosTotales ?: 0}"

            // Configurar la gráfica de barras
            setupBarChart(prenda)
        }
    }
    private fun setupBarChart(prenda: Prenda) {
        val barChart = findViewById<BarChart>(R.id.barChart)

        // Obtener fecha actual para determinar el año actual
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)

        // Crear una lista con todos los meses del año actual
        val allMonths = ArrayList<String>()
        val monthNames = ArrayList<String>()

        for (month in 1..12) {
            // Formato MM-YYYY
            val monthStr = String.format("%02d", month)
            allMonths.add("$monthStr-$currentYear")

            // Nombre corto del mes para etiquetas
            val monthName = when(month) {
                1 -> "Ene"
                2 -> "Feb"
                3 -> "Mar"
                4 -> "Abr"
                5 -> "May"
                6 -> "Jun"
                7 -> "Jul"
                8 -> "Ago"
                9 -> "Sep"
                10 -> "Oct"
                11 -> "Nov"
                12 -> "Dic"
                else -> month.toString()
            }
            monthNames.add("$monthName\n$currentYear")
        }

        // Preparar entradas para la gráfica
        val entries = ArrayList<BarEntry>()

        // Recorrer todos los meses y asignar valor (0 si no hay datos)
        allMonths.forEachIndexed { index, monthKey ->
            // Buscar primero en formato MM-YYYY
            var usos = prenda.usosPorMes[monthKey] ?: 0

            // Si no hay datos, intentar con formato MM/YYYY
            if (usos == 0) {
                val altKey = monthKey.replace("-", "/")
                usos = prenda.usosPorMes[altKey] ?: 0
            }

            entries.add(BarEntry(index.toFloat(), usos.toFloat()))
        }

        try {
            // Crear el dataset y configurarlo
            val dataSet = BarDataSet(entries, "Usos por mes")
            dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
            dataSet.valueTextSize = 12f

            // Solo mostrar valores mayores que cero
            dataSet.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return if (value > 0) value.toInt().toString() else ""
                }
            }

            // Configurar datos y gráfica
            val barData = BarData(dataSet)
            barChart.data = barData

            // Configurar eje X
            val xAxis = barChart.xAxis
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.valueFormatter = IndexAxisValueFormatter(monthNames)
            xAxis.textSize = 10f
            xAxis.labelRotationAngle = -45f // Rotar etiquetas para mejor visualización

            // Configurar eje Y para que empiece desde 0
            val leftAxis = barChart.axisLeft
            leftAxis.axisMinimum = 0f

            // Configuración adicional
            barChart.description.isEnabled = false
            barChart.legend.isEnabled = true
            barChart.setFitBars(true)
            barChart.animateY(1000)

            // Actualizar gráfica
            barChart.invalidate()
        } catch (e: Exception) {
            Log.e("BarChart", "Error al configurar gráfica: ${e.message}", e)
            barChart.setNoDataText("Error al cargar gráfica: ${e.message}")
            barChart.invalidate()
        }
    }
    }
