package com.koba.inventario.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "traffic")
data class TrafficEntity(
    @PrimaryKey (autoGenerate = true)
    val trafficId: Long,
    val trafficCode: Int,
    val user: String,
    val indSync: Int
)
