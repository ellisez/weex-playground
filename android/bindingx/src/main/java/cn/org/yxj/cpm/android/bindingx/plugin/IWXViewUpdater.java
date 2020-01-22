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
package cn.org.yxj.cpm.android.bindingx.plugin;

import androidx.annotation.NonNull;
import android.view.View;

import cn.org.yxj.cpm.android.bindingx.core.PlatformManager;
import org.apache.weex.ui.component.WXComponent;

import java.util.Map;

/**
 * Description:
 *
 * Interface for update native view in weex
 *
 * Created by rowandjj(chuyi)<br/>
 */

public interface IWXViewUpdater {

    void update(@NonNull WXComponent component,
                @NonNull View targetView,
                @NonNull Object cmd,
                @NonNull PlatformManager.IDeviceResolutionTranslator translator,
                @NonNull Map<String, Object> config);
}
