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
package cn.org.yxj.android.bindingx.core.internal;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cn.org.yxj.android.bindingx.core.BindingXCore;
import cn.org.yxj.android.bindingx.core.BindingXEventType;
import cn.org.yxj.android.bindingx.core.LogProxy;
import cn.org.yxj.android.bindingx.core.PlatformManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */
public class BindingXSpringHandler extends AbstractEventHandler implements PhysicsAnimationDriver.OnAnimationUpdateListener, PhysicsAnimationDriver.OnAnimationEndListener {

    private SpringAnimationDriver mSpringAnimationDriver;

    public BindingXSpringHandler(Context context, PlatformManager manager, Object... extension) {
        super(context, manager, extension);
    }

    @Override
    public void onBindExpression(@NonNull String eventType,
                                 @Nullable Map<String, Object> globalConfig,
                                 @Nullable ExpressionPair exitExpressionPair,
                                 @NonNull List<Map<String, Object>> expressionArgs,
                                 @Nullable BindingXCore.JavaScriptCallback callback) {
        super.onBindExpression(eventType, globalConfig, exitExpressionPair, expressionArgs, callback);

        double velocity = 0;
        double position = 0;
        if(mSpringAnimationDriver != null) {
            velocity = mSpringAnimationDriver.getCurrentVelocity();
            position = mSpringAnimationDriver.getCurrentValue();
            mSpringAnimationDriver.cancel();
        }
        mSpringAnimationDriver = new SpringAnimationDriver();
        mSpringAnimationDriver.setOnAnimationUpdateListener(this);
        mSpringAnimationDriver.setOnAnimationEndListener(this);
        mSpringAnimationDriver.start(resolveParams(mOriginParams, position, velocity));
        fireEventByState(BindingXConstants.STATE_START, mSpringAnimationDriver.getCurrentValue(),mSpringAnimationDriver.getCurrentVelocity());
    }

    private Map<String, Object> resolveParams(Map<String, Object> originalParams,double position, double velocity) {
        if(originalParams == null) {
            return Collections.emptyMap();
        }
        Map<String,Object> map = Utils.getMapValue(originalParams,BindingXConstants.KEY_EVENT_CONFIG);
        if(map.get("initialVelocity") == null) {
            if(map.isEmpty()) {
                map = new HashMap<>();
            }
            map.put("initialVelocity", velocity);
        }
        if(map.get("fromValue") == null) {
            if(map.isEmpty()) {
                map = new HashMap<>();
            }
            map.put("fromValue", position);
        }
        return map;
    }

    @Override
    protected void onExit(@NonNull Map<String, Object> scope) {
        double p = (double) scope.get("p");
        double v = (double) scope.get("v");
        fireEventByState(BindingXConstants.STATE_EXIT, p, v);
        if(mSpringAnimationDriver != null) {
            mSpringAnimationDriver.cancel();
        }
    }

    @SuppressWarnings("unchecked")
    private void fireEventByState(@BindingXConstants.State String state, double position, double velocity, Object... extension) {
        if (mCallback != null) {
            Map<String, Object> param = new HashMap<>();
            param.put("state", state);
            param.put("position", position);
            param.put("velocity", velocity);
            param.put(BindingXConstants.KEY_TOKEN, mToken);

            if(extension != null && extension.length > 0 && extension[0] instanceof Map) {
                param.putAll((Map<String,Object>) extension[0]);
            }

            mCallback.callback(param);
            LogProxy.d(">>>>>>>>>>>fire event:(" + state + ",position:" + position + ",velocity:"+velocity+")");
        }
    }

    @Override
    protected void onUserIntercept(String interceptorName, @NonNull Map<String, Object> scope) {
        if(mSpringAnimationDriver != null) {
            fireEventByState(BindingXConstants.STATE_INTERCEPTOR, mSpringAnimationDriver.getCurrentValue(),
                    mSpringAnimationDriver.getCurrentVelocity(), Collections.singletonMap(BindingXConstants.STATE_INTERCEPTOR,interceptorName));
        }
    }

    @Override
    public boolean onCreate(@NonNull String sourceRef, @NonNull String eventType) {
        return true;
    }

    @Override
    public void onStart(@NonNull String sourceRef, @NonNull String eventType) {
        // nope
    }

    @Override
    public boolean onDisable(@NonNull String sourceRef, @NonNull String eventType) {
        clearExpressions();
        if(mSpringAnimationDriver != null) {
            fireEventByState(BindingXConstants.STATE_END, mSpringAnimationDriver.getCurrentValue(), mSpringAnimationDriver.getCurrentVelocity());
            mSpringAnimationDriver.setOnAnimationEndListener(null);
            mSpringAnimationDriver.setOnAnimationUpdateListener(null);
            mSpringAnimationDriver.cancel();
        }
        return true;
    }

    @Override
    public void onActivityPause() {
    }

    @Override
    public void onActivityResume() {
    }

    @Override
    public void onAnimationUpdate(@NonNull PhysicsAnimationDriver driver, double value, double velocity) {
        if(LogProxy.sEnableLog) {
            LogProxy.v(String.format(Locale.getDefault(),"animation update, [value: %f, velocity: %f]",value, velocity));
        }
        try {
            JSMath.applySpringValueToScope(mScope, value, velocity);
            if(!evaluateExitExpression(mExitExpressionPair,mScope)) {
                consumeExpression(mExpressionHoldersMap, mScope, BindingXEventType.TYPE_SPRING);
            }
        } catch (Exception e) {
            LogProxy.e("runtime error", e);
        }
    }

    @Override
    public void onAnimationEnd(@NonNull PhysicsAnimationDriver driver, double value, double velocity) {
        if(LogProxy.sEnableLog) {
            LogProxy.v(String.format(Locale.getDefault(),"animation end, [value: %f, velocity: %f]",value, velocity));
        }
        fireEventByState(BindingXConstants.STATE_END, mSpringAnimationDriver.getCurrentValue(), mSpringAnimationDriver.getCurrentVelocity());
    }
}
