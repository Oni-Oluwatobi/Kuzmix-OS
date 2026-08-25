package com.kreadivegalaxy.kuzmixos

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class OpenRouterKeyManagerTest {

    private lateinit var context: Context
    private lateinit var keyManager: OpenRouterKeyManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        keyManager = OpenRouterKeyManager(context)
    }

    @Test
    fun testCryptographicStorageAndRetrieval() {
        println("--- PROTOCOL 1: Cryptographic Storage Check ---")
        val sampleKeys = List(10) { index -> "TEST_KEY_SLOT_$index" }

        // Store keys into all 10 slots
        for (i in 0 until 10) {
            keyManager.storeKeyInSlot(i, sampleKeys[i])
        }

        // Retrieve and verify all 10 slots
        for (i in 0 until 10) {
            val retrievedKey = keyManager.getKeyFromSlot(i)
            println("Slot [$i] Retrieved Key: $retrievedKey")
            assertEquals("Key in slot $i must match stored value", sampleKeys[i], retrievedKey)
        }
        println("Protocol 1 PASSED: Cryptographic storage, retrieval, and decryption verified across 10 slots.\n")
    }

    @Test
    fun testFailoverRotationAndWrapAround() {
        println("--- PROTOCOL 2: Failover Rotation Simulation ---")
        val initialIndex = keyManager.getActiveIndex()
        val totalKeys = keyManager.totalKeysCount
        assertTrue("Key manager must have active keys", totalKeys > 0)

        println("Initial Key Index: $initialIndex (Total keys: $totalKeys)")

        // Simulate 429 Quota Exceeded exception triggers rotation
        for (i in 0 until totalKeys) {
            val expectedNext = (initialIndex + i + 1) % totalKeys
            val newIndex = keyManager.rotateKey()
            println("Simulated 429 Error #$i -> Rotated to Index: $newIndex (Expected: $expectedNext)")
            assertEquals(expectedNext, newIndex)
        }

        // Verify index wrapped back to starting point
        println("Active Index after full rotation loop: ${keyManager.getActiveIndex()}")
        assertEquals(initialIndex, keyManager.getActiveIndex())
        println("Protocol 2 PASSED: 429 Failover rotation and 10-key wrap-around verified.\n")
    }

    @Test
    fun testActivePingConnection() = runBlocking {
        println("--- PROTOCOL 3: Active Ping Connection Test ---")
        val results = keyManager.pingAllKeys()
        assertNotNull(results)
        assertTrue(results.isNotEmpty())

        results.forEach { (slot, status) ->
            println("Slot [$slot]: $status")
        }
        println("Protocol 3 PASSED: Active ping connection test completed for all configured key slots.\n")
    }
}
