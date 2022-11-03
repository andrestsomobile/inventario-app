package com.koba.inventario.database

import androidx.room.*

@Dao
interface ValidateDao {

    @Query("SELECT * FROM validate WHERE  indSync = :indSync AND user = :user")
    suspend fun findValidateByIndSync(indSync: Int, user: String) : List<ValidateEntity>

    @Query("SELECT * FROM validate WHERE user = :user")
    suspend fun findAllValidate(user: String) : List<ValidateEntity>

    @Query("SELECT * FROM validate WHERE barcodeProduct = :barcodeProduct AND barcodeLocation = :barcodeLocation AND user = :user")
    suspend fun findByBarcodeProduct(barcodeProduct: String,barcodeLocation: String, user: String) : List<ValidateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun create(entity: ValidateEntity)

    @Update
    suspend fun update(entity: ValidateEntity)
}