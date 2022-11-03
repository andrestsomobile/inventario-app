package com.koba.inventario.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "position")
data class PositionEntity(
    @PrimaryKey (autoGenerate = true)
    val positionId: Long,
    val barcodeProduct: String,
    val barcodeLocation: String,
    val user: String,
    val amount: Int,
    val indSync: Int,
    val trafficId: Int
)
