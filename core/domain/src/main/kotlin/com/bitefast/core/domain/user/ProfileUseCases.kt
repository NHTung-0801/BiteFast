package com.bitefast.core.domain.user

import com.bitefast.core.domain.repository.AuthRepository
import com.bitefast.core.model.User
import javax.inject.Inject

// ─── Validation Result ────────────────────────────────────────────────────────

sealed interface ProfileValidationResult {
    data class Success(val user: User) : ProfileValidationResult
    data class Invalid(val field: String, val message: String) : ProfileValidationResult
}

// ─── GetUserProfileUseCase ────────────────────────────────────────────────────

/**
 * Lấy thông tin User hiện tại từ AuthRepository.
 * Trả về null nếu chưa đăng nhập.
 */
class GetUserProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): User? = authRepository.getCurrentUser()
}

// ─── UpdateProfileUseCase ─────────────────────────────────────────────────────

/**
 * Validate dữ liệu đầu vào và cập nhật hồ sơ cá nhân người dùng.
 *
 * Business rules:
 * - Họ và tên: Không được để trống, tối thiểu 2 ký tự.
 * - Số điện thoại: Chuẩn Việt Nam 10 số (03x, 05x, 07x, 08x, 09x) — có thể bỏ trống.
 * - Avatar: Không bắt buộc (nullable).
 */
class UpdateProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    companion object {
        // SĐT Việt Nam 10 số: 03x, 05x, 07x, 08x, 09x
        private val PHONE_REGEX = Regex("""^(0)(3|5|7|8|9)\d{8}$""")
    }

    suspend operator fun invoke(
        name: String,
        phone: String,
        avatar: String? = null
    ): ProfileValidationResult {
        // Validate Họ và tên
        val trimmedName = name.trim()
        if (trimmedName.length < 2) {
            return ProfileValidationResult.Invalid(
                field = "name",
                message = "Họ và tên phải có ít nhất 2 ký tự."
            )
        }

        // Validate Số điện thoại (nếu có nhập)
        val trimmedPhone = phone.replace(" ", "").trim()
        if (trimmedPhone.isNotEmpty() && !PHONE_REGEX.matches(trimmedPhone)) {
            return ProfileValidationResult.Invalid(
                field = "phone",
                message = "Số điện thoại không hợp lệ. Vui lòng nhập số Việt Nam 10 chữ số (VD: 0987654321)."
            )
        }

        // Gọi Repository để cập nhật và lưu bền vững
        val updatedUser = authRepository.updateProfile(
            name = trimmedName,
            phone = trimmedPhone,
            avatar = avatar
        )

        return ProfileValidationResult.Success(updatedUser)
    }
}
