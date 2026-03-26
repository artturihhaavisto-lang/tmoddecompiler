package com.openjugg.ios

import com.openjugg.di.iosModule
import com.openjugg.di.sharedModule
import org.koin.core.context.startKoin

/**
 * Called from Swift's AppDelegate or @main struct to initialize Koin.
 * Swift cannot call Koin directly, so this helper bridges the gap.
 */
fun initKoin() {
    startKoin {
        modules(
            sharedModule,
            iosModule
        )
    }
}
