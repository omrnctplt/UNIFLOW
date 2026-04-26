package com.uniflow.app.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class HashUtilsTest {
    @Test
    fun `sha256 returns correct hash`() {
        val input = "password123"
        // Expected hash for "password123"
        val expected = "ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f"
        assertEquals(expected, HashUtils.sha256(input))
    }
}
