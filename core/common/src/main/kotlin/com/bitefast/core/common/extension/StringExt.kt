package com.bitefast.core.common.extension

import java.text.Normalizer

/**
 * Loại bỏ dấu tiếng Việt và chuyển sang chữ thường không dấu để so khớp tìm kiếm hoặc lọc linh hoạt.
 * Ví dụ: "Cơm Tấm" -> "com tam", "Phở & Bún" -> "pho & bun", "Đồ uống" -> "do uong".
 */
fun String.unaccent(): String {
    val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
    return "\\p{InCombiningDiacriticalMarks}+".toRegex()
        .replace(normalized, "")
        .replace('đ', 'd')
        .replace('Đ', 'd')
        .lowercase()
        .trim()
}
