package com.bitefast.core.common.extension

import java.text.NumberFormat
import java.util.Locale

fun Double.formatVnd(): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return formatter.format(this)
}

fun Long.formatVnd(): String {
    return this.toDouble().formatVnd()
}

fun Double.formatCurrency(currencyCode: String = "VND"): String {
    return when (currencyCode.uppercase()) {
        "USD" -> "$%.2f".format(this)
        "VND" -> "%,.0f ₫".format(this)
        else -> "%.2f %s".format(this, currencyCode)
    }
}
