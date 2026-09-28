package com.kiranaflow.ai.runtime

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidOnDeviceRuntime @Inject constructor() : InferenceRuntime {
    override val isAvailable: Boolean = true
    override val providerName: String = "Android-OnDevice-CPU"

    override suspend fun initialize(): Boolean = true

    override suspend fun close() {}
}
