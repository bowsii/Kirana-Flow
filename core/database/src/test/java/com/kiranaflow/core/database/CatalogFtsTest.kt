package com.kiranaflow.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.kiranaflow.core.model.BaseUnitType
import com.kiranaflow.core.model.CatalogItem
import com.kiranaflow.core.model.DisplayUnit
import com.kiranaflow.core.model.InventoryType
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CatalogFtsTest {

    private lateinit var db: KiranaFlowDatabase
    private lateinit var catalogDao: CatalogDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, KiranaFlowDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        catalogDao = db.catalogDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testFts_unicode61_matchesTamilScriptAlias() = runTest {
        val toorDal = CatalogItem(
            id = "item_toor_dal",
            name = "Toor Dal 1kg",
            aliases = """["துவரம் பருப்பு", "paruppu", "toor dhal"]""",
            pricePaise = 16000,
            baseUnit = BaseUnitType.GRAM,
            displayUnit = DisplayUnit.KG,
            inventoryType = InventoryType.STOCK,
            stockBaseUnits = 50000L
        )

        val tataSalt = CatalogItem(
            id = "item_tata_salt",
            name = "Tata Salt 1kg",
            aliases = """["உப்பு", "uppu", "tata salt"]""",
            pricePaise = 2800,
            baseUnit = BaseUnitType.GRAM,
            displayUnit = DisplayUnit.KG,
            inventoryType = InventoryType.STOCK,
            stockBaseUnits = 25000L
        )

        val sugar = CatalogItem(
            id = "item_sugar",
            name = "Sugar 1kg",
            aliases = """["சர்க்கரை", "sakkarai", "seeni"]""",
            pricePaise = 4400,
            baseUnit = BaseUnitType.GRAM,
            displayUnit = DisplayUnit.KG,
            inventoryType = InventoryType.STOCK,
            stockBaseUnits = 40000L
        )

        catalogDao.insertAll(listOf(toorDal, tataSalt, sugar))

        // Query by Tamil script alias: பருப்பு (paruppu)
        val dalResults = catalogDao.searchFts("பருப்பு*")
        assertEquals("Should find exactly 1 item for பருப்பு*", 1, dalResults.size)
        assertEquals("item_toor_dal", dalResults[0].id)
        assertEquals("Toor Dal 1kg", dalResults[0].name)

        // Query by Tamil script alias: உப்பு (uppu / salt)
        val saltResults = catalogDao.searchFts("உப்பு*")
        assertEquals("Should find exactly 1 item for உப்பு*", 1, saltResults.size)
        assertEquals("item_tata_salt", saltResults[0].id)

        // Query by Tamil script alias: சர்க்கரை (sakkarai / sugar)
        val sugarResults = catalogDao.searchFts("சர்க்கரை*")
        assertEquals("Should find exactly 1 item for சர்க்கரை*", 1, sugarResults.size)
        assertEquals("item_sugar", sugarResults[0].id)
    }

    @Test
    fun testFts_contentEntity_syncOnUpdateAndInsert() = runTest {
        val item = CatalogItem(
            id = "item_cardamom",
            name = "Cardamom 50g",
            aliases = """["ஏலக்காய்", "elakkai"]""",
            pricePaise = 12000,
            baseUnit = BaseUnitType.GRAM,
            displayUnit = DisplayUnit.G,
            inventoryType = InventoryType.STOCK,
            stockBaseUnits = 1000L
        )

        catalogDao.insert(item)

        // Verify initial FTS indexing of Tamil alias
        val initialMatch = catalogDao.searchFts("ஏலக்காய்*")
        assertEquals(1, initialMatch.size)
        assertEquals("item_cardamom", initialMatch[0].id)

        // Update item name and aliases
        val updatedItem = item.copy(
            name = "Green Cardamom 50g",
            aliases = """["பச்சை ஏலக்காய்", "elachi"]"""
        )
        catalogDao.update(updatedItem)

        // Verify triggers synced with contentEntity
        val updatedMatch = catalogDao.searchFts("பச்சை*")
        assertEquals(1, updatedMatch.size)
        assertEquals("Green Cardamom 50g", updatedMatch[0].name)
    }
}
