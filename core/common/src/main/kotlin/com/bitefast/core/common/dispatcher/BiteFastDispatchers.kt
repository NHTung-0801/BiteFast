package com.bitefast.core.common.dispatcher

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val dispatcher: BiteFastDispatchers)

enum class BiteFastDispatchers {
    Default,
    IO,
    Main
}
