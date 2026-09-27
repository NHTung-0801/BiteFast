package com.bitefast.core.model

import app.cash.turbine.test
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.IOException

class ResultTest {

    @Test
    fun `exhaustive when expression works without else branch`() {
        val results: List<Result<String>> = listOf(
            Result.Loading,
            Result.Success("BiteFast"),
            Result.Error(IllegalStateException("Failed"))
        )

        val mapped = results.map { result ->
            when (result) {
                is Result.Loading -> "LOADING"
                is Result.Success -> "SUCCESS: ${result.data}"
                is Result.Error -> "ERROR: ${result.exception.message}"
            }
        }

        assertEquals(listOf("LOADING", "SUCCESS: BiteFast", "ERROR: Failed"), mapped)
    }

    @Test
    fun `asResult emits Loading then Success for normal flow`() = runTest {
        val flow = flowOf("item1", "item2")

        flow.asResult().test {
            assertEquals(Result.Loading, awaitItem())
            assertEquals(Result.Success("item1"), awaitItem())
            assertEquals(Result.Success("item2"), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `asResult emits Loading then Error when upstream flow throws`() = runTest {
        val flow = flow<String> {
            throw IOException("Network timeout")
        }

        flow.asResult().test {
            assertEquals(Result.Loading, awaitItem())
            val errorItem = awaitItem()
            assertTrue(errorItem is Result.Error)
            assertEquals("Network timeout", (errorItem as Result.Error).exception.message)
            awaitComplete()
        }
    }

    @Test
    fun `onSuccess, onError, and onLoading extension callbacks execute appropriately`() {
        var successData: String? = null
        var errorThrown: Throwable? = null
        var loadingTriggered = false

        val success: Result<String> = Result.Success("Delicious Pho")
        success
            .onSuccess { successData = it }
            .onError { errorThrown = it }
            .onLoading { loadingTriggered = true }

        assertEquals("Delicious Pho", successData)
        assertNull(errorThrown)
        assertTrue(!loadingTriggered)

        val error: Result<String> = Result.Error(IllegalArgumentException("Invalid code"))
        error
            .onSuccess { successData = "Overwritten" }
            .onError { errorThrown = it }
            .onLoading { loadingTriggered = true }

        assertTrue(errorThrown is IllegalArgumentException)
        assertEquals("Delicious Pho", successData)

        val loading: Result<String> = Result.Loading
        loading
            .onLoading { loadingTriggered = true }

        assertTrue(loadingTriggered)
    }

    @Test
    fun `getOrNull and getOrDefault return expected values`() {
        val success: Result<Int> = Result.Success(42)
        val error: Result<Int> = Result.Error(RuntimeException())
        val loading: Result<Int> = Result.Loading

        assertEquals(42, success.getOrNull())
        assertNull(error.getOrNull())
        assertNull(loading.getOrNull())

        assertEquals(42, success.getOrDefault(0))
        assertEquals(99, error.getOrDefault(99))
        assertEquals(-1, loading.getOrDefault(-1))
    }
}