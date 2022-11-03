package com.koba.inventario.database

import androidx.room.*

@Dao
interface IncomeDao {

    @Query("SELECT * FROM income WHERE  indSync = :indSync AND user = :user")
    suspend fun findIncomeByIndSync(indSync: Int, user: String) : List<IncomeEntity>

    @Query("SELECT * FROM income WHERE user = :user")
    suspend fun findAllIncome(user: String) : List<IncomeEntity>

    @Query("SELECT * FROM income WHERE barcodeProduct = :barcodeProduct AND user = :user")
    suspend fun findByBarcodeProduct(barcodeProduct: String, user: String) : List<IncomeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun create(entity: IncomeEntity)

    @Update
    suspend fun update(entity: IncomeEntity)
}