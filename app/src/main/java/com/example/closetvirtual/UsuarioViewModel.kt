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


    fun loginUser(email: String, password: String) {
        _loginResult.value = AuthResult.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _loginResult.postValue(AuthResult.Success)
            } catch (e: Exception) {
                _loginResult.postValue(AuthResult.Error(e.localizedMessage ?: "Error desconocido"))
            }
        }
    }
}