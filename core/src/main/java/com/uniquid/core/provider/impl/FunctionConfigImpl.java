/*
 * Copyright (c) 2016-2018. Uniquid Inc. or its affiliates. All Rights Reserved.
 *
 * License is in the "LICENSE" file accompanying this file.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.uniquid.core.provider.impl;

        import com.uniquid.core.provider.FunctionConfig;
        import com.uniquid.core.provider.FunctionContext;

/**
 * Implementation of {@link FunctionConfig}
 */
public class FunctionConfigImpl implements FunctionConfig {

    private FunctionContext functionContext;

    /**
     * Creates an instance from the {@link FunctionContext}
     * @param functionContext the {@link FunctionContext} to use.
     */
    public FunctionConfigImpl(FunctionContext functionContext) {
        this.functionContext = functionContext;
    }

    @Override
    public FunctionContext getFunctionContext() {
        return functionContext;
    }

}
