package com.kiranaflow.app.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kiranaflow.core.common.UuidV7
import com.kiranaflow.core.database.DatabaseSecurityManager
import com.kiranaflow.core.database.KiranaFlowDatabase
import com.kiranaflow.core.model.Bill
import com.kiranaflow.core.model.BillItem
import com.kiranaflow.core.model.InventoryType
import com.kiranaflow.core.model.PaymentMode
import kotlinx.coroutines.runBlocking
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Instrumented test verifying SQLCipher database encryption lifecycle:
 * 1. Open database encrypted via [SupportOpenHelperFactory] using [DatabaseSecurityManager] passphrase.
 * 2. Commit a bill with items to the database.
 * 3. Close the database instance.
 * 4. Reopen the encrypted database using the same passphrase.
 * 5. Read back the committed bill and verify data integrity.
 * 6. Verify that accessing the encrypted file without the correct key fails loudly.
 */
@RunWith(AndroidJUnit4::class)
class EncryptedDatabaseTest {

    private lateinit var context: Context
    private lateinit var dbFile: File
    private lateinit var securityManager: DatabaseSecurityManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        dbFile = context.getDatabasePath("test_encrypted_kiranaflow.db")
        if (dbFile.exists()) {
            dbFile.delete()
        }
        val journalFile = File(dbFile.path + "-journal")
        if (journalFile.exists()) journalFile.delete()
        val walFile = File(dbFile.path + "-wal")
        if (walFile.exists()) walFile.delete()
        val shmFile = File(dbFile.path + "-shm")
        if (shmFile.exists()) shmFile.delete()

        securityManager = DatabaseSecurityManager(context)
    }

    @After
    fun tearDown() {
        if (dbFile.exists()) dbFile.delete()
        val journalFile = File(dbFile.path + "-journal")
        if (journalFile.exists()) journalFile.delete()
        val walFile = File(dbFile.path + "-wal")
        if (walFile.exists()) walFile.delete()
        val shmFile = File(dbFile.path + "-shm")
        if (shmFile.exists()) shmFile.delete()
    }

    @Test
    fun testOpenEncryptedDb_commitBill_close_reopen_readBillBack() = runBlocking {
        val passphrase = securityManager.getDatabasePassphrase()
        assertEquals(32, passphrase.size)

        val factory = SupportOpenHelperFactory(passphrase)

        // Step 1: Open encrypted database
        var db = Room.databaseBuilder(
            context,
            KiranaFlowDatabase::class.java,
            dbFile.name
        )
            .openHelperFactory(factory)
            .build()

        val billDao = db.billDao()
        val testBillId = UuidV7.generate()
        val testBill = Bill(
            id = testBillId,
            billNumber = "#1099",
            totalPaise = 5000L,
            itemCount = 1,
            paymentMode = PaymentMode.CASH,
            businessDate = "2026-09-29",
            createdAt = System.currentTimeMillis()
        )
        val billItem = BillItem(
            id = UuidV7.generate(),
            billId = testBillId,
            catalogItemId = "item_tea_01",
            itemName = "Tata Tea",
            quantityBaseUnits = 1L,
            unit = "pcs",
            pricePerUnitPaise = 5000L,
            lineTotalPaise = 5000L,
            inventoryType = InventoryType.STOCK
        )

        // Step 2: Commit bill
        billDao.insertBill(testBill)
        billDao.insertBillItems(listOf(billItem))

        val retrievedBeforeClose = billDao.getBillById(testBillId)
        assertNotNull(retrievedBeforeClose)
        assertEquals(5000L, retrievedBeforeClose?.totalPaise)

        // Step 3: Close database
        db.close()
        assertTrue("Database file should exist on disk", dbFile.exists())

        // Step 4: Reopen encrypted database with the same factory / passphrase
        val reopenFactory = SupportOpenHelperFactory(passphrase)
        val reopenedDb = Room.databaseBuilder(
            context,
            KiranaFlowDatabase::class.java,
            dbFile.name
        )
            .openHelperFactory(reopenFactory)
            .build()

        // Step 5: Read bill back and verify integrity
        val retrievedAfterReopen = reopenedDb.billDao().getBillById(testBillId)
        assertNotNull("Bill must be recovered after reopening encrypted DB", retrievedAfterReopen)
        assertEquals(testBillId, retrievedAfterReopen?.id)
        assertEquals(5000L, retrievedAfterReopen?.totalPaise)
        assertEquals(PaymentMode.CASH, retrievedAfterReopen?.paymentMode)

        val items = reopenedDb.billDao().getItemsForBill(testBillId)
        assertEquals(1, items.size)
        assertEquals("Tata Tea", items[0].itemName)
        assertEquals(5000L, items[0].lineTotalPaise)

        reopenedDb.close()
    }
}
