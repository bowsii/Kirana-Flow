package com.kiranaflow.core.database

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions
import com.kiranaflow.core.model.CatalogItem

@Entity(tableName = "catalog_items_fts")
@Fts4(
    contentEntity = CatalogItem::class,
    tokenizer = FtsOptions.TOKENIZER_UNICODE61
)
data class CatalogItemFts(
    val name: String,
    val aliases: String
)
