package com.example.closetvirtual

data class RegistrosDiarios(
    var id: String = "",
    var fecha: String = "",
    var prendas: List<Prenda> = emptyList()
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "fecha" to fecha,
            "prendas" to prendas.map { it.toMap() }
        )
    }

    // Constructor secundario para cuando los datos vienen de Firestore como mapas
    companion object {
        fun fromMap(map: Map<String, Any>, id: String): RegistrosDiarios {
            val prendasMap = map["prendas"] as? List<Map<String, Any>> ?: emptyList()
            val prendas = prendasMap.map { prendasData ->
                val prenda = Prenda()
                prenda.id = prendasData["id"] as? String ?: ""
                prenda.imagen = prendasData["imagen"] as? String ?: ""
                prenda.nombre = prendasData["nombre"] as? String ?: ""
                prenda.categoria = prendasData["categoria"] as? String ?: ""
                prenda.color = prendasData["color"] as? String ?: ""
                prenda.estampada = prendasData["estampada"] as? Boolean ?: false
                prenda.tags = prendasData["tags"] as? List<String> ?: emptyList()
                prenda
            }

            return RegistrosDiarios(
                id = id,
                fecha = map["fecha"] as? String ?: "",
                prendas = prendas
            )
        }
    }
}
