package com.kiranaflow.app.data.db

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kiranaflow.app.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [CatalogItem::class, Bill::class, BillItem::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class KiranaFlowDatabase : RoomDatabase() {

    abstract fun catalogDao(): CatalogDao
    abstract fun billDao(): BillDao

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
                    // Room uses SQLite WAL mode by default for crash-safe commits
                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed the database with sample kirana catalog
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

        /**
         * Seeds the catalog with a realistic kirana shop inventory.
         * STOCK: items that can be counted on the shelf.
         * FLOW : perishables sold fresh daily.
         */
        private suspend fun seedCatalog(dao: CatalogDao) {
            val items = listOf(
                // ── STOCK items ──
                CatalogItem(name = "Parle-G",       aliases = """["parleg","parle g","paarle ji","biscuit"]""",   price = 5.0,   unit = "pcs",  inventoryType = InventoryType.STOCK, stockQty = 50.0, reorderThreshold = 10.0),
                CatalogItem(name = "Good Day",      aliases = """["good day biscuit","goodday"]""",               price = 20.0,  unit = "pcs",  inventoryType = InventoryType.STOCK, stockQty = 30.0, reorderThreshold = 5.0),
                CatalogItem(name = "Tata Salt 1kg", aliases = """["tata uppu","uppu","salt","tata salt"]""",       price = 22.0,  unit = "pcs",  inventoryType = InventoryType.STOCK, stockQty = 20.0, reorderThreshold = 3.0),
                CatalogItem(name = "Amul Butter",   aliases = """["amul","butter","vennai"]""",                   price = 55.0,  unit = "pcs",  inventoryType = InventoryType.STOCK, stockQty = 15.0, reorderThreshold = 3.0),
                CatalogItem(name = "Dettol Soap",   aliases = """["dettol","soap","saappu","dettol soap"]""",     price = 45.0,  unit = "pcs",  inventoryType = InventoryType.STOCK, stockQty = 25.0, reorderThreshold = 5.0),
                CatalogItem(name = "Fortune Oil 1L",aliases = """["fortune","oil","ennai","cooking oil"]""",      price = 135.0, unit = "pcs",  inventoryType = InventoryType.STOCK, stockQty = 12.0, reorderThreshold = 3.0),
                CatalogItem(name = "Aashirvaad Atta 5kg", aliases = """["atta","maavu","wheat","aashirvaad"]""", price = 249.0, unit = "pcs",  inventoryType = InventoryType.STOCK, stockQty = 8.0,  reorderThreshold = 2.0),
                CatalogItem(name = "Maggi 70g",     aliases = """["maggi","noodles","nodules","magi"]""",         price = 14.0,  unit = "pcs",  inventoryType = InventoryType.STOCK, stockQty = 40.0, reorderThreshold = 10.0),
                CatalogItem(name = "Colgate 100g",  aliases = """["colgate","toothpaste","paste","pallet"]""",   price = 65.0,  unit = "pcs",  inventoryType = InventoryType.STOCK, stockQty = 18.0, reorderThreshold = 4.0),
                CatalogItem(name = "Vim Bar",       aliases = """["vim","dish soap","washing","vimbar"]""",      price = 10.0,  unit = "pcs",  inventoryType = InventoryType.STOCK, stockQty = 30.0, reorderThreshold = 5.0),

                // ── FLOW items ──
                CatalogItem(name = "Amul Milk",     aliases = """["paal","milk","aavin","amul paal","liter paal","litre"]""", price = 28.0, unit = "L", inventoryType = InventoryType.FLOW),
                CatalogItem(name = "Curd 500ml",    aliases = """["thayir","curd","yogurt","dahi"]""",           price = 30.0,  unit = "pcs",  inventoryType = InventoryType.FLOW),
                CatalogItem(name = "Eggs",          aliases = """["muttai","egg","eggs","kozhi muttai"]""",      price = 7.0,   unit = "pcs",  inventoryType = InventoryType.FLOW),
                CatalogItem(name = "Tomato 1kg",    aliases = """["thakkali","tomato","tomatoes"]""",            price = 40.0,  unit = "kg",   inventoryType = InventoryType.FLOW),
                CatalogItem(name = "Onion 1kg",     aliases = """["vengayam","onion","onions","pyaz"]""",        price = 35.0,  unit = "kg",   inventoryType = InventoryType.FLOW),
                CatalogItem(name = "Dal 1kg",       aliases = """["paruppu","dal","dhal","lentil"]""",           price = 90.0,  unit = "kg",   inventoryType = InventoryType.FLOW),
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
}
