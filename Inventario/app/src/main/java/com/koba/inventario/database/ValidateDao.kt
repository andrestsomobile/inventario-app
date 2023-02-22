package com.koba.inventario.database

import androidx.room.*

@Dao
interface ValidateDao {

    @Query("SELECT * FROM validate WHERE  indSync = :indSync AND user = :user")
    suspend fun findValidateByIndSync(indSync: Int, user: String) : List<ValidateEntity>

    @Query("SELECT * FROM validate_backup WHERE  indSync = :indSync AND user = :user")
    suspend fun findValidateByIndSyncBackup(indSync: Int, user: String) : List<ValidateBackupEntity>

    @Query("SELECT * FROM validate_backup WHERE  indSync = :indSync AND user = :user")
    suspend fun findValidateByIndSyncCopy(indSync: Int, user: String) : List<ValidateBackupEntity>

    @Query("SELECT * FROM validate WHERE user = :user")
    suspend fun findAllValidate(user: String) : List<ValidateEntity>

    @Query("SELECT * FROM validate WHERE barcodeProduct = :barcodeProduct AND barcodeLocation = :barcodeLocation AND user = :user AND id = :id")
    suspend fun findByBarcodeProduct(barcodeProduct: String,barcodeLocation: String, user: String, id: String) : List<ValidateEntity>

    @Query("SELECT * FROM validate WHERE barcodeProduct = :barcodeProduct AND barcodeLocation = :barcodeLocation AND amount = :amount AND user = :user AND id = :id")
    suspend fun findByBarcodeProduct(barcodeProduct: String,barcodeLocation: String, user: String, amount: String, id: String) : List<ValidateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun create(entity: ValidateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createCopy(entity: ValidateBackupEntity)

    @Update
    suspend fun update(entity: ValidateEntity)

    @Delete
    suspend fun delete(entity: ValidateEntity)

    @Query("DELETE FROM validate")
    suspend fun deleteAll()
}