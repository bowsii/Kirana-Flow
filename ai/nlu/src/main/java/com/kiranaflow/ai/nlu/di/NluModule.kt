package com.kiranaflow.ai.nlu.di

import com.kiranaflow.ai.nlu.IntentParser
import com.kiranaflow.ai.nlu.RuleBasedIntentParser
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NluModule {

    @Binds
    @Singleton
    abstract fun bindIntentParser(impl: RuleBasedIntentParser): IntentParser
}
