package com.kiranaflow.app

import android.content.Context
import android.os.Vibrator
import android.os.VibratorManager
import com.kiranaflow.app.data.db.BillDao
import com.kiranaflow.app.data.db.CatalogDao
import com.kiranaflow.app.data.db.KiranaFlowDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext ctx: Context): KiranaFlowDatabase =
        KiranaFlowDatabase.getInstance(ctx)

    @Singleton
    @Provides
    fun provideCatalogDao(db: KiranaFlowDatabase): CatalogDao = db.catalogDao()

    @Singleton
    @Provides
    fun provideBillDao(db: KiranaFlowDatabase): BillDao = db.billDao()

    @Singleton
    @Provides
    fun provideJournalDao(db: KiranaFlowDatabase): com.kiranaflow.app.data.db.JournalDao = db.journalDao()

    @Singleton
    @Provides
    fun provideStockMovementDao(db: KiranaFlowDatabase): com.kiranaflow.app.data.db.StockMovementDao = db.stockMovementDao()

    @Singleton
    @Provides
    fun provideFlowDailyDao(db: KiranaFlowDatabase): com.kiranaflow.app.data.db.FlowDailyDao = db.flowDailyDao()

    @Singleton
    @Provides
    fun provideDraftCartDao(db: KiranaFlowDatabase): com.kiranaflow.app.data.db.DraftCartDao = db.draftCartDao()

    @Singleton
    @Provides
    fun provideVibrator(@ApplicationContext ctx: Context): Vibrator {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val manager = ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
}
