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
import androidx.annotation.VisibleForTesting;
import android.view.animation.AnimationUtils;

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
 * A built-in implementation of {@link com.alibaba.android.bindingx.core.IEventHandler} which handle timing event.
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class BindingXTimingHandler extends AbstractEventHandler implements AnimationFrame.Callback {

    private long mStartTime = 0;

    private AnimationFrame mAnimationFrame;
    private boolean isFinish = false;

    public BindingXTimingHandler(Context context, PlatformManager manager, Object... extension) {
        super(context, manager, extension);
        if(mAnimationFrame == null) {
            mAnimationFrame = AnimationFrame.newInstance();
        } else {
            mAnimationFrame.clear();
        }
    }

    @VisibleForTesting
    /*package*/ BindingXTimingHandler(Context context, PlatformManager manager, AnimationFrame frame, Object... extension) {
        super(context, manager, extension);
        mAnimationFrame = frame;
    }

    @Override
    public boolean onCreate(@NonNull String sourceRef, @NonNull String eventType) {
        return true;
    }

    @Override
    public void onStart(@NonNull String sourceRef, @NonNull String eventType) {
        //nope
    }

    @Override
    public void onBindExpression(@NonNull String eventType,
                                 @Nullable Map<String,Object> globalConfig,
                                 @Nullable ExpressionPair exitExpressionPair,
                                 @NonNull List<Map<String, Object>> expressionArgs,
                                 @Nullable BindingXCore.JavaScriptCallback callback) {
        super.onBindExpression(eventType,globalConfig, exitExpressionPair, expressionArgs, callback);

        if(mAnimationFrame == null) {
            mAnimationFrame = AnimationFrame.newInstance();
        }

        fireEventByState(BindingXConstants.STATE_START, 0);

        mAnimationFrame.clear();
        mAnimationFrame.requestAnimationFrame(this);
    }

    private void handleTimingCallback() {
        long deltaT;
        if(mStartTime == 0) {
            mStartTime = AnimationUtils.currentAnimationTimeMillis();
            deltaT = 0;
            isFinish = false;
        } else {
            deltaT = AnimationUtils.currentAnimationTimeMillis() - mStartTime;
        }

        try {
            if(LogProxy.sEnableLog) {
                LogProxy.d(String.format(Locale.getDefault(), "[TimingHandler] timing elapsed. (t:%d)", deltaT));
            }
            JSMath.applyTimingValuesToScope(mScope, deltaT);
            if(!isFinish) {
                consumeExpression(mExpressionHoldersMap, mScope, BindingXEventType.TYPE_TIMING);
            }
            isFinish = evaluateExitExpression(mExitExpressionPair,mScope);
        } catch (Exception e) {
            LogProxy.e("runtime error", e);
        }
    }

    @Override
    public boolean onDisable(@NonNull String sourceRef, @NonNull String eventType) {
        fireEventByState(BindingXConstants.STATE_END, (System.currentTimeMillis() - mStartTime));
        clearExpressions();
        if(mAnimationFrame != null) {
            mAnimationFrame.clear();
        }
        mStartTime = 0;

        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clearExpressions();

        if(mAnimationFrame != null) {
            mAnimationFrame.terminate();
            mAnimationFrame = null;
        }
        mStartTime = 0;
    }

    @Override
    protected void onExit(@NonNull Map<String, Object> scope) {
        double t = (double) scope.get("t");
        fireEventByState(BindingXConstants.STATE_EXIT, (long) t);

        if(mAnimationFrame != null) {
            mAnimationFrame.clear();
        }
        mStartTime = 0;
    }

    @Override
    protected void onUserIntercept(String interceptorName, @NonNull Map<String, Object> scope) {
        double t = (double) scope.get("t");
        fireEventByState(BindingXConstants.STATE_INTERCEPTOR, (long)t, Collections.singletonMap(BindingXConstants.STATE_INTERCEPTOR,interceptorName));
    }

    @SuppressWarnings("unchecked")
    private void fireEventByState(@BindingXConstants.State String state, long t, Object... extension) {
        if (mCallback != null) {
            Map<String, Object> param = new HashMap<>();
            param.put("state", state);
            param.put("t", t);
            param.put(BindingXConstants.KEY_TOKEN, mToken);

            if(extension != null && extension.length > 0 && extension[0] instanceof Map) {
                param.putAll((Map<String,Object>) extension[0]);
            }

            mCallback.callback(param);
            LogProxy.d(">>>>>>>>>>>fire event:(" + state + "," + t + ")");
        }
    }

    @Override
    public void doFrame() {
        handleTimingCallback();
    }

    @Override
    public void onActivityPause() {
    }

    @Override
    public void onActivityResume() {
    }

}
