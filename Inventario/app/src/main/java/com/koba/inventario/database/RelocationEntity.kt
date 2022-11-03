package com.koba.inventario.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "relocation")
data class RelocationEntity(
    @PrimaryKey (autoGenerate = true)
    val relocationId: Long,
    val barcodeProduct: String,
    val barcodeOrigin: String,
    val barcodeDestination: String,
    val user: String,
    val amount: Int,
    val indSync: Int
)
