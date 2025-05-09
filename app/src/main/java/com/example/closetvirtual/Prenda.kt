package com.example.closetvirtual

data class Prenda(
    var id: String = "",
    var imagen: String = "",
    var nombre: String = "",
    var categoria: String = "",
    var color: String = "",
    var estampada: Boolean = false,
    var tags: List<String> = emptyList(),
    var usosTotales: Int = 0,
    var usosRegistrosDiarios: Int = 0,
    var usosOutfits: Int = 0,
    var usosPorMes: Map<String, Int> = emptyMap()
) {
    // Modificar el método toMap para incluir los nuevos campos
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "imagen" to imagen,
            "nombre" to nombre,
            "categoria" to categoria,
            "color" to color,
            "estampada" to estampada,
            "tags" to tags,
            "usosTotales" to usosTotales,
            "usosRegistrosDiarios" to usosRegistrosDiarios,
            "usosOutfits" to usosOutfits,
            "usosPorMes" to usosPorMes
        )
    }
}