package com.kiranaflow.app.data.db

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kiranaflow.app.data.model.*
import com.kiranaflow.app.util.UuidV7
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        @Volatile
        private var INSTANCE: KiranaFlowDatabase? = null

        fun getInstance(context: Context): KiranaFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    KiranaFlowDatabase::class.java,
                    "kiranaflow.db"
                )
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
