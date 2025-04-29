package com.example.closetvirtual

data class Usuario(
    var id: String = "",
    var nombres: String = "",
    var apellidos: String = "",
    var email: String = "",
    var usuario: String = "",
    var contraseña: String = ""
) {

    fun toMap(): Map<String, String> {
        return mapOf(
            "id" to id,
            "nombres" to nombres,
            "apellidos" to apellidos,
            "email" to email,
            "usuario" to usuario,
            "contraseña" to contraseña
        )
    }
}
