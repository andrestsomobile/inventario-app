package com.koba.inventario.database

import androidx.room.*

@Dao
interface TrafficDao {

    @Query("SELECT * FROM traffic WHERE  indSync = :indSync AND user = :user")
    suspend fun findTrafficByIndSync(indSync: Int, user: String) : List<TrafficEntity>

    @Query("SELECT * FROM traffic WHERE user = :user")
    suspend fun findAllTraffics(user: String) : List<TrafficEntity>

    @Query("SELECT * FROM traffic WHERE trafficCode = :trafficCode AND user = :user")
    suspend fun findByTrafficCode(trafficCode: Int, user: String) : List<TrafficEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun create(entity: TrafficEntity)

    @Update
    suspend fun update(entity: TrafficEntity)
}