package com.koba.inventario.database

import androidx.room.*

@Dao
interface PickupDao {

    @Query("SELECT * FROM pickup WHERE  indSync = :indSync AND user = :user")
    suspend fun findPickupByIndSync(indSync: Int, user: String) : List<PickupEntity>

    @Query("SELECT * FROM pickup WHERE user = :user")
    suspend fun findAllPickup(user: String) : List<PickupEntity>

    @Query("SELECT * FROM pickup WHERE barcodeProduct = :barcodeProduct AND barcodeLocation = :barcodeLocation AND user = :user")
    suspend fun findByBarcodeProduct(barcodeProduct: String,barcodeLocation: String, user: String) : List<PickupEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun create(entity: PickupEntity)

    @Update
    suspend fun update(entity: PickupEntity)
}