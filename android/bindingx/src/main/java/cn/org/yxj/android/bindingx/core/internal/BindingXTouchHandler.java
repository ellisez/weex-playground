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
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;

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
 * A built-in implementation of {@link com.alibaba.android.bindingx.core.IEventHandler} which handle pan event.
 */
public class BindingXTouchHandler extends AbstractEventHandler implements View.OnTouchListener, GestureDetector.OnGestureListener {
    private float mDownX;
    private float mDownY;

    private double mDx;
    private double mDy;

    private GestureDetector mGestureDetector;
    private VelocityTracker mVelocityTracker;

    private int mMinimumFlingVelocity;
    private int mMaximumFlingVelocity;

    public BindingXTouchHandler(Context context, PlatformManager manager, Object... extension) {
        super(context, manager, extension);
        Handler handler = new Handler(Looper.myLooper() == null ? Looper.getMainLooper() : Looper.myLooper());
        mGestureDetector = new GestureDetector(context, this, handler);

        ViewConfiguration configuration = ViewConfiguration.get(context);
        mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity();
        mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        try {
            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain();
            }
            mVelocityTracker.addMovement(event);

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    mDownX = event.getRawX();
                    mDownY = event.getRawY();
                    fireEventByState(BindingXConstants.STATE_START, 0, 0,0,0);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mDownX == 0 && mDownY == 0) {
                        mDownX = event.getRawX();
                        mDownY = event.getRawY();
                        fireEventByState(BindingXConstants.STATE_START, 0, 0,0,0);
                        break;
                    }
                    mDx = event.getRawX() - mDownX;
                    mDy = event.getRawY() - mDownY;
                    break;
                case MotionEvent.ACTION_UP:
                    mDownX = 0;
                    mDownY = 0;
                    clearExpressions();

                    mVelocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                    float velocityX = mVelocityTracker.getXVelocity();
                    float velocityY = mVelocityTracker.getYVelocity();

                    fireEventByState(BindingXConstants.STATE_END, mDx, mDy,velocityX,velocityY);
                    //bugFixed:we must reset dx & dy every time.
                    mDx = 0;
                    mDy = 0;
                    if(mVelocityTracker != null) {
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    mDownX = 0;
                    mDownY = 0;
                    clearExpressions();
                    fireEventByState(BindingXConstants.STATE_CANCEL, mDx, mDy,0,0);
                    if(mVelocityTracker != null) {
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                    }
                    break;
            }
        } catch (Exception e) {
            LogProxy.e("runtime error ", e);
        }
        return mGestureDetector.onTouchEvent(event);
    }


    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        float downX, downY;
        if (e1 == null) {
            //the first time e1 is null.
            downX = mDownX;
            downY = mDownY;
        } else {
            downX = e1.getRawX();
            downY = e1.getRawY();
        }

        if (e2 == null) {
            return false;
        }

        float curX = e2.getRawX();
        float curY = e2.getRawY();

        float deltaX = curX - downX;
        float deltaY = curY - downY;
        try {
            if(LogProxy.sEnableLog) {
                LogProxy.d(String.format(Locale.getDefault(), "[TouchHandler] pan moved. (x:%f,y:%f)", deltaX,deltaY));
            }
            JSMath.applyXYToScope(mScope, deltaX, deltaY, mPlatformManager.getResolutionTranslator());
            if(!evaluateExitExpression(mExitExpressionPair,mScope)) {
                consumeExpression(mExpressionHoldersMap, mScope, BindingXEventType.TYPE_PAN);
            }
        } catch (Exception e) {
            LogProxy.e("runtime error", e);
        }

        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onCreate(@NonNull String sourceRef, @NonNull String eventType) {
        String instanceId = TextUtils.isEmpty(mAnchorInstanceId) ? mInstanceId : mAnchorInstanceId;
        View sourceView = mPlatformManager.getViewFinder().findViewBy(sourceRef, instanceId);
        if (sourceView == null) {
            LogProxy.e("[ExpressionTouchHandler] onCreate failed. sourceView not found:" + sourceRef);
            return false;
        }
        sourceView.setOnTouchListener(this);
        LogProxy.d("[ExpressionTouchHandler] onCreate success. {source:" + sourceRef + ",type:" + eventType + "}");
        return true;
    }

    @Override
    public void onStart(@NonNull String sourceRef, @NonNull String eventType) {
        // nope
    }

    @Override
    public void onBindExpression(@NonNull String eventType,
                                 @Nullable Map<String,Object> globalConfig,
                                 @Nullable ExpressionPair exitExpressionPair,
                                 @NonNull List<Map<String, Object>> expressionArgs,
                                 @Nullable BindingXCore.JavaScriptCallback callback) {
        super.onBindExpression(eventType,globalConfig, exitExpressionPair, expressionArgs, callback);
    }

    @Override
    public boolean onDisable(@NonNull String sourceRef, @NonNull String eventType) {
        String instanceId = TextUtils.isEmpty(mAnchorInstanceId) ? mInstanceId : mAnchorInstanceId;
        View hostView = mPlatformManager.getViewFinder().findViewBy(sourceRef, instanceId);
        if (hostView != null) {
            hostView.setOnTouchListener(null);
        }
        LogProxy.d("remove touch listener success.[" + sourceRef + "," + eventType + "]");
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mExpressionHoldersMap != null) {
            mExpressionHoldersMap.clear();
            mExpressionHoldersMap = null;
        }
        mExitExpressionPair = null;
        mCallback = null;
    }

    @Override
    protected void onExit(@NonNull Map<String, Object> scope) {
        double deltaX = (double) scope.get("internal_x");
        double deltaY = (double) scope.get("internal_y");
        fireEventByState(BindingXConstants.STATE_EXIT, deltaX, deltaY,0,0);
    }

    @Override
    protected void onUserIntercept(String interceptorName, @NonNull Map<String, Object> scope) {
        double deltaX = (double) scope.get("internal_x");
        double deltaY = (double) scope.get("internal_y");
        fireEventByState(BindingXConstants.STATE_INTERCEPTOR, deltaX, deltaY, 0,0,Collections.singletonMap(BindingXConstants.STATE_INTERCEPTOR,interceptorName));
    }

    @SuppressWarnings("unchecked")
    private void fireEventByState(@BindingXConstants.State String state, double dx, double dy, float velocityX, float velocityY, Object... extension) {
        if (mCallback != null) {
            Map<String, Object> param = new HashMap<>();
            param.put("state", state);
            double x = mPlatformManager.getResolutionTranslator().nativeToWeb(dx);
            double y = mPlatformManager.getResolutionTranslator().nativeToWeb(dy);
            param.put("deltaX", x);
            param.put("deltaY", y);
            if(BindingXConstants.STATE_END.equals(state)) {
                param.put("velocityX", velocityX);
                param.put("velocityY", velocityY);
            }

            param.put(BindingXConstants.KEY_TOKEN, mToken);

            if(extension != null && extension.length > 0 && extension[0] instanceof Map) {
                param.putAll((Map<String,Object>) extension[0]);
            }

            mCallback.callback(param);
            LogProxy.d(">>>>>>>>>>>fire event:(" + state + "," + x + "," + y + ")");
        }
    }

    @Override
    public void onActivityPause() {
    }

    @Override
    public void onActivityResume() {
    }

}
