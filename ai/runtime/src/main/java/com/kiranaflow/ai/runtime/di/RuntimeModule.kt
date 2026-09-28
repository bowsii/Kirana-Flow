package com.kiranaflow.ai.runtime.di

import com.kiranaflow.ai.runtime.AndroidOnDeviceRuntime
import com.kiranaflow.ai.runtime.InferenceRuntime
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RuntimeModule {

    @Binds
    @Singleton
    abstract fun bindInferenceRuntime(impl: AndroidOnDeviceRuntime): InferenceRuntime
}
