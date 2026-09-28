package com.kiranaflow.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point. Triggers Hilt component generation.
 * Also a good place to initialise crash reporters / analytics in production.
 */
@HiltAndroidApp
class KiranaFlowApp : Application()
