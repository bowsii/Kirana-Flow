package com.kiranaflow.core.database

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kiranaflow.core.model.*
import com.kiranaflow.core.common.UuidV7
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(
    entities = [
        CatalogItem::class,
        Bill::class,
        BillItem::class,
        BillJournal::class,
        StockMovement::class,
        FlowDaily::class,
        DraftCartEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class KiranaFlowDatabase : RoomDatabase() {

    abstract fun catalogDao(): CatalogDao
    abstract fun billDao(): BillDao
    abstract fun journalDao(): JournalDao
    abstract fun stockMovementDao(): StockMovementDao
    abstract fun flowDailyDao(): FlowDailyDao
    abstract fun draftCartDao(): DraftCartDao

    companion object {
        init {
            try {
                System.loadLibrary("sqlcipher")
            } catch (_: UnsatisfiedLinkError) {
                // Handled in environments where native libs are bundled or mocked
            }
        }

        @Volatile
        private var INSTANCE: KiranaFlowDatabase? = null

        fun resetInstanceForTesting() {
            INSTANCE = null
        }

        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `bill_journal` (
                        `journalId` TEXT NOT NULL,
                        `billId` TEXT NOT NULL,
                        `payloadJson` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `appliedAt` INTEGER,
                        PRIMARY KEY(`journalId`)
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_bill_journal_billId` ON `bill_journal` (`billId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_bill_journal_status` ON `bill_journal` (`status`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_bill_journal_createdAt` ON `bill_journal` (`createdAt`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `stock_movements` (
                        `id` TEXT NOT NULL,
                        `itemId` TEXT NOT NULL,
                        `deltaBaseUnits` INTEGER NOT NULL,
                        `reason` TEXT NOT NULL,
                        `refId` TEXT NOT NULL,
                        `businessDate` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_movements_itemId` ON `stock_movements` (`itemId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_movements_businessDate` ON `stock_movements` (`businessDate`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_movements_createdAt` ON `stock_movements` (`createdAt`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_movements_refId` ON `stock_movements` (`refId`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `flow_daily` (
                        `id` TEXT NOT NULL,
                        `itemId` TEXT NOT NULL,
                        `businessDate` TEXT NOT NULL,
                        `soldBaseUnits` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_flow_daily_itemId` ON `flow_daily` (`itemId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_flow_daily_businessDate` ON `flow_daily` (`businessDate`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_flow_daily_itemId_businessDate` ON `flow_daily` (`itemId`, `businessDate`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `draft_cart` (
                        `id` TEXT NOT NULL,
                        `cartJson` TEXT NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """)
            }
        }

        fun getInstance(
            context: Context,
            securityManager: DatabaseSecurityManager = DatabaseSecurityManager(context.applicationContext)
        ): KiranaFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val passphrase = securityManager.getDatabasePassphrase()
                val factory = SupportOpenHelperFactory(passphrase)
                Room.databaseBuilder(
                    context.applicationContext,
                    KiranaFlowDatabase::class.java,
                    "kiranaflow.db"
                )
                    .openHelperFactory(factory)
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    seedCatalog(database.catalogDao())
                                }
                            }
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
        }

        suspend fun seedCatalog(dao: CatalogDao) {
            val items = listOf(
                // ── STOCK items (Counted shelf goods) ──
                CatalogItem(
                    id = UuidV7.generate(),
                    name = "Parle-G",
                    aliases = """["parleg","parle g","paarle ji","biscuit"]""",
                    pricePaise = 500L,
                    baseUnit = BaseUnitType.PIECE,
                    displayUnit = DisplayUnit.PCS,
                    inventoryType = InventoryType.STOCK,
                    stockBaseUnits = 50L,
                    reorderThresholdBaseUnits = 10L
                ),
                CatalogItem(
                    id = UuidV7.generate(),
                    name = "Good Day",
                    aliases = """["good day biscuit","goodday"]""",
                    pricePaise = 2000L,
                    baseUnit = BaseUnitType.PIECE,
                    displayUnit = DisplayUnit.PCS,
                    inventoryType = InventoryType.STOCK,
                    stockBaseUnits = 30L,
                    reorderThresholdBaseUnits = 5L
                ),
                CatalogItem(
                    id = UuidV7.generate(),
                    name = "Tata Salt 1kg",
                    aliases = """["tata uppu","uppu","salt","tata salt"]""",
                    pricePaise = 2200L,
                    baseUnit = BaseUnitType.PIECE,
                    displayUnit = DisplayUnit.PACK,
                    inventoryType = InventoryType.STOCK,
                    stockBaseUnits = 20L,
                    reorderThresholdBaseUnits = 3L
                ),
                CatalogItem(
                    id = UuidV7.generate(),
                    name = "Amul Butter",
                    aliases = """["amul","butter","vennai"]""",
                    pricePaise = 5500L,
                    baseUnit = BaseUnitType.PIECE,
                    displayUnit = DisplayUnit.PACK,
                    inventoryType = InventoryType.STOCK,
                    stockBaseUnits = 15L,
                    reorderThresholdBaseUnits = 3L
                ),
                CatalogItem(
                    id = UuidV7.generate(),
                    name = "Dettol Soap",
                    aliases = """["dettol","soap","saappu","dettol soap"]""",
                    pricePaise = 4500L,
                    baseUnit = BaseUnitType.PIECE,
                    displayUnit = DisplayUnit.PCS,
                    inventoryType = InventoryType.STOCK,
                    stockBaseUnits = 25L,
                    reorderThresholdBaseUnits = 5L
                ),
                CatalogItem(
                    id = UuidV7.generate(),
                    name = "Fortune Oil 1L",
                    aliases = """["fortune","oil","ennai","cooking oil"]""",
                    pricePaise = 13500L,
                    baseUnit = BaseUnitType.MILLILITER,
                    displayUnit = DisplayUnit.L,
                    inventoryType = InventoryType.STOCK,
                    stockBaseUnits = 12000L, // 12 liters
                    reorderThresholdBaseUnits = 3000L
                ),
                CatalogItem(
                    id = UuidV7.generate(),
                    name = "Aashirvaad Atta 5kg",
                    aliases = """["atta","maavu","wheat","aashirvaad"]""",
                    pricePaise = 24900L,
                    baseUnit = BaseUnitType.GRAM,
                    displayUnit = DisplayUnit.PACK,
                    inventoryType = InventoryType.STOCK,
                    stockBaseUnits = 8L,
                    reorderThresholdBaseUnits = 2L
                ),
                CatalogItem(
                    id = UuidV7.generate(),
                    name = "Maggi 70g",
                    aliases = """["maggi","noodles","nodules","magi"]""",
                    pricePaise = 1400L,
                    baseUnit = BaseUnitType.PIECE,
                    displayUnit = DisplayUnit.PACK,
                    inventoryType = InventoryType.STOCK,
                    stockBaseUnits = 40L,
                    reorderThresholdBaseUnits = 10L
                ),
                CatalogItem(
                    id = UuidV7.generate(),
                    name = "Colgate 100g",
                    aliases = """["colgate","toothpaste","paste","pallet"]""",
                    pricePaise = 6500L,
                    baseUnit = BaseUnitType.PIECE,
                    displayUnit = DisplayUnit.PACK,
                    inventoryType = InventoryType.STOCK,
                    stockBaseUnits = 18L,
                    reorderThresholdBaseUnits = 4L
                ),
                CatalogItem(
                    id = UuidV7.generate(),
                    name = "Vim Bar",
                    aliases = """["vim","dish soap","washing","vimbar"]""",
                    pricePaise = 1000L,
                    baseUnit = BaseUnitType.PIECE,
                    displayUnit = DisplayUnit.PCS,
                    inventoryType = InventoryType.STOCK,
                    stockBaseUnits = 30L,
                    reorderThresholdBaseUnits = 5L
                ),

                // ── FLOW items (Sold fresh daily) ──
                CatalogItem(
                    id = UuidV7.generate(),
                    name = "Amul Milk",
                    aliases = """["paal","milk","aavin","amul paal","liter paal","litre"]""",
                    pricePaise = 2800L,
                    baseUnit = BaseUnitType.MILLILITER,
                    displayUnit = DisplayUnit.L,
                    inventoryType = InventoryType.FLOW
                ),
                CatalogItem(
                    id = UuidV7.generate(),
                    name = "Curd 500ml",
                    aliases = """["thayir","curd","yogurt","dahi"]""",
                    pricePaise = 3000L,
                    baseUnit = BaseUnitType.PIECE,
                    displayUnit = DisplayUnit.PACK,
                    inventoryType = InventoryType.FLOW
                ),
                CatalogItem(
                    id = UuidV7.generate(),
                    name = "Eggs",
                    aliases = """["muttai","egg","eggs","kozhi muttai"]""",
                    pricePaise = 700L,
                    baseUnit = BaseUnitType.PIECE,
                    displayUnit = DisplayUnit.PCS,
                    inventoryType = InventoryType.FLOW
                ),
                CatalogItem(
                    id = UuidV7.generate(),
                    name = "Tomato 1kg",
                    aliases = """["thakkali","tomato","tomatoes"]""",
                    pricePaise = 4000L,
                    baseUnit = BaseUnitType.GRAM,
                    displayUnit = DisplayUnit.KG,
                    inventoryType = InventoryType.FLOW
                ),
                CatalogItem(
                    id = UuidV7.generate(),
                    name = "Onion 1kg",
                    aliases = """["vengayam","onion","onions","pyaz"]""",
                    pricePaise = 3500L,
                    baseUnit = BaseUnitType.GRAM,
                    displayUnit = DisplayUnit.KG,
                    inventoryType = InventoryType.FLOW
                ),
                CatalogItem(
                    id = UuidV7.generate(),
                    name = "Dal 1kg",
                    aliases = """["paruppu","dal","dhal","lentil"]""",
                    pricePaise = 9000L,
                    baseUnit = BaseUnitType.GRAM,
                    displayUnit = DisplayUnit.KG,
                    inventoryType = InventoryType.FLOW
                )
            )
            dao.insertAll(items)
        }
    }
}

class Converters {
    @TypeConverter
    fun fromInventoryType(type: InventoryType): String = type.name

    @TypeConverter
    fun toInventoryType(value: String): InventoryType = InventoryType.valueOf(value)

    @TypeConverter
    fun fromPaymentMode(mode: PaymentMode): String = mode.name

    @TypeConverter
    fun toPaymentMode(value: String): PaymentMode = PaymentMode.valueOf(value)

    @TypeConverter
    fun fromBillStatus(status: BillStatus): String = status.name

    @TypeConverter
    fun toBillStatus(value: String): BillStatus = BillStatus.valueOf(value)

    @TypeConverter
    fun fromJournalStatus(status: JournalStatus): String = status.name

    @TypeConverter
    fun toJournalStatus(value: String): JournalStatus = JournalStatus.valueOf(value)

    @TypeConverter
    fun fromMovementReason(reason: MovementReason): String = reason.name

    @TypeConverter
    fun toMovementReason(value: String): MovementReason = MovementReason.valueOf(value)

    @TypeConverter
    fun fromBaseUnitType(type: BaseUnitType): String = type.name

    @TypeConverter
    fun toBaseUnitType(value: String): BaseUnitType = BaseUnitType.valueOf(value)

    @TypeConverter
    fun fromDisplayUnit(unit: DisplayUnit): String = unit.name

    @TypeConverter
    fun toDisplayUnit(value: String): DisplayUnit = DisplayUnit.valueOf(value)
}
