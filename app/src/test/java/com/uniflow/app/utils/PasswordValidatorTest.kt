package com.uniflow.app.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordValidatorTest {

    @Test
    fun `isValid returns true for valid password`() {
        assertTrue(PasswordValidator.isValid("StrongPass1!"))
        assertTrue(PasswordValidator.isValid("Abc1234#"))
    }

    @Test
    fun `isValid returns false for short password`() {
        assertFalse(PasswordValidator.isValid("Abc1!"))
    }

    @Test
    fun `isValid returns false for password without uppercase`() {
        assertFalse(PasswordValidator.isValid("strongpass1!"))
    }

    @Test
    fun `isValid returns false for password without lowercase`() {
        assertFalse(PasswordValidator.isValid("STRONGPASS1!"))
    }

    @Test
    fun `isValid returns false for password without digit`() {
        assertFalse(PasswordValidator.isValid("StrongPass!"))
    }

    @Test
    fun `isValid returns false for password without special character`() {
        assertFalse(PasswordValidator.isValid("StrongPass1"))
    }

    @Test
    fun `isValid returns false for password with spaces`() {
        assertFalse(PasswordValidator.isValid("Strong Pass1!"))
    }
}
