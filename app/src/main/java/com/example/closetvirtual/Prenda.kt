package com.example.closetvirtual

data class Prenda(
    var id: String = "",
    var imagen: String = "",
    var nombre: String = "",
    var categoria: String = "",
    var color: String = "",
    var estampada: Boolean = false,
    var tags: List<String> = emptyList()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "imagen"    to imagen,
            "nombre"    to nombre,
            "categoria" to categoria,
            "color"     to color,
            "estampada" to estampada,
            "tags"      to tags
        )
    }
}