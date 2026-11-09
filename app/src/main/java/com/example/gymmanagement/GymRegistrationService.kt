package com.example.gymmanagement

data class GymMember(
    val fullName: String,
    val age: Int,
    val phone: String,
    val durationMonths: Int,
    val category: String,
    val discountPercent: Int,
    val totalPrice: Double,

    val trainer: String = "Не назначен",
    val branch: String = "Не выбран"
)

sealed class RegistrationResult {
    data class Success(val member: GymMember, val formattedCard: String) : RegistrationResult()
    data class Error(val errorMessage: String) : RegistrationResult()
}
    class GymRegistrationService {

        companion object {
            const val PRICE_PER_MONTH = 2500.0
            const val MIN_AGE = 14
            const val MAX_AGE = 99
            const val MIN_MONTHS = 1
            const val MAX_MONTHS = 36
        }

        fun registerMember(
            nameInput: String?,
            ageInput: String?,
            phoneInput: String?,
            monthsInput: String?
        ): RegistrationResult {
            val name = nameInput?.trim().orEmpty()
            if (name.length < 2 || !name.all { it.isLetter() || it.isWhitespace() || it == '-' }) {
                return RegistrationResult.Error("Ошибка: Введите корректное ФИО (буквы и дефис, не менее 2 символов).")
            }

            val age = ageInput?.trim()?.toIntOrNull()
            if (age == null || age !in MIN_AGE..MAX_AGE) {
                return RegistrationResult.Error("Ошибка: Возраст должен быть числом от $MIN_AGE до $MAX_AGE лет.")
            }

            val phone = phoneInput?.trim().orEmpty()
            val digitsOnly = phone.filter { it.isDigit() }
            if (digitsOnly.length != 11) {
                return RegistrationResult.Error("Ошибка: Номер телефона должен содержать 11 цифр (например, +79991234567).")
            }

            val months = monthsInput?.trim()?.toIntOrNull()
            if (months == null || months !in MIN_MONTHS..MAX_MONTHS) {
                return RegistrationResult.Error("Ошибка: Срок абонемента должен быть от $MIN_MONTHS до $MAX_MONTHS мес.")
            }

            val category = when {
                age < 18 -> "Юниор"
                age >= 60 -> "Сеньор"
                else -> "Стандарт"
            }

            val discount = when {
                months >= 12 -> 20
                months >= 6 -> 10
                else -> 0
            }

            val basePrice = months * PRICE_PER_MONTH
            val finalPrice = basePrice * (1.0 - discount / 100.0)

            val member = GymMember(
                fullName = name,
                age = age,
                phone = phone,
                durationMonths = months,
                category = category,
                discountPercent = discount,
                totalPrice = finalPrice
            )

            val card = """
            [КАРТОЧКА КЛИЕНТА СПОРТЗАЛА]
            --------------------------------------------------
            ФИО: ${member.fullName}
            Возраст: ${member.age} лет ($category)
            Телефон: ${member.phone}
            Срок абонемента: ${member.durationMonths} мес.
            Скидка: $discount%
            ИТОГО К ОПЛАТЕ: ${"%.2f".format(member.totalPrice)} руб.
            Статус: Активен
        """.trimIndent()

            return RegistrationResult.Success(member, card)
        }
    }


