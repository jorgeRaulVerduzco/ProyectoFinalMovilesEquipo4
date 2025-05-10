package com.example.closetvirtual
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class OutfitsViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()  // Añadir Firebase Auth

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

    // Obtener el ID del usuario actual
    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: ""
    }

    fun obtenerOutfits() {
        val userId = getCurrentUserId()
        if (userId.isEmpty()) {
            _errorMessage.value = "Usuario no autenticado"
            return
        }

        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snapshot = db.collection("outfits")
                    .whereEqualTo("usuarioId", userId)  // Filtrar por ID de usuario actual

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

        val userId = getCurrentUserId()
        if (userId.isEmpty()) {
            _errorMessage.value = "Usuario no autenticado"
            return
        }

        _isLoading.value = true
        val prendas = _prendasSeleccionadas.value ?: mutableListOf()
        val nuevoOutfit = Outfits(
            id = UUID.randomUUID().toString(),
            nombre = nombre,
            items = prendas,
            usuarioId = userId  // Asignar el ID del usuario actual
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Guardar el outfit
                db.collection("outfits")
                    .document(nuevoOutfit.id)
                    .set(nuevoOutfit.toMap())
                    .await()

                // Actualizar los contadores de uso para cada prenda
                val fechaActual = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date())
                for (prenda in prendas) {
                    // Obtener la prenda actual de Firestore
                    val prendaDoc = db.collection("prendas").document(prenda.id).get().await()
                    val prendaData = prendaDoc.toObject(Prenda::class.java)
                    if (prendaData != null) {
                        // Actualizar contadores
                        val usosTotales = (prendaData.usosTotales ?: 0) + 1
                        val usosOutfits = (prendaData.usosOutfits ?: 0) + 1

                        // Actualizar usos por mes
                        val usosPorMes = prendaData.usosPorMes?.toMutableMap() ?: mutableMapOf()
                        usosPorMes[fechaActual] = (usosPorMes[fechaActual] ?: 0) + 1

                        // Actualizar la prenda en Firestore
                        db.collection("prendas").document(prenda.id).update(
                            mapOf(
                                "usosTotales" to usosTotales,
                                "usosOutfits" to usosOutfits,
                                "usosPorMes" to usosPorMes
                            )
                        ).await()
                    }
                }

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
        val userId = getCurrentUserId()
        if (userId.isEmpty()) {
            _errorMessage.value = "Usuario no autenticado"
            return
        }

        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Verificar que el outfit pertenezca al usuario actual
                val outfitDoc = db.collection("outfits").document(id).get().await()
                val data = outfitDoc.data
                if (data != null && data["usuarioId"] == userId) {
                    // Si pertenece al usuario, proceder con la eliminación
                    db.collection("outfits")
                        .document(id)
                        .delete()
                        .await()

                    withContext(Dispatchers.Main) {
                        onComplete?.invoke()
                        obtenerOutfits()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _errorMessage.value = "No tienes permiso para eliminar este outfit"
                    }
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
        val userId = getCurrentUserId()
        if (userId.isEmpty()) {
            _errorMessage.value = "Usuario no autenticado"
            callback(null)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val doc = db.collection("outfits").document(id).get().await()
                val data = doc.data

                // Verificar que el outfit pertenezca al usuario actual
                if (data != null && data["usuarioId"] == userId) {
                    val outfit = Outfits.fromMap(data, doc.id)
                    withContext(Dispatchers.Main) {
                        callback(outfit)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _errorMessage.value = "No tienes permiso para ver este outfit"
                        callback(null)
                    }
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