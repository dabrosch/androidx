/*
 * Copyright 2022 The Android Open Source Project
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

package androidx.privacysandbox.ads.adservices.customaudience

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Represents data used during the ad selection process to fetch buyer bidding signals from a
 * trusted key/value server. The fetched data is used during the ad selection process and consumed
 * by buyer JavaScript logic running in an isolated execution environment.
 *
 * @param trustedBiddingUri the URI pointing to the trusted key-value server holding bidding
 * signals. The URI must use HTTPS.
 * @param trustedBiddingKeys the list of keys to query from the trusted key-value server holding
 * bidding signals.
 */
class TrustedBiddingData public constructor(
    val trustedBiddingUri: Uri = Uri.EMPTY,
    val trustedBiddingKeys: List<String> = emptyList()
    ) {
    /**
     * @return `true` if two [TrustedBiddingData] objects contain the same information
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TrustedBiddingData) return false
        return this.trustedBiddingUri == other.trustedBiddingUri &&
            this.trustedBiddingKeys == other.trustedBiddingKeys
    }

    /**
     * @return the hash of the [TrustedBiddingData] object's data
     */
    override fun hashCode(): Int {
        return (31 * trustedBiddingUri.hashCode()) + trustedBiddingKeys.hashCode()
    }

    override fun toString(): String {
        return "TrustedBiddingData: trustedBiddingUri=$trustedBiddingUri " +
            "trustedBiddingKeys=$trustedBiddingKeys"
    }

    /** Builder for [TrustedBiddingData] objects. */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    public class Builder {
        private var trustedBiddingUri: Uri = Uri.EMPTY
        private var trustedBiddingKeys: List<String> = emptyList()

        /**
         * Sets the trusted Bidding Uri
         *
         * @param trustedBiddingUri the URI pointing to the trusted key-value server holding bidding
         * signals. The URI must use HTTPS.
         */
        fun setTrustedBiddingUri(trustedBiddingUri: Uri): Builder = apply {
            this.trustedBiddingUri = trustedBiddingUri
        }

        /**
         * Sets the trusted Bidding keys.
         *
         * @param trustedBiddingKeys list of keys to query the trusted key-value server with.
         * This list is permitted to be empty.
         */
        fun setTrustedBiddingKeys(trustedBiddingKeys: List<String>): Builder = apply {
            this.trustedBiddingKeys = trustedBiddingKeys
        }

        /**
         * Builds and instance of [TrustedBiddingData]
         */
        fun build(): TrustedBiddingData {
            return TrustedBiddingData(trustedBiddingUri, trustedBiddingKeys)
        }
    }
}