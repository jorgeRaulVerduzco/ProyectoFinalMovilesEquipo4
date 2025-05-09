package com.example.closetvirtual
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class OutfitsViewModel : ViewModel() {
    private val db = Firebase.firestore

    // LiveData para la lista de outfits
    private val _outfits = MutableLiveData<List<Outfits>>(emptyList())
    val outfits: LiveData<List<Outfits>> = _outfits

    // LiveData para las prendas seleccionadas en el outfit actual
    private val _prendasSeleccionadas = MutableLiveData<MutableList<Prenda>>(mutableListOf())
    val prendasSeleccionadas: LiveData<MutableList<Prenda>> = _prendasSeleccionadas

    // LiveData para estado de carga
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData para mensajes de error
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    init {
        obtenerOutfits()
    }

    fun obtenerOutfits() {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snapshot = db.collection("outfits")
                    .orderBy("nombre", Query.Direction.ASCENDING)
                    .get()
                    .await()

                val listaOutfits = snapshot.documents.mapNotNull { doc ->
                    try {
                        val data = doc.data
                        if (data != null) {
                            Outfits.fromMap(data, doc.id)
                        } else null
                    } catch (e: Exception) {
                        null // Omitimos documentos con error
                    }
                }

                withContext(Dispatchers.Main) {
                    _outfits.value = listaOutfits
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "Error al obtener outfits: ${e.message}"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                }
            }
        }
    }

    fun guardarOutfit(nombre: String) {
        if (_prendasSeleccionadas.value.isNullOrEmpty()) {
            _errorMessage.value = "Debes seleccionar al menos una prenda"
            return
        }

        if (nombre.isBlank()) {
            _errorMessage.value = "Debes asignar un nombre al outfit"
            return
        }

        _isLoading.value = true

        val prendas = _prendasSeleccionadas.value ?: mutableListOf()
        val nuevoOutfit = Outfits(
            id = UUID.randomUUID().toString(),
            nombre = nombre,
            items = prendas
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection("outfits")
                    .document(nuevoOutfit.id)
                    .set(nuevoOutfit.toMap())
                    .await()

                // Actualizar la lista después de agregar
                withContext(Dispatchers.Main) {
                    obtenerOutfits()
                    // Limpiar las prendas seleccionadas
                    _prendasSeleccionadas.value = mutableListOf()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "Error al guardar outfit: ${e.message}"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                }
            }
        }
    }

    fun eliminarOutfit(id: String, onComplete: (() -> Unit)? = null) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection("outfits")
                    .document(id)
                    .delete()
                    .await()

                withContext(Dispatchers.Main) {
                    onComplete?.invoke()
                    obtenerOutfits()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "Error al eliminar outfit: ${e.message}"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                }
            }
        }
    }

    fun obtenerOutfitPorId(id: String, callback: (Outfits?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val doc = db.collection("outfits").document(id).get().await()
                val data = doc.data
                val outfit = if (data != null) {
                    Outfits.fromMap(data, doc.id)
                } else null

                withContext(Dispatchers.Main) {
                    callback(outfit)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "Error al obtener outfit: ${e.message}"
                    callback(null)
                }
            }
        }
    }

    // Funciones para manejar la selección de prendas
    fun agregarPrendaSeleccionada(prenda: Prenda) {
        val lista = _prendasSeleccionadas.value ?: mutableListOf()
        // Verificar si ya está seleccionada
        if (!lista.any { it.id == prenda.id }) {
            lista.add(prenda)
            _prendasSeleccionadas.value = lista
        }
    }

    fun removerPrendaSeleccionada(prenda: Prenda) {
        val lista = _prendasSeleccionadas.value ?: mutableListOf()
        lista.removeAll { it.id == prenda.id }
        _prendasSeleccionadas.value = lista
    }

    fun estaSeleccionada(prenda: Prenda): Boolean {
        return _prendasSeleccionadas.value?.any { it.id == prenda.id } ?: false
    }

    fun limpiarSeleccion() {
        _prendasSeleccionadas.value = mutableListOf()
    }
}