package com.koba.inventario.database

import androidx.room.*

@Dao
interface PositionDao {

    @Query("SELECT * FROM position WHERE  indSync = :indSync AND user = :user")
    suspend fun findPositionByIndSync(indSync: Int, user: String) : List<PositionEntity>

    @Query("SELECT * FROM position WHERE user = :user")
    suspend fun findAllPosition(user: String) : List<PositionEntity>

    @Query("SELECT * FROM position WHERE barcodeProduct = :barcodeProduct AND barcodeLocation = :barcodeLocation AND user = :user")
    suspend fun findByBarcodeProduct(barcodeProduct: String,barcodeLocation: String, user: String) : List<PositionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun create(entity: PositionEntity)

    @Update
    suspend fun update(entity: PositionEntity)
}