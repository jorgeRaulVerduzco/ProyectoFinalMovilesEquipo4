package com.example.closetvirtual
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class PrendaViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val _prendas = MutableLiveData<List<Prenda>>(emptyList())
    val prendas: LiveData<List<Prenda>> = _prendas

    // Lista completa de prendas antes de filtrar
    private var prendasCompletas: List<Prenda> = emptyList()

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        obtenerPrendas()
    }

    fun obtenerPrendas() {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snapshot = db.collection("prendas").get().await()
                val lista = snapshot.documents.mapNotNull { doc ->
                    val prenda = doc.toObject(Prenda::class.java)
                    prenda?.apply { id = doc.id } // Aseguramos que el ID se establezca correctamente
                }
                // Guardar la lista completa
                prendasCompletas = lista
                // Inicialmente, mostrar todas las prendas
                _prendas.postValue(lista)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // Nueva función para filtrar prendas
    fun filtrarPrendas(query: String) {
        if (query.isEmpty()) {
            // Si la consulta está vacía, mostrar todas las prendas
            _prendas.value = prendasCompletas
            return
        }

        val queryLowerCase = query.lowercase()
        val prendasFiltradas = prendasCompletas.filter { prenda ->
            // Buscar por nombre
            prenda.nombre.lowercase().contains(queryLowerCase) ||
                    // Buscar por tags
                    prenda.tags.any { tag -> tag.lowercase().contains(queryLowerCase) }
        }

        _prendas.value = prendasFiltradas
    }

    fun agregarPrenda(prenda: Prenda) {
        _isLoading.value = true

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
            }  finally {
                _isLoading.postValue(false)
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

    fun actualizarPrenda(prenda: Prenda) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection("prendas")
                    .document(prenda.id)
                    .set(prenda.toMap())
                    .await()
                obtenerPrendas()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun eliminarPrenda(id: String, onComplete: (() -> Unit)? = null) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection("prendas")
                    .document(id)
                    .delete()
                    .await()
                onComplete?.invoke()
                obtenerPrendas()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

}