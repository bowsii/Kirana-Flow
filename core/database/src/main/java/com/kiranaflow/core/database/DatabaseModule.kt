package com.kiranaflow.core.database

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext ctx: Context,
        securityManager: DatabaseSecurityManager
    ): KiranaFlowDatabase =
        KiranaFlowDatabase.getInstance(ctx, securityManager)

    @Singleton
    @Provides
    fun provideCatalogDao(db: KiranaFlowDatabase): CatalogDao = db.catalogDao()

    @Singleton
    @Provides
    fun provideBillDao(db: KiranaFlowDatabase): BillDao = db.billDao()

    @Singleton
    @Provides
    fun provideJournalDao(db: KiranaFlowDatabase): JournalDao = db.journalDao()

    @Singleton
    @Provides
    fun provideStockMovementDao(db: KiranaFlowDatabase): StockMovementDao = db.stockMovementDao()

    @Singleton
    @Provides
    fun provideFlowDailyDao(db: KiranaFlowDatabase): FlowDailyDao = db.flowDailyDao()

    @Singleton
    @Provides
    fun provideDraftCartDao(db: KiranaFlowDatabase): DraftCartDao = db.draftCartDao()

    @Singleton
    @Provides
    fun provideBillCounterDao(db: KiranaFlowDatabase): BillCounterDao = db.billCounterDao()
}
