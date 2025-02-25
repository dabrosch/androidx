/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.core.telecom

import android.os.Build.VERSION_CODES
import android.telecom.DisconnectCause
import androidx.annotation.RequiresApi
import androidx.core.telecom.internal.utils.Utils
import androidx.core.telecom.utils.BaseTelecomTest
import androidx.core.telecom.utils.TestUtils
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.filters.SdkSuppress
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * This test class verifies the [CallControlScope] functionality is working as intended when adding
 * a VoIP call.  Each test should add a call via [CallsManager.addCall] and changes the call state
 * via the [CallControlScope].
 *
 * Note: Be careful with using a delay in a runBlocking scope to avoid missing flows. ex:
 * runBlocking {
 *      addCall(...){
 *          delay(x time) // The flow will be emitted here and missed
 *          currentCallEndpoint.counter.getFirst() // The flow may never be collected
 *      }
 * }
 */
@SdkSuppress(minSdkVersion = VERSION_CODES.O)
@RequiresApi(VERSION_CODES.O)
@RunWith(AndroidJUnit4::class)
class BasicCallControlsTest : BaseTelecomTest() {
    private val NUM_OF_TIMES_TO_TOGGLE = 3

    @Before
    fun setUp() {
        Utils.resetUtils()
    }

    @After
    fun onDestroy() {
        Utils.resetUtils()
    }

    /***********************************************************************************************
     *                           V2 APIs (Android U and above) tests
     *********************************************************************************************/

    /**
     * assert [CallsManager.addCall] can successfully add an *OUTGOING* call and set it active. The
     * call should use the *V2 platform APIs* under the hood.
     */
    @SdkSuppress(minSdkVersion = VERSION_CODES.UPSIDE_DOWN_CAKE)
    @LargeTest
    @Test
    fun testBasicOutgoingCall() {
        setUpV2Test()
        runBlocking_addCallAndSetActive(TestUtils.OUTGOING_CALL_ATTRIBUTES)
    }

    /**
     * assert [CallsManager.addCall] can successfully add an *INCOMING* call and answer it. The
     * call should use the *V2 platform APIs* under the hood.
     */
    @SdkSuppress(minSdkVersion = VERSION_CODES.UPSIDE_DOWN_CAKE)
    @LargeTest
    @Test
    fun testBasicIncomingCall() {
        setUpV2Test()
        runBlocking_addCallAndSetActive(TestUtils.INCOMING_CALL_ATTRIBUTES)
    }

    /**
     * assert [CallsManager.addCall] can successfully add a call and **TOGGLE** active and inactive.
     * The call should use the *V2 platform APIs* under the hood.
     */
    @SdkSuppress(minSdkVersion = VERSION_CODES.UPSIDE_DOWN_CAKE)
    @LargeTest
    @Test
    fun testTogglingHoldOnActiveCall() {
        setUpV2Test()
        runBlocking_ToggleCallAsserts()
    }

    /**
     * assert [CallsManager.addCall] can successfully add a call and request a new
     * [CallEndpointCompat] via [CallControlScope.requestEndpointChange].
     * The call should use the *V2 platform APIs* under the hood.
     */
    @SdkSuppress(minSdkVersion = VERSION_CODES.UPSIDE_DOWN_CAKE)
    @LargeTest
    @Test
    fun testRequestEndpointChange() {
        setUpV2Test()
        runBlocking_RequestEndpointChangeAsserts()
    }

    /***********************************************************************************************
     *                           Backwards Compatibility Layer tests
     *********************************************************************************************/

    /**
     * assert [CallsManager.addCall] can successfully add an *OUTGOING* call and set it active. The
     * call should use the *[android.telecom.ConnectionService] and [android.telecom.Connection]
     * APIs* under the hood.
     */
    @SdkSuppress(minSdkVersion = VERSION_CODES.O)
    @LargeTest
    @Test
    fun testBasicOutgoingCall_BackwardsCompat() {
        setUpBackwardsCompatTest()
        runBlocking_addCallAndSetActive(TestUtils.OUTGOING_CALL_ATTRIBUTES)
    }

    /**
     * assert [CallsManager.addCall] can successfully add an *INCOMING* call and answer it.
     * The call should use the *[android.telecom.ConnectionService] and [android.telecom.Connection]
     * APIs* under the hood.
     */
    @SdkSuppress(minSdkVersion = VERSION_CODES.O)
    @LargeTest
    @Test
    fun testBasicIncomingCall_BackwardsCompat() {
        setUpBackwardsCompatTest()
        runBlocking_addCallAndSetActive(TestUtils.INCOMING_CALL_ATTRIBUTES)
    }

    /**
     * assert [CallsManager.addCall] can successfully add a call and **TOGGLE** active and inactive.
     * The call should use the *[android.telecom.ConnectionService] and [android.telecom.Connection]
     * APIs* under the hood.
     */
    @SdkSuppress(minSdkVersion = VERSION_CODES.O)
    @LargeTest
    @Test
    fun testTogglingHoldOnActiveCall_BackwardsCompat() {
        setUpBackwardsCompatTest()
        runBlocking_ToggleCallAsserts()
    }

    /**
     * assert [CallsManager.addCall] can successfully add a call and request a new
     * [CallEndpointCompat] via [CallControlScope.requestEndpointChange].
     * The call should use the *[android.telecom.ConnectionService] and [android.telecom.Connection]
     * APIs* under the hood.
     */
    @SdkSuppress(minSdkVersion = VERSION_CODES.O)
    @LargeTest
    @Test
    fun testRequestEndpointChange_BackwardsCompat() {
        setUpBackwardsCompatTest()
        runBlocking_RequestEndpointChangeAsserts()
        // TODO:: tracking bug: b/283324578. This test passes when the request is sent off and does
        // not actually verify the request was successful. Need to change the impl. details.
    }

    /***********************************************************************************************
     *                           Helpers
     *********************************************************************************************/

    /**
     * This helper facilitates adding a call, setting it active or answered, and disconnecting.
     *
     * Note: delays are inserted to simulate more natural calling. Otherwise the call dumpsys
     * does not reflect realistic transitions.
     *
     * Note: This helper blocks the TestRunner from finishing until all asserts and async functions
     * have finished or the timeout has been reached.
     */
    private fun runBlocking_addCallAndSetActive(callAttributesCompat: CallAttributesCompat) {
        runBlocking {
            val deferred = CompletableDeferred<Unit>()
            assertWithinTimeout_addCall(deferred, callAttributesCompat) {
                launch {
                    if (callAttributesCompat.isOutgoingCall()) {
                        assertTrue(setActive())
                    } else {
                        assertTrue(answer(CallAttributesCompat.CALL_TYPE_AUDIO_CALL))
                    }
                    assertTrue(disconnect(DisconnectCause(DisconnectCause.LOCAL)))
                    deferred.complete(Unit) // completed all asserts. cancel timeout!
                }
            }
        }
    }

    // similar to runBlocking_addCallAndSetActive except for toggling
    private fun runBlocking_ToggleCallAsserts() {
        runBlocking {
            val deferred = CompletableDeferred<Unit>()
            assertWithinTimeout_addCall(deferred, TestUtils.OUTGOING_CALL_ATTRIBUTES) {
                launch {
                    repeat(NUM_OF_TIMES_TO_TOGGLE) {
                        assertTrue(setActive())
                        assertTrue(setInactive())
                    }
                    assertTrue(disconnect(DisconnectCause(DisconnectCause.LOCAL)))
                    deferred.complete(Unit) // completed all asserts. cancel timeout!
                }
            }
        }
    }

    // similar to runBlocking_addCallAndSetActive except for requesting a new call endpoint
    private fun runBlocking_RequestEndpointChangeAsserts() {
        runBlocking {
            val deferred = CompletableDeferred<Unit>()
            assertWithinTimeout_addCall(deferred, TestUtils.OUTGOING_CALL_ATTRIBUTES) {
                launch {
                    // ============================================================================
                    //   NOTE:: DO NOT DELAY BEFORE COLLECTING FLOWS OR THEY COULD BE MISSED!!
                    // ============================================================================
                    val currentEndpoint = currentCallEndpoint.first()
                    assertNotNull("currentEndpoint is null", currentEndpoint)
                    val availableEndpointsList = availableEndpoints.first()
                    // only run the following asserts if theres another endpoint available
                    // (This will most likely the speaker endpoint)
                    if (availableEndpointsList.size > 1) {
                        // grab another endpoint
                        val anotherEndpoint =
                            getAnotherEndpoint(currentEndpoint, availableEndpointsList)
                        assertNotNull(anotherEndpoint)
                        // set the call active
                        assertTrue(setActive())
                        // request an endpoint switch
                        assertTrue(requestEndpointChange(anotherEndpoint!!))
                    }
                    assertTrue(disconnect(DisconnectCause(DisconnectCause.LOCAL)))
                    deferred.complete(Unit) // completed all asserts. cancel timeout!
                }
            }
        }
    }

    private fun getAnotherEndpoint(
        currentEndpoint: CallEndpointCompat,
        availableEndpoints: List<CallEndpointCompat>
    ): CallEndpointCompat? {
        for (endpoint in availableEndpoints) {
            if (endpoint.type != currentEndpoint.type) {
                return endpoint
            }
        }
        return null
    }
}