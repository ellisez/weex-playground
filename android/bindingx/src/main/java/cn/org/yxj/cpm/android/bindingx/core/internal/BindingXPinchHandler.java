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
package cn.org.yxj.cpm.android.bindingx.core.internal;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import cn.org.yxj.cpm.android.bindingx.core.BindingXEventType;
import cn.org.yxj.cpm.android.bindingx.core.LogProxy;
import cn.org.yxj.cpm.android.bindingx.core.PlatformManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Description:
 *
 *  A built-in implementation of {@link com.alibaba.android.bindingx.core.IEventHandler} which handle pinch gesture event.
 *
 * Created by rowandjj(chuyi)<br/>
 */
public class BindingXPinchHandler extends AbstractEventHandler implements View.OnTouchListener, ScaleGestureDetector.OnScaleGestureListener {

    private ScaleGestureDetector mScaleGestureDetector;
    private boolean mInProgress;
    private int mPointerIds[] = new int[2];
    private double mAbsoluteScaleFactor = 1d;

    public BindingXPinchHandler(Context context, PlatformManager manager, Object... extension) {
        super(context, manager, extension);
        Handler handler = new Handler(Looper.myLooper() == null ? Looper.getMainLooper() : Looper.myLooper());
        if(Build.VERSION.SDK_INT >= 19) {
            mScaleGestureDetector = new ScaleGestureDetector(context,this, handler);
        } else {
            mScaleGestureDetector = new ScaleGestureDetector(context, this);
        }
    }

    @Override
    protected void onExit(@NonNull Map<String, Object> scope) {
        double scaleFactor = (double) scope.get("s");
        fireEventByState(BindingXConstants.STATE_EXIT, scaleFactor);
    }

    @Override
    protected void onUserIntercept(String interceptorName, @NonNull Map<String, Object> scope) {
        double scaleFactor = (double) scope.get("s");
        fireEventByState(BindingXConstants.STATE_INTERCEPTOR, scaleFactor, Collections.singletonMap(BindingXConstants.STATE_INTERCEPTOR,interceptorName));
    }

    @Override
    public boolean onCreate(@NonNull String sourceRef, @NonNull String eventType) {
        String instanceId = TextUtils.isEmpty(mAnchorInstanceId) ? mInstanceId : mAnchorInstanceId;
        View sourceView = mPlatformManager.getViewFinder().findViewBy(sourceRef, instanceId);
        if (sourceView == null) {
            LogProxy.e("[BindingXPinchHandler] onCreate failed. sourceView not found:" + sourceRef);
            return false;
        }
        sourceView.setOnTouchListener(this);
        LogProxy.d("[BindingXPinchHandler] onCreate success. {source:" + sourceRef + ",type:" + eventType + "}");
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
    private void fireEventByState(@BindingXConstants.State String state, double scaleFactor, Object... extension) {
        if (mCallback != null) {
            Map<String, Object> param = new HashMap<>();
            param.put("state", state);
            param.put("scale", scaleFactor);
            param.put(BindingXConstants.KEY_TOKEN, mToken);

            if(extension != null && extension.length > 0 && extension[0] instanceof Map) {
                param.putAll((Map<String,Object>) extension[0]);
            }

            mCallback.callback(param);
            LogProxy.d(">>>>>>>>>>>fire event:(" + state + "," + scaleFactor + ")");
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mInProgress = false;
                mPointerIds[0] = event.getPointerId(event.getActionIndex());
                mPointerIds[1] = MotionEvent.INVALID_POINTER_ID;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if(!mInProgress) {
                    mPointerIds[1] = event.getPointerId(event.getActionIndex());
                    mInProgress = true;
                    pinchStart();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                pinchEnd();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if(mInProgress) {
                    int pointerId = event.getPointerId(event.getActionIndex());
                    if (pointerId == mPointerIds[0] || pointerId == mPointerIds[1]) {
                        // One of the key pointer has been lifted up, pinch gesture end
                        pinchEnd();
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return mScaleGestureDetector.onTouchEvent(event);
    }

    private void pinchStart() {
        LogProxy.d("[PinchHandler] pinch gesture begin");
        fireEventByState(BindingXConstants.STATE_START, 1.0f);
    }

    private void pinchEnd() {
        if (mInProgress) {
            LogProxy.d("[PinchHandler] pinch gesture end");
            fireEventByState(BindingXConstants.STATE_END, mAbsoluteScaleFactor);
            // reset states
            mInProgress = false;
            mPointerIds[0] = MotionEvent.INVALID_POINTER_ID;
            mPointerIds[1] = MotionEvent.INVALID_POINTER_ID;
            mAbsoluteScaleFactor = 1d;
        }
    }


    // -------------- Pinch Gesture Callback -----------------

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scaleFactor = detector.getScaleFactor();
        if (Float.isNaN(scaleFactor) || Float.isInfinite(scaleFactor))
            return true;
        mAbsoluteScaleFactor *= scaleFactor;

        try {
            if(LogProxy.sEnableLog) {
                LogProxy.d(String.format(Locale.getDefault(), "[PinchHandler] current scale factor: %f", mAbsoluteScaleFactor));
            }
            JSMath.applyScaleFactorToScope(mScope, mAbsoluteScaleFactor);
            if(!evaluateExitExpression(mExitExpressionPair,mScope)) {
                consumeExpression(mExpressionHoldersMap, mScope, BindingXEventType.TYPE_PINCH);
            }
        } catch (Exception e) {
            LogProxy.e("runtime error", e);
        }

        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        // nope
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        // nope
    }

}
