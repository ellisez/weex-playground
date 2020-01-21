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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;

import org.apache.weex.common.WXModule;
import org.apache.weex.ui.IExternalModuleGetter;


public class WXBindingXModuleService extends Service implements IExternalModuleGetter {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public Class<? extends WXModule> getExternalModuleClass(String type, Context context) {
        if ("bindingx".equals(type)){
            return WXBindingXModule.class;
        }else if ("binding".equals(type)){
            return WXBindingXModule.class;
        }else if ("expressionBinding".equals(type)){
            return WXExpressionBindingModule.class;
        }
        return null;
    }
}
