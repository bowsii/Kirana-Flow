package com.kiranaflow.app.data.db

import androidx.room.*
import com.kiranaflow.app.data.model.CatalogItem
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogDao {

    @Query("SELECT * FROM catalog_items WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActive(): Flow<List<CatalogItem>>

    @Query("SELECT * FROM catalog_items WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): CatalogItem?

    /**
     * Full-text fuzzy search: name OR aliases contain the query.
     */
    @Query("""
        SELECT * FROM catalog_items 
        WHERE isActive = 1 
          AND (name LIKE '%' || :query || '%' OR aliases LIKE '%' || :query || '%')
        ORDER BY 
            CASE WHEN name LIKE :query || '%' THEN 0 ELSE 1 END,
            name ASC
        LIMIT 10
    """)
    suspend fun search(query: String): List<CatalogItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CatalogItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CatalogItem>)

    @Update
    suspend fun update(item: CatalogItem)

    @Query("UPDATE catalog_items SET stockQty = stockQty - :qty, updatedAt = :now WHERE id = :id")
    suspend fun decrementStock(id: Long, qty: Double, now: Long = System.currentTimeMillis())

    @Query("""
        UPDATE catalog_items 
        SET flowSoldToday = CASE WHEN flowDate = :date THEN flowSoldToday + :qty ELSE :qty END,
            flowDate = :date,
            updatedAt = :now
        WHERE id = :id
    """)
    suspend fun addFlowSale(id: Long, qty: Double, date: String, now: Long = System.currentTimeMillis())

    @Query("SELECT * FROM catalog_items WHERE inventoryType = 'STOCK' AND stockQty <= reorderThreshold AND isActive = 1")
    fun getLowStockItems(): Flow<List<CatalogItem>>

    @Query("SELECT * FROM catalog_items WHERE inventoryType = 'FLOW' AND flowDate = :date AND isActive = 1")
    suspend fun getFlowItemsForDate(date: String): List<CatalogItem>

    @Query("DELETE FROM catalog_items WHERE id = :id")
    suspend fun delete(id: Long)
}
