package com.bitefast.core.network.websocket

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bo gia lap WebSocket stream GPS va trang thai don hang cho moi truong MOCK / Demo.
 */
@Singleton
class MockOrderTrackingSocket @Inject constructor() {

    fun streamTracking(orderId: String): Flow<OrderLiveTrackingEvent> = flow {
        val resLat = 10.8490
        val resLng = 106.7725
        val custLat = 10.8540
        val custLng = 106.7810

        var currentProgress = 0.35f
        var status = "ON_THE_WAY"

        while (true) {
            val dLat = resLat + (custLat - resLat) * currentProgress
            val dLng = resLng + (custLng - resLng) * currentProgress
            val eta = ((1.0f - currentProgress) * 20).toInt().coerceAtLeast(1)

            emit(
                OrderLiveTrackingEvent(
                    orderId = orderId,
                    status = status,
                    driverId = "drv_01",
                    driverName = "Nguyễn Văn Hùng",
                    driverPhone = "0901234567",
                    driverLat = dLat,
                    driverLng = dLng,
                    etaMinutes = eta,
                    progressPercent = currentProgress,
                    timestamp = System.currentTimeMillis()
                )
            )

            if (currentProgress >= 1.0f) {
                status = "DELIVERED"
                emit(
                    OrderLiveTrackingEvent(
                        orderId = orderId,
                        status = status,
                        driverId = "drv_01",
                        driverName = "Nguyễn Văn Hùng",
                        driverPhone = "0901234567",
                        driverLat = custLat,
                        driverLng = custLng,
                        etaMinutes = 0,
                        progressPercent = 1.0f,
                        timestamp = System.currentTimeMillis()
                    )
                )
                break
            }

            delay(2000L)
            currentProgress = (currentProgress + 0.05f).coerceAtMost(1.0f)
        }
    }
}
