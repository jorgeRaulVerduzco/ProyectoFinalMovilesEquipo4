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
import kotlinx.coroutines.withContext
import java.util.UUID


class PrendaViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val _prendas = MutableLiveData<List<Prenda>>(emptyList())
    val prendas: LiveData<List<Prenda>> = _prendas

    // Lista completa de prendas antes de filtrar
    private var prendasCompletas: List<Prenda> = emptyList()

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // Error message LiveData
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    init {
        obtenerPrendas()
    }

    fun obtenerPrendas() {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snapshot = db.collection("prendas").get().await()
                val lista = snapshot.documents.mapNotNull { doc ->
                    try {
                        val prenda = doc.toObject(Prenda::class.java)
                        prenda?.apply { id = doc.id } // Aseguramos que el ID se establezca correctamente
                    } catch (e: Exception) {
                        null // Si hay error al convertir un documento, lo omitimos
                    }
                }
                // Guardar la lista completa
                prendasCompletas = lista
                // Inicialmente, mostrar todas las prendas
                _prendas.postValue(lista)
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "Error al obtener prendas: ${e.message}"
                }
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // Función para filtrar prendas
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

        // Validar que la prenda tenga los campos requeridos
        if (prenda.nombre.isBlank() || prenda.imagen.isBlank() || prenda.categoria.isBlank()) {
            _errorMessage.value = "La prenda debe tener nombre, imagen y categoría"
            _isLoading.value = false
            return
        }

        prenda.id = UUID.randomUUID().toString()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection("prendas")
                    .document(prenda.id)
                    .set(prenda.toMap())
                    .await()

                // Recargar la lista después de agregar
                withContext(Dispatchers.Main) {
                    obtenerPrendas()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "Error al agregar prenda: ${e.message}"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                }
            }
        }
    }

    fun obtenerPrendaPorId(id: String, callback: (Prenda?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val doc = db.collection("prendas").document(id).get().await()
                val p = doc.toObject(Prenda::class.java)
                if (p != null) {
                    p.id = id
                }
                withContext(Dispatchers.Main) {
                    callback(p)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "Error al obtener prenda: ${e.message}"
                    callback(null)
                }
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

                withContext(Dispatchers.Main) {
                    obtenerPrendas()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "Error al actualizar prenda: ${e.message}"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                }
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

                withContext(Dispatchers.Main) {
                    onComplete?.invoke()
                    obtenerPrendas()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "Error al eliminar prenda: ${e.message}"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                }
            }
        }
    }

}