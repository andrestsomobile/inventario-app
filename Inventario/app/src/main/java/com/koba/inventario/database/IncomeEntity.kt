package com.koba.inventario.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "income")
data class IncomeEntity(
    @PrimaryKey (autoGenerate = true)
    val incomeId: Long,
    val barcodeProduct: String,
    val user: String,
    val amount: Int,
    val indSync: Int
)
