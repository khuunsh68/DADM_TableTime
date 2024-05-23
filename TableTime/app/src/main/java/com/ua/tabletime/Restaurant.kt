package com.ua.tabletime

import java.sql.Time

data class Restaurant(
    val id: Int,
    val name: String,
    val endereco: String,
    val horario_abertura: Time,
    val horario_encerramento: Time,
    val avaliacao: Double,
    val imageResource: String,
    val cuisineType: String
) {
    constructor(
        imageResource: String,
        name: String,
        cuisineType: String,
        avaliacao: Double
    ) : this(0, name, "", Time(0), Time(0), avaliacao, imageResource, cuisineType)
}