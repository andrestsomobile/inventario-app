package com.koba.inventario.database

import androidx.room.*

@Dao
interface RelocationDao {

    @Query("SELECT * FROM relocation WHERE  indSync = :indSync AND user = :user")
    suspend fun findRelocationByIndSync(indSync: Int, user: String) : List<RelocationEntity>

    @Query("SELECT * FROM relocation WHERE user = :user")
    suspend fun findAllRelocation(user: String) : List<RelocationEntity>

    @Query("SELECT * FROM relocation WHERE barcodeProduct = :barcodeProduct AND barcodeOrigin = :barcodeOrigin AND barcodeDestination = :barcodeDestination AND user = :user")
    suspend fun findByBarcodeProduct(barcodeProduct: String,barcodeOrigin: String, barcodeDestination: String, user: String) : List<RelocationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun create(entity: RelocationEntity)

    @Update
    suspend fun update(entity: RelocationEntity)
}