package com.refugio.journal.core.security

import android.content.Context
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class AndroidSecurityManagerTest {

    private lateinit var context: Context
    private lateinit var securityManager: AndroidSecurityManager

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        securityManager = AndroidSecurityManager(context)
    }

    @Test
    fun `isPinConfigured returns false initially`() = runTest {
        assertFalse(securityManager.isPinConfigured())
    }

    @Test
    fun `setupPin hashes and stores pin so isPinConfigured returns true and validatePin succeeds`() = runTest {
        val pin = "1234"

        val setupResult = securityManager.setupPin(pin)

        assertTrue(setupResult.isSuccess)
        assertTrue(securityManager.isPinConfigured())
        assertTrue(securityManager.validatePin("1234"))
        assertFalse(securityManager.validatePin("9999"))
    }
}
