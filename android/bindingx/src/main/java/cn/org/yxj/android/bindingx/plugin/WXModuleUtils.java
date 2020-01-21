/**
 * Copyright 2018 Alibaba Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.org.yxj.android.bindingx.plugin;

import androidx.annotation.Nullable;
import android.view.View;

import org.apache.weex.WXSDKManager;
import org.apache.weex.ui.component.WXComponent;

public class WXModuleUtils {
    private WXModuleUtils() {}

    @Nullable
    public static View findViewByRef(@Nullable String instanceId, @Nullable String ref) {
        WXComponent component = findComponentByRef(instanceId, ref);
        if (component == null) {
            return null;
        }
        return component.getHostView();
    }

    @Nullable
    public static WXComponent findComponentByRef(@Nullable String instanceId, @Nullable String ref) {
        return WXSDKManager.getInstance().getWXRenderManager().getWXComponent(instanceId, ref);
    }
}
