package com.example.closetvirtual
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class PrendaViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val _prendas = MutableLiveData<List<Prenda>>(emptyList())
    val prendas: LiveData<List<Prenda>> = _prendas

    init {
        obtenerPrendas()
    }

    fun obtenerPrendas() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snapshot = db.collection("prendas").get().await()
                val lista = snapshot.documents.mapNotNull { it.toObject(Prenda::class.java) }
                _prendas.postValue(lista)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun agregarPrenda(prenda: Prenda) {
        prenda.id = UUID.randomUUID().toString()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection("prendas")
                    .document(prenda.id)
                    .set(prenda.toMap())
                    .await()
                // opcional: refrescar
                obtenerPrendas()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun obtenerPrendaPorId(id: String, callback: (Prenda?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val doc = db.collection("prendas").document(id).get().await()
                val p = doc.toObject(Prenda::class.java)
                callback(p)
            } catch (e: Exception) {
                e.printStackTrace()
                callback(null)
            }
        }
    }
}