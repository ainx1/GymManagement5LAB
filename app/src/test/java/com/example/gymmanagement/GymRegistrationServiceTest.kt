package com.example.gymmanagement

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GymRegistrationServiceTest {

    private lateinit var service: GymRegistrationService

    @Before
    fun setUp() {
        service = GymRegistrationService()
    }

    // 1. Успешная регистрация: стандартный клиент, без скидки
    @Test
    fun testRegisterMember_ValidStandard_Success() {
        val result = service.registerMember("Иван Иванов", "25", "+79991234567", "3")
        assertTrue(result is RegistrationResult.Success)
        val member = (result as RegistrationResult.Success).member
        assertEquals("Стандарт", member.category)
        assertEquals(0, member.discountPercent)
        assertEquals(7500.0, member.totalPrice, 0.001)
    }

    // 2. Граничное значение возраста снизу (14 лет) -> категория Юниор
    @Test
    fun testRegisterMember_JuniorCategory_Success() {
        val result = service.registerMember("Алексей", "14", "+79991234567", "1")
        assertTrue(result is RegistrationResult.Success)
        assertEquals("Юниор", (result as RegistrationResult.Success).member.category)
    }

    // 3. Граничное значение возраста (60+ лет) -> категория Сеньор
    @Test
    fun testRegisterMember_SeniorCategory_Success() {
        val result = service.registerMember("Ольга", "60", "+79991234567", "1")
        assertTrue(result is RegistrationResult.Success)
        assertEquals("Сеньор", (result as RegistrationResult.Success).member.category)
    }

    // 4. Проверка скидки 20% при покупке на 12 месяцев
    @Test
    fun testRegisterMember_AnnualDiscount20Percent_Success() {
        val result = service.registerMember("Сергей", "30", "+79991234567", "12")
        assertTrue(result is RegistrationResult.Success)
        val member = (result as RegistrationResult.Success).member
        assertEquals(20, member.discountPercent)
        // 12 * 2500 = 30000; со скидкой 20% = 24000
        assertEquals(24000.0, member.totalPrice, 0.001)
    }

    // 5. Проверка скидки 10% при покупке на 6 месяцев
    @Test
    fun testRegisterMember_HalfYearDiscount10Percent_Success() {
        val result = service.registerMember("Мария", "28", "+79991234567", "6")
        assertTrue(result is RegistrationResult.Success)
        val member = (result as RegistrationResult.Success).member
        assertEquals(10, member.discountPercent)
        // 6 * 2500 = 15000; со скидкой 10% = 13500
        assertEquals(13500.0, member.totalPrice, 0.001)
    }

    // 6. Невалидное ФИО (меньше 2 букв или цифры)
    @Test
    fun testRegisterMember_InvalidName_ReturnsError() {
        val result = service.registerMember("Иван123", "20", "+79991234567", "1")
        assertTrue(result is RegistrationResult.Error)
        assertTrue((result as RegistrationResult.Error).errorMessage.contains("ФИО"))
    }

    // 7. Невалидный возраст (слишком мал: < 14)
    @Test
    fun testRegisterMember_AgeTooYoung_ReturnsError() {
        val result = service.registerMember("Петр", "13", "+79991234567", "1")
        assertTrue(result is RegistrationResult.Error)
        assertTrue((result as RegistrationResult.Error).errorMessage.contains("Возраст"))
    }

    // 8. Невалидный возраст (слишком велик: > 99)
    @Test
    fun testRegisterMember_AgeTooOld_ReturnsError() {
        val result = service.registerMember("Петр", "105", "+79991234567", "1")
        assertTrue(result is RegistrationResult.Error)
        assertTrue((result as RegistrationResult.Error).errorMessage.contains("Возраст"))
    }

    // 9. Невалидный телефон (не 11 цифр)
    @Test
    fun testRegisterMember_InvalidPhone_ReturnsError() {
        val result = service.registerMember("Петр", "25", "8999", "1")
        assertTrue(result is RegistrationResult.Error)
        assertTrue((result as RegistrationResult.Error).errorMessage.contains("телефон"))
    }

    // 10. Невалидный срок абонемента (0 или больше 36)
    @Test
    fun testRegisterMember_InvalidMonths_ReturnsError() {
        val result = service.registerMember("Петр", "25", "+79991234567", "0")
        assertTrue(result is RegistrationResult.Error)
        assertTrue((result as RegistrationResult.Error).errorMessage.contains("Срок абонемента"))
    }
}