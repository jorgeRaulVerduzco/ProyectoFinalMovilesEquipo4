package com.example.closetvirtual

data class Prenda(
    val imagen: Int,
    val nombre: String,
    val categoria: String,
    val color: String,
    val estampada: Boolean,
    val tags: List<String>
)