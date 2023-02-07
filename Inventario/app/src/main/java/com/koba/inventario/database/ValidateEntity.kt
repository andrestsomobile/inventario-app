package com.koba.inventario.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "validate")
data class ValidateEntity(
    @PrimaryKey (autoGenerate = true)
    val validateId: Long,
    val barcodeProduct: String,
    val barcodeLocation: String,
    val amount : String,
    val user: String,
    val id: String,
    val indSync: Int
)
