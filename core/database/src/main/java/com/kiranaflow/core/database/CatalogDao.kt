package com.kiranaflow.core.database

import androidx.room.*
import com.kiranaflow.core.model.CatalogItem
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogDao {

    @Query("SELECT * FROM catalog_items WHERE isActive = 1 AND deletedAt IS NULL ORDER BY name ASC")
    fun getAllActive(): Flow<List<CatalogItem>>

    @Query("SELECT * FROM catalog_items WHERE id = :id AND deletedAt IS NULL LIMIT 1")
    suspend fun getById(id: String): CatalogItem?

    @Query("""
        SELECT * FROM catalog_items 
        WHERE isActive = 1 AND deletedAt IS NULL
          AND (name LIKE '%' || :query || '%' OR aliases LIKE '%' || :query || '%')
        ORDER BY 
            CASE WHEN name LIKE :query || '%' THEN 0 ELSE 1 END,
            name ASC
        LIMIT 15
    """)
    suspend fun search(query: String): List<CatalogItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CatalogItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CatalogItem>)

    @Update
    suspend fun update(item: CatalogItem)

    @Query("UPDATE catalog_items SET stockBaseUnits = stockBaseUnits - :deltaBaseUnits, updatedAt = :now WHERE id = :id")
    suspend fun decrementStock(id: String, deltaBaseUnits: Long, now: Long = System.currentTimeMillis())

    @Query("UPDATE catalog_items SET stockBaseUnits = :newStockBaseUnits, updatedAt = :now WHERE id = :id")
    suspend fun setStock(id: String, newStockBaseUnits: Long, now: Long = System.currentTimeMillis())

    @Query("UPDATE catalog_items SET reorderThresholdBaseUnits = :threshold, updatedAt = :now WHERE id = :id")
    suspend fun updateReorderThreshold(id: String, threshold: Long, now: Long = System.currentTimeMillis())

    @Query("SELECT * FROM catalog_items WHERE inventoryType = 'STOCK' AND stockBaseUnits <= reorderThresholdBaseUnits AND isActive = 1 AND deletedAt IS NULL")
    fun getLowStockItems(): Flow<List<CatalogItem>>

    @Query("SELECT * FROM catalog_items WHERE inventoryType = 'STOCK' AND stockBaseUnits <= reorderThresholdBaseUnits AND isActive = 1 AND deletedAt IS NULL")
    suspend fun getLowStockItemsList(): List<CatalogItem>

    @Query("UPDATE catalog_items SET deletedAt = :now, isActive = 0 WHERE id = :id")
    suspend fun softDelete(id: String, now: Long = System.currentTimeMillis())

    @Query("DELETE FROM catalog_items WHERE id = :id")
    suspend fun delete(id: String)
}
