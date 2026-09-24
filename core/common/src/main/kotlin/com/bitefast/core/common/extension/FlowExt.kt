package com.bitefast.core.common.extension

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.retryWhen
import java.io.IOException
import kotlin.math.pow
import kotlin.random.Random

fun <T> Flow<T>.retryWithExponentialBackoff(
    maxRetries: Int = 3,
    initialDelayMs: Long = 1000L,
    maxDelayMs: Long = 8000L,
    factor: Double = 2.0
): Flow<T> = retryWhen { cause, attempt ->
    if (cause is IOException && attempt < maxRetries) {
        val delayTime = (initialDelayMs * factor.pow(attempt.toDouble())).toLong().coerceAtMost(maxDelayMs)
        val jitter = Random.nextLong(0, 300)
        delay(delayTime + jitter)
        true
    } else {
        false
    }
}
