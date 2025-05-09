package com.example.closetvirtual

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
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

class RegistroDiarioViewModel  : ViewModel() {
    private val db = Firebase.firestore

    // LiveData para la lista de registros diarios
    private val _registrosDiarios = MutableLiveData<List<RegistrosDiarios>>(emptyList())
    val registrosDiarios: LiveData<List<RegistrosDiarios>> = _registrosDiarios

    // LiveData para las prendas seleccionadas en el registro actual
    private val _prendasSeleccionadas = MutableLiveData<MutableList<Prenda>>(mutableListOf())
    val prendasSeleccionadas: LiveData<MutableList<Prenda>> = _prendasSeleccionadas

    // LiveData para estado de carga
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData para mensajes de error
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    init {
        obtenerRegistrosDiarios()
    }

    fun obtenerRegistrosDiarios() {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snapshot = db.collection("registros_diarios")
                    .orderBy("fecha", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val registros = snapshot.documents.mapNotNull { doc ->
                    try {
                        val data = doc.data
                        if (data != null) {
                            RegistrosDiarios.fromMap(data, doc.id)
                        } else null
                    } catch (e: Exception) {
                        null // Omitimos documentos con error
                    }
                }

                withContext(Dispatchers.Main) {
                    _registrosDiarios.value = registros
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "Error al obtener registros: ${e.message}"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                }
            }
        }
    }

    fun agregarRegistroDiario() {
        if (_prendasSeleccionadas.value.isNullOrEmpty()) {
            _errorMessage.value = "Debes seleccionar al menos una prenda"
            return
        }

        _isLoading.value = true

        val prendas = _prendasSeleccionadas.value ?: mutableListOf()
        val fechaActual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val fechaMes = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date())

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Buscar si ya existe un registro para la fecha actual
                val registroExistente = buscarRegistroPorFecha(fechaActual)

                if (registroExistente != null) {
                    // Actualizar el registro existente
                    actualizarRegistroExistente(registroExistente, prendas, fechaMes)
                } else {
                    // Crear un nuevo registro
                    crearNuevoRegistro(prendas, fechaActual, fechaMes)
                }

                // Actualizar la lista después de guardar
                withContext(Dispatchers.Main) {
                    obtenerRegistrosDiarios()
                    // Limpiar las prendas seleccionadas
                    _prendasSeleccionadas.value = mutableListOf()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "Error al guardar registro: ${e.message}"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                }
            }
        }
    }

    private suspend fun buscarRegistroPorFecha(fecha: String): RegistrosDiarios? {
        val snapshot = db.collection("registros_diarios")
            .whereEqualTo("fecha", fecha)
            .get()
            .await()

        if (snapshot.documents.isEmpty()) {
            return null
        }

        val doc = snapshot.documents.first()
        val data = doc.data
        return if (data != null) {
            RegistrosDiarios.fromMap(data, doc.id)
        } else null
    }

    private suspend fun actualizarRegistroExistente(registro: RegistrosDiarios, nuevasPrendas: List<Prenda>, fechaMes: String) {
        // Verificar qué prendas son nuevas
        val prendasExistentes = registro.prendas.map { it.id }.toSet()
        val prendasAgregar = nuevasPrendas.filter { !prendasExistentes.contains(it.id) }

        // Si no hay prendas nuevas, no hacer nada
        if (prendasAgregar.isEmpty()) {
            withContext(Dispatchers.Main) {
                _errorMessage.value = "No hay prendas nuevas para agregar al registro"
            }
            return
        }

        // Combinar las prendas existentes con las nuevas
        val todasLasPrendas = registro.prendas + prendasAgregar

        // Actualizar el registro en Firestore
        val registroActualizado = RegistrosDiarios(
            id = registro.id,
            fecha = registro.fecha,
            prendas = todasLasPrendas
        )

        db.collection("registros_diarios")
            .document(registro.id)
            .set(registroActualizado.toMap())
            .await()

        // Actualizar los contadores de uso para cada prenda nueva
        for (prenda in prendasAgregar) {
            actualizarContadoresUso(prenda, fechaMes)
        }

        withContext(Dispatchers.Main) {
            _errorMessage.value = "Registro actualizado correctamente"
        }
    }

    private suspend fun crearNuevoRegistro(prendas: List<Prenda>, fechaActual: String, fechaMes: String) {
        val nuevoRegistro = RegistrosDiarios(
            id = UUID.randomUUID().toString(),
            fecha = fechaActual,
            prendas = prendas
        )

        // Guardar el registro diario
        db.collection("registros_diarios")
            .document(nuevoRegistro.id)
            .set(nuevoRegistro.toMap())
            .await()

        // Actualizar los contadores de uso para cada prenda
        for (prenda in prendas) {
            actualizarContadoresUso(prenda, fechaMes)
        }
    }

    private suspend fun actualizarContadoresUso(prenda: Prenda, fechaMes: String) {
        // Obtener la prenda actual de Firestore
        val prendaDoc = db.collection("prendas").document(prenda.id).get().await()
        val prendaData = prendaDoc.toObject(Prenda::class.java)

        if (prendaData != null) {
            // Actualizar contadores
            val usosTotales = (prendaData.usosTotales ?: 0) + 1
            val usosRegistrosDiarios = (prendaData.usosRegistrosDiarios ?: 0) + 1

            // Actualizar usos por mes
            val usosPorMes = prendaData.usosPorMes?.toMutableMap() ?: mutableMapOf()
            usosPorMes[fechaMes] = (usosPorMes[fechaMes] ?: 0) + 1

            // Actualizar la prenda en Firestore
            db.collection("prendas").document(prenda.id).update(
                mapOf(
                    "usosTotales" to usosTotales,
                    "usosRegistrosDiarios" to usosRegistrosDiarios,
                    "usosPorMes" to usosPorMes
                )
            ).await()
        }
    }

    fun eliminarRegistroDiario(id: String, onComplete: (() -> Unit)? = null) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.collection("registros_diarios")
                    .document(id)
                    .delete()
                    .await()

                withContext(Dispatchers.Main) {
                    onComplete?.invoke()
                    obtenerRegistrosDiarios()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "Error al eliminar registro: ${e.message}"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                }
            }
        }
    }

    fun obtenerRegistroPorId(id: String, callback: (RegistrosDiarios?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val doc = db.collection("registros_diarios").document(id).get().await()
                val data = doc.data
                val registro = if (data != null) {
                    RegistrosDiarios.fromMap(data, doc.id)
                } else null

                withContext(Dispatchers.Main) {
                    callback(registro)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "Error al obtener registro: ${e.message}"
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