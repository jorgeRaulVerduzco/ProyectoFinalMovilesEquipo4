package com.example.closetvirtual

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Outfits(
    var id: String = "",
    val nombre: String = "",
    val items: List<Prenda> = emptyList(),
    val usuarioId: String = "",
    val fecha: String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())  // Fecha como String formateado
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "nombre" to nombre,
            "items" to items.map { it.toMap() },
            "usuarioId" to usuarioId,
            "fecha" to fecha  // Guardamos la fecha como String formateado
        )
    }

    // Constructor secundario para cuando los datos vienen de Firestore como mapas
    companion object {
        fun fromMap(map: Map<String, Any>, id: String): Outfits {
            val itemsMap = map["items"] as? List<Map<String, Any>> ?: emptyList()
            val prendas = itemsMap.map { itemData ->
                val prenda = Prenda()
                prenda.id = itemData["id"] as? String ?: ""
                prenda.imagen = itemData["imagen"] as? String ?: ""
                prenda.nombre = itemData["nombre"] as? String ?: ""
                prenda.categoria = itemData["categoria"] as? String ?: ""
                prenda.color = itemData["color"] as? String ?: ""
                prenda.estampada = itemData["estampada"] as? Boolean ?: false
                prenda.tags = itemData["tags"] as? List<String> ?: emptyList()
                prenda
            }
            return Outfits(
                id = id,
                nombre = map["nombre"] as? String ?: "",
                items = prendas,
                usuarioId = map["usuarioId"] as? String ?: "",
                fecha = map["fecha"] as? String ?: SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            )
        }
    }
}