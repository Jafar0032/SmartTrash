package com.jafar.smarttrash

data class User( // Firebase haur
    val nis: String,
    val nama: String,
    var score: Int? = 0
) {
    constructor() : this("", "", 0)
}
