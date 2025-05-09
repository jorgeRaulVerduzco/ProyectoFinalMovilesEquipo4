package com.example.closetvirtual

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


sealed class AuthResult {
    object Loading : AuthResult()
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
}
class UsuarioViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    private val _registerResult = MutableLiveData<AuthResult>()
    val registerResult: LiveData<AuthResult> = _registerResult

    private val _loginResult = MutableLiveData<AuthResult>()
    val loginResult: LiveData<AuthResult> = _loginResult

    // Registra en Auth y luego guarda perfil en Firestore
    fun registerUser(usuario: Usuario, confirmPassword: String) {
        if (usuario.contraseña != confirmPassword) {
            _registerResult.value = AuthResult.Error("Las contraseñas no coinciden")
            return
        }
        _registerResult.value = AuthResult.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Verificar si ya existe un usuario con el mismo nombre de usuario
                val usernameQuery = db.collection("usuarios")
                    .whereEqualTo("usuario", usuario.usuario)
                    .get()
                    .await()

                if (!usernameQuery.isEmpty) {
                    _registerResult.postValue(AuthResult.Error("El nombre de usuario ya está en uso"))
                    return@launch
                }

                // 1) crear el Auth
                val authResult = auth
                    .createUserWithEmailAndPassword(usuario.email, usuario.contraseña)
                    .await()
                val uid = authResult.user?.uid ?: throw Exception("UID nulo")
                usuario.id = uid

                // 2) guardar datos extra en Firestore
                db.collection("usuarios")
                    .document(uid)
                    .set(usuario.toMap())
                    .await()

                _registerResult.postValue(AuthResult.Success)
            } catch (e: Exception) {
                _registerResult.postValue(AuthResult.Error(e.localizedMessage ?: "Error desconocido"))
            }
        }
    }


    fun loginUser(identifier: String, password: String) {
        _loginResult.value = AuthResult.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Si no tiene '@', asumimos que es nombre de usuario
                val emailToUse = if ('@' in identifier) {
                    identifier
                } else {
                    // Buscamos el email en Firestore
                    val query = db.collection("usuarios")
                        .whereEqualTo("usuario", identifier)
                        .get()
                        .await()
                    if (query.isEmpty) {
                        throw Exception("Usuario no registrado")
                    }
                    query.documents[0].getString("email")
                        ?: throw Exception("Email no encontrado para ese usuario")
                }

                // Autenticamos con el email obtenido
                auth.signInWithEmailAndPassword(emailToUse, password).await()
                _loginResult.postValue(AuthResult.Success)
            } catch (e: Exception) {
                _loginResult.postValue(AuthResult.Error(e.localizedMessage ?: "Error desconocido"))
            }
        }
    }
}