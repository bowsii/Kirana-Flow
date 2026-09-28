package com.kiranaflow.app.data.db

import androidx.room.*
import com.kiranaflow.app.data.model.DraftCartEntity

@Dao
interface DraftCartDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDraft(draft: DraftCartEntity)

    @Query("SELECT * FROM draft_cart WHERE id = :id LIMIT 1")
    suspend fun getDraft(id: String = "ACTIVE_CART"): DraftCartEntity?

    @Query("DELETE FROM draft_cart WHERE id = :id")
    suspend fun clearDraft(id: String = "ACTIVE_CART")
}
