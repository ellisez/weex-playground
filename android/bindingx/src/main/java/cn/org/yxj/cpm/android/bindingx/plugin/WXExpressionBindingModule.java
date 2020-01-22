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
import androidx.annotation.Nullable;

import cn.org.yxj.cpm.android.bindingx.core.BindingXCore;
import cn.org.yxj.cpm.android.bindingx.core.BindingXEventType;
import cn.org.yxj.cpm.android.bindingx.core.IEventHandler;
import cn.org.yxj.cpm.android.bindingx.core.PlatformManager;
import cn.org.yxj.cpm.android.bindingx.core.internal.ExpressionPair;
import org.apache.weex.WXSDKEngine;
import org.apache.weex.annotation.JSMethod;
import org.apache.weex.bridge.JSCallback;

import java.util.List;
import java.util.Map;

/**
 * Description:
 *
 * old binding APIs which will be removed later.
 * you should use {@link WXBindingXModule} instead.
 *
 * Created by rowandjj(chuyi)<br/>
 */

@Deprecated
//@WeexModule(name = "expressionBinding")
public final class WXExpressionBindingModule extends WXSDKEngine.DestroyableModule {

    private BindingXCore mExpressionBindingCore;
    private PlatformManager mPlatformManager;

    @JSMethod
    @Deprecated
    public void enableBinding(@Nullable String sourceRef, @Nullable String eventType) {
        if(mPlatformManager == null) {
            mPlatformManager = WXBindingXModule.createPlatformManager(mWXSDKInstance);
        }
        if (mExpressionBindingCore == null) {
            mExpressionBindingCore = new BindingXCore(mPlatformManager);
            mExpressionBindingCore.registerEventHandler(BindingXEventType.TYPE_SCROLL,
                    new BindingXCore.ObjectCreator<IEventHandler, Context, PlatformManager>() {
                        @Override
                        public IEventHandler createWith(@NonNull Context context, @NonNull PlatformManager manager, Object... extension) {
                            return new BindingXScrollHandler(context, manager, extension);
                        }
                    });
        }
    }

    @JSMethod
    @Deprecated
    public void createBinding(@Nullable String sourceRef, @Nullable String eventType, @Nullable String exitExpression,
                              @Nullable List<Map<String, Object>> expressionArgs, @Nullable final JSCallback callback) {
        enableBinding(null,null);

        ExpressionPair exitExpressionPair = ExpressionPair.create(null, exitExpression);
        mExpressionBindingCore.doBind(
                sourceRef,
                null,
                eventType,
                null,
                exitExpressionPair,
                expressionArgs,
                null,
                new BindingXCore.JavaScriptCallback() {
                    @Override
                    public void callback(Object params) {
                        if (callback != null) {
                            callback.invokeAndKeepAlive(params);
                        }
                    }
                },
                mWXSDKInstance == null ? null : mWXSDKInstance.getContext(),
                mWXSDKInstance == null ? null : mWXSDKInstance.getInstanceId(),null);
    }

    @JSMethod
    @Deprecated
    public void disableBinding(@Nullable String sourceRef, @Nullable String eventType) {
        if (mExpressionBindingCore != null) {
            mExpressionBindingCore.doUnbind(sourceRef, eventType);
        }
    }

    @JSMethod
    @Deprecated
    public void disableAll() {
        if (mExpressionBindingCore != null) {
            mExpressionBindingCore.doRelease();
        }
    }

    @Override
    public void destroy() {
        if (mExpressionBindingCore != null) {
            mExpressionBindingCore.doRelease();
            mExpressionBindingCore = null;
        }
    }

    ///////// Lifecycle Callbacks

    @Override
    public void onActivityPause() {
        if (mExpressionBindingCore != null) {
            mExpressionBindingCore.onActivityPause();
        }
    }

    @Override
    public void onActivityResume() {
        if (mExpressionBindingCore != null) {
            mExpressionBindingCore.onActivityResume();
        }
    }

}
