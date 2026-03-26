package com.openjugg.android

import android.app.Application
import com.openjugg.di.androidModule
import com.openjugg.di.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class OpenJuggApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@OpenJuggApp)
            modules(
                sharedModule,
                androidModule
            )
        }
    }
}
