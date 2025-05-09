package com.example.closetvirtual

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class TusOutfitsActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tus_outfits)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_tus_outfits)) { v, insets ->
            val sb = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom)
            insets
        }

        val rv: RecyclerView = findViewById(R.id.rvOutfits)
        rv.layoutManager = LinearLayoutManager(this)

        val adapter = OutfitAdapter { outfit ->
            startActivity(Intent(this, OutfitSeleccionado::class.java)
                .putExtra("OUTFIT_NAME", outfit.nombre))
        }
        rv.adapter = adapter

        // Carga todos los outfits guardados
        db.collection("outfits").get().addOnSuccessListener { snap ->
            val list = snap.documents.mapNotNull { it.toObject(Outfits::class.java) }
            adapter.submitList(list)
        }
    }
}
