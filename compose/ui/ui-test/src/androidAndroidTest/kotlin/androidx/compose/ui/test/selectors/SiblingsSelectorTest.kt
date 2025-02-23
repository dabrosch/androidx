/*
 * Copyright 2020 The Android Open Source Project
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

package androidx.compose.ui.test.selectors

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onSiblings
import androidx.compose.ui.test.util.BoundaryNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@MediumTest
@RunWith(AndroidJUnit4::class)
class SiblingsSelectorTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun siblings_noSibling() {
        rule.setContent {
            BoundaryNode(testTag = "Parent") {
                BoundaryNode(testTag = "Child")
            }
        }

        rule.onNodeWithTag("Child")
            .onSiblings()
            .assertCountEquals(0)
    }

    @Test
    fun siblings_oneSibling() {
        rule.setContent {
            BoundaryNode(testTag = "Parent") {
                BoundaryNode(testTag = "Child1")
                BoundaryNode(testTag = "Child2")
            }
        }

        rule.onNodeWithTag("Child1")
            .onSiblings()
            .assertCountEquals(1)
    }

    @Test
    fun siblings_twoSiblings() {
        rule.setContent {
            BoundaryNode(testTag = "Parent") {
                BoundaryNode(testTag = "Child1")
                BoundaryNode(testTag = "Child2")
                BoundaryNode(testTag = "Child3")
            }
        }

        rule.onNodeWithTag("Child2")
            .onSiblings()
            .assertCountEquals(2)
            .apply {
                get(0).assert(hasTestTag("Child1"))
                get(1).assert(hasTestTag("Child3"))
            }
    }
}