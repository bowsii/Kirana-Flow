package com.kiranaflow.ai.asr.di

import com.kiranaflow.ai.asr.AndroidSpeechEngine
import com.kiranaflow.ai.asr.SpeechEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SpeechEngineModule {

    @Binds
    @Singleton
    abstract fun bindSpeechEngine(impl: AndroidSpeechEngine): SpeechEngine
}
