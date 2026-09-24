package com.bitefast.core.common.extension

private val EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
private val PHONE_REGEX = "^(\\+84|0)[3|5|7|8|9][0-9]{8}$".toRegex()

fun String.isValidEmail(): Boolean {
    return isNotBlank() && EMAIL_REGEX.matches(this)
}

fun String.isValidPhoneNumber(): Boolean {
    return isNotBlank() && PHONE_REGEX.matches(this)
}

fun String.isValidPassword(): Boolean {
    return length >= 8 && any { it.isDigit() } && any { it.isLetter() }
}
