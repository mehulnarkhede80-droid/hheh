package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TransitDemandRecord
import com.example.data.model.TransitRoute
import kotlinx.coroutines.flow.Flow

@Dao
interface TransitDao {

    // Routes
    @Query("SELECT * FROM transit_routes ORDER BY routeId ASC")
    fun getAllRoutes(): Flow<List<TransitRoute>>

    @Query("SELECT * FROM transit_routes ORDER BY routeId ASC")
    suspend fun getAllRoutesSync(): List<TransitRoute>

    @Query("SELECT * FROM transit_routes WHERE routeId = :routeId LIMIT 1")
    suspend fun getRouteById(routeId: String): TransitRoute?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: TransitRoute)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutes(routes: List<TransitRoute>)

    @Query("DELETE FROM transit_routes WHERE routeId = :routeId")
    suspend fun deleteRouteById(routeId: String)

    @Query("DELETE FROM transit_routes")
    suspend fun clearAllRoutes()

    // Demand Records
    @Query("SELECT * FROM transit_demand_records ORDER BY timestamp DESC")
    fun getAllDemandRecords(): Flow<List<TransitDemandRecord>>

    @Query("SELECT * FROM transit_demand_records WHERE routeId = :routeId ORDER BY timestamp DESC")
    fun getRecordsForRoute(routeId: String): Flow<List<TransitDemandRecord>>

    @Query("SELECT * FROM transit_demand_records WHERE routeId = :routeId ORDER BY timestamp DESC")
    suspend fun getRecordsForRouteSync(routeId: String): List<TransitDemandRecord>

    @Query("SELECT * FROM transit_demand_records ORDER BY timestamp DESC")
    suspend fun getAllRecordsSync(): List<TransitDemandRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDemandRecord(record: TransitDemandRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDemandRecords(records: List<TransitDemandRecord>)

    @Query("DELETE FROM transit_demand_records WHERE id = :id")
    suspend fun deleteDemandRecordById(id: Long)

    @Query("DELETE FROM transit_demand_records WHERE routeId = :routeId")
    suspend fun deleteRecordsForRoute(routeId: String)

    @Query("DELETE FROM transit_demand_records")
    suspend fun clearAllDemandRecords()

    @Query("SELECT COUNT(*) FROM transit_demand_records")
    suspend fun getRecordCount(): Int

    @Query("SELECT COUNT(*) FROM transit_routes")
    suspend fun getRouteCount(): Int
}
