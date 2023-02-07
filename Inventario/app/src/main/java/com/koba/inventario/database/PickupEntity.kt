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
    val requisitionNumber: String,
    val novelty: String,
    val requisitionId: String,
    val indSync: Int
)
