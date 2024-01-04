package com.jafar.smarttrash

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User( // Firebase harus ad constructor kosong
    val nis: String,
    val nama: String,
    var score: Int? = 0
) : Parcelable {
    constructor() : this("", "", 0)
}
