package com.koba.inventario.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pickup")
data class PickupEntity(
    @PrimaryKey (autoGenerate = true)
    val pickupId: Long,
    val barcodeProduct: String,
    val barcodeLocation: String,
    val user: String,
    val amount: Int,
    val novelty: String,
    val indSync: Int
)
