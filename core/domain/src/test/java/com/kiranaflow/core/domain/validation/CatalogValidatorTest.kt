package com.kiranaflow.core.domain.validation

import com.kiranaflow.core.model.BaseUnitType
import com.kiranaflow.core.model.CatalogItem
import com.kiranaflow.core.model.DisplayUnit
import com.kiranaflow.core.model.InventoryType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class CatalogValidatorTest {

    private lateinit var validator: CatalogValidator
    private lateinit var catalog: List<CatalogItem>

    @Before
    fun setUp() {
        validator = CatalogValidator()
        catalog = listOf(
            CatalogItem(
                id = "item_1",
                name = "Parle-G",
                aliases = """["parle g","paarle ji","biscuit"]""",
                pricePaise = 500L,
                baseUnit = BaseUnitType.PIECE,
                displayUnit = DisplayUnit.PCS,
                inventoryType = InventoryType.STOCK
            ),
            CatalogItem(
                id = "item_2",
                name = "Tata Salt 1kg",
                aliases = """["tata uppu","uppu","salt"]""",
                pricePaise = 2200L,
                baseUnit = BaseUnitType.GRAM,
                displayUnit = DisplayUnit.KG,
                inventoryType = InventoryType.STOCK
            ),
            CatalogItem(
                id = "item_3",
                name = "Good Day",
                aliases = """["good day biscuit","goodday"]""",
                pricePaise = 2000L,
                baseUnit = BaseUnitType.PIECE,
                displayUnit = DisplayUnit.PCS,
                inventoryType = InventoryType.STOCK
            )
        )
    }

    @Test
    fun testExactNameMatch() {
        val match = validator.findBestMatch("Parle-G", catalog)
        assertNotNull(match)
        assertEquals("item_1", match?.id)
    }

    @Test
    fun testAliasMatch() {
        val match = validator.findBestMatch("uppu", catalog)
        assertNotNull(match)
        assertEquals("item_2", match?.id)
    }

    @Test
    fun testFuzzyMatch_Levenshtein() {
        val match = validator.findBestMatch("parleg", catalog)
        assertNotNull(match)
        assertEquals("item_1", match?.id)
    }

    @Test
    fun testBlankOrUnknownQuery_returnsNull() {
        assertNull(validator.findBestMatch("", catalog))
        assertNull(validator.findBestMatch("nonexistent widget xyz", catalog))
    }
}
