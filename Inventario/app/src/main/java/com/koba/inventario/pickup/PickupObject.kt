package com.koba.inventario.pickup

import androidx.room.Entity
import androidx.room.PrimaryKey

data class PickupObject(
    val barcodeProduct: String,
    val barcodeLocation: String,
    var amount: Int
)
