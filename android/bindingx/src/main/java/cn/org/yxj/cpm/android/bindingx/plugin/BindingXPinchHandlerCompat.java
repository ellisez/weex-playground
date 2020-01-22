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

import android.content.Context;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import cn.org.yxj.cpm.android.bindingx.core.LogProxy;
import cn.org.yxj.cpm.android.bindingx.core.PlatformManager;
import cn.org.yxj.cpm.android.bindingx.core.internal.BindingXPinchHandler;
import org.apache.weex.ui.component.WXComponent;
import org.apache.weex.ui.view.gesture.WXGesture;
import org.apache.weex.ui.view.gesture.WXGestureObservable;

/**
 * Description:
 *
 * a compatible version of pinch handler which use weex's new interface to inject touch listener.
 *
 * Created by rowandjj(chuyi)<br/>
 */
public class BindingXPinchHandlerCompat extends BindingXPinchHandler {

    private WXGesture mWeexGestureHandler = null;

    public BindingXPinchHandlerCompat(Context context, PlatformManager manager, Object... extension) {
        super(context, manager, extension);
    }

    @Override
    public boolean onCreate(@NonNull String sourceRef, @NonNull String eventType) {
        String instanceId = TextUtils.isEmpty(mAnchorInstanceId) ? mInstanceId : mAnchorInstanceId;
        WXComponent sourceComponent = WXModuleUtils.findComponentByRef(instanceId, sourceRef);
        if(sourceComponent == null) {
            return super.onCreate(sourceRef, eventType);
        }
        View view = sourceComponent.getHostView();
        if(!(view instanceof ViewGroup) || !(view instanceof WXGestureObservable)) {
            return super.onCreate(sourceRef, eventType);
        }

        try {
            WXGestureObservable gestureTarget = (WXGestureObservable) view;
            mWeexGestureHandler = gestureTarget.getGestureListener();
            if(mWeexGestureHandler != null) {
                mWeexGestureHandler.addOnTouchListener(this);
                LogProxy.d("[BindingXPinchHandlerCompat] onCreate success. {source:" + sourceRef + ",type:" + eventType + "}");
                return true;
            } else {
                return super.onCreate(sourceRef, eventType);
            }
        }catch (Throwable e) {
            // fallback
            LogProxy.e("experimental gesture features open failed." + e.getMessage());
            return super.onCreate(sourceRef, eventType);
        }
    }

    @Override
    public boolean onDisable(@NonNull String sourceRef, @NonNull String eventType) {
        boolean result = super.onDisable(sourceRef, eventType);
        if(mWeexGestureHandler != null) {
            try {
                result |= mWeexGestureHandler.removeTouchListener(this);
            }catch (Throwable e) {
                LogProxy.e("[BindingXPanHandlerCompat]  disabled failed." + e.getMessage());
            }
        }
        return result;
    }
}
