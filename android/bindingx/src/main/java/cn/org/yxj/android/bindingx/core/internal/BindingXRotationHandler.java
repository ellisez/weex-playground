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
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;

import cn.org.yxj.android.bindingx.core.BindingXEventType;
import cn.org.yxj.android.bindingx.core.LogProxy;
import cn.org.yxj.android.bindingx.core.PlatformManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Description:
 *
 * A built-in implementation of {@link com.alibaba.android.bindingx.core.IEventHandler} which handle rotation gesture event.
 *
 * Created by rowandjj(chuyi)<br/>
 */
public class BindingXRotationHandler extends AbstractEventHandler implements View.OnTouchListener, RotationGestureDetector.OnRotationGestureListener {

    private RotationGestureDetector mRotationGestureDetector;
    private double mAbsoluteRotationInDegrees;

    public BindingXRotationHandler(Context context, PlatformManager manager, Object... extension) {
        super(context, manager, extension);
        mRotationGestureDetector = new RotationGestureDetector(this);
    }

    @Override
    protected void onExit(@NonNull Map<String, Object> scope) {
        double rotation = (double) scope.get("r");
        fireEventByState(BindingXConstants.STATE_EXIT, rotation);
    }

    @Override
    protected void onUserIntercept(String interceptorName, @NonNull Map<String, Object> scope) {
        double rotation = (double) scope.get("r");
        fireEventByState(BindingXConstants.STATE_INTERCEPTOR, rotation, Collections.singletonMap(BindingXConstants.STATE_INTERCEPTOR,interceptorName));
    }

    @Override
    public boolean onCreate(@NonNull String sourceRef, @NonNull String eventType) {
        String instanceId = TextUtils.isEmpty(mAnchorInstanceId) ? mInstanceId : mAnchorInstanceId;
        View sourceView = mPlatformManager.getViewFinder().findViewBy(sourceRef, instanceId);
        if (sourceView == null) {
            LogProxy.e("[RotationHandler] onCreate failed. sourceView not found:" + sourceRef);
            return false;
        }
        sourceView.setOnTouchListener(this);
        LogProxy.d("[RotationHandler] onCreate success. {source:" + sourceRef + ",type:" + eventType + "}");
        return true;
    }

    @Override
    public void onStart(@NonNull String sourceRef, @NonNull String eventType) {
        // nope
    }

    @Override
    public boolean onDisable(@NonNull String sourceRef, @NonNull String eventType) {
        String instanceId = TextUtils.isEmpty(mAnchorInstanceId) ? mInstanceId : mAnchorInstanceId;
        View hostView = mPlatformManager.getViewFinder().findViewBy(sourceRef, instanceId);
        LogProxy.d("remove touch listener success.[" + sourceRef + "," + eventType + "]");
        if (hostView != null) {
            hostView.setOnTouchListener(null);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onActivityPause() {
        // nope
    }

    @Override
    public void onActivityResume() {
        // nope
    }

    @SuppressWarnings("unchecked")
    private void fireEventByState(@BindingXConstants.State String state, double rotation, Object... extension) {
        if (mCallback != null) {
            Map<String, Object> param = new HashMap<>();
            param.put("state", state);
            param.put("rotation", rotation);
            param.put(BindingXConstants.KEY_TOKEN, mToken);

            if(extension != null && extension.length > 0 && extension[0] instanceof Map) {
                param.putAll((Map<String,Object>) extension[0]);
            }

            mCallback.callback(param);
            LogProxy.d(">>>>>>>>>>>fire event:(" + state + "," + rotation + ")");
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mRotationGestureDetector.onTouchEvent(event);
    }

    // -------------- Rotation Gesture Callback -----------------

    @Override
    public void onRotation(RotationGestureDetector detector) {
        try {

            double rotation = detector.getRotationInDegrees();
            mAbsoluteRotationInDegrees += rotation;

            if(LogProxy.sEnableLog) {
                LogProxy.d(String.format(Locale.getDefault(), "[RotationHandler] current rotation in degrees: %f", mAbsoluteRotationInDegrees));
            }
            JSMath.applyRotationInDegreesToScope(mScope,mAbsoluteRotationInDegrees);
            if(!evaluateExitExpression(mExitExpressionPair,mScope)) {
                consumeExpression(mExpressionHoldersMap, mScope, BindingXEventType.TYPE_ROTATION);
            }
        } catch (Exception e) {
            LogProxy.e("runtime error", e);
        }
    }

    @Override
    public void onRotationBegin(RotationGestureDetector detector) {
        LogProxy.d("[RotationHandler] rotation gesture begin");
        fireEventByState(BindingXConstants.STATE_START, 0f);
    }

    @Override
    public void onRotationEnd(RotationGestureDetector detector) {
        LogProxy.d("[RotationHandler] rotation gesture end");
        fireEventByState(BindingXConstants.STATE_END, mAbsoluteRotationInDegrees);
        mAbsoluteRotationInDegrees = 0;
    }
}
