/*
 * Copyright (c) 2016-2018. Uniquid Inc. or its affiliates. All Rights Reserved.
 *
 * License is in the "LICENSE" file accompanying this file.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.uniquid.core;

import com.uniquid.core.impl.UniquidSimplifier;
import com.uniquid.messages.FunctionResponseMessage;
import com.uniquid.messages.UniquidMessage;

public interface MessageHandler {

    FunctionResponseMessage handleMessage(UniquidSimplifier simplifier, UniquidMessage message);
}
