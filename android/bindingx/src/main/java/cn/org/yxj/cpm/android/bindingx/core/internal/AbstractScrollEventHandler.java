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
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

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
 * An abstract scroll event handler. Because there are some difference between Weex and ReactNative.
 * In Weex, both Scroller and List are scrollable. However in ReactNative, only ScrollView is scrollable.
 *
 * Created by rowandjj(chuyi)<br/>
 */

public abstract class AbstractScrollEventHandler extends AbstractEventHandler {

    protected int mContentOffsetX, mContentOffsetY;
    private boolean isStart = false;

    public AbstractScrollEventHandler(Context context, PlatformManager manager, Object... extension) {
        super(context, manager, extension);
    }

    @Override
    @CallSuper
    public boolean onDisable(@NonNull String sourceRef, @NonNull String eventType) {
        clearExpressions();
        isStart = false;
        fireEventByState(BindingXConstants.STATE_END, mContentOffsetX, mContentOffsetY,0,0,0,0);
        return true;
    }

    @Override
    protected void onExit(@NonNull Map<String, Object> scope) {
        double contentOffsetX = (double) scope.get("internal_x");
        double contentOffsetY = (double) scope.get("internal_y");
        this.fireEventByState(BindingXConstants.STATE_EXIT, contentOffsetX, contentOffsetY,0,0,0,0);
    }

    @Override
    protected void onUserIntercept(String interceptorName, @NonNull Map<String, Object> scope) {
        double contentOffsetX = (double) scope.get("internal_x");
        double contentOffsetY = (double) scope.get("internal_y");
        double dx = (double) scope.get("dx");
        double dy = (double) scope.get("dy");
        double tdx = (double) scope.get("tdx");
        double tdy = (double) scope.get("tdy");
        this.fireEventByState(BindingXConstants.STATE_INTERCEPTOR, contentOffsetX, contentOffsetY,dx,dy,tdx,tdy, Collections.singletonMap(BindingXConstants.STATE_INTERCEPTOR,interceptorName));
    }

    @Override
    @CallSuper
    public void onDestroy() {
        super.onDestroy();
        isStart = false;
    }

    /**
     * @param contentOffsetX the absolute horizontal offset in pixel
     * @param contentOffsetY the absolute vertical offset in pixel
     * @param dx The amount of horizontal scroll relative to last onscroll event
     * @param dy The amount of vertical scroll offset relative to last onscroll event
     * @param tdx The amount of horizontal scroll offset relative to last inflection point
     * @param tdy The amount of vertical scroll offset relative to last inflection point
     * */
    protected void handleScrollEvent(int contentOffsetX, int contentOffsetY, int dx, int dy,
                                   int tdx, int tdy) {
        handleScrollEvent(contentOffsetX,contentOffsetY,dx,dy,tdx,tdy, BindingXEventType.TYPE_SCROLL);
    }


    protected void handleScrollEvent(int contentOffsetX, int contentOffsetY, int dx, int dy,
                                     int tdx, int tdy, String eventType) {
        if(LogProxy.sEnableLog) {
            LogProxy.d(String.format(Locale.getDefault(),
                    "[ScrollHandler] scroll changed. (contentOffsetX:%d,contentOffsetY:%d,dx:%d,dy:%d,tdx:%d,tdy:%d)",
                    contentOffsetX,contentOffsetY,dx,dy,tdx,tdy));
        }

        this.mContentOffsetX = contentOffsetX;
        this.mContentOffsetY = contentOffsetY;

        if(!isStart) {
            isStart = true;
            fireEventByState(BindingXConstants.STATE_START,contentOffsetX,contentOffsetY,dx,dy,tdx,tdy);
        }

        try {
            JSMath.applyScrollValuesToScope(mScope, contentOffsetX, contentOffsetY, dx, dy, tdx, tdy, mPlatformManager.getResolutionTranslator());
            if(!evaluateExitExpression(mExitExpressionPair,mScope)) {
                consumeExpression(mExpressionHoldersMap, mScope, eventType);
            }
        } catch (Exception e) {
            LogProxy.e("runtime error", e);
        }
    }

    @SuppressWarnings("unchecked")
    protected void fireEventByState(@BindingXConstants.State String state, double contentOffsetX, double contentOffsetY,
                                    double dx, double dy, double tdx, double tdy, Object... extension) {
        if (mCallback != null) {
            Map<String, Object> param = new HashMap<>();
            param.put("state", state);
            double x = mPlatformManager.getResolutionTranslator().nativeToWeb(contentOffsetX);
            double y = mPlatformManager.getResolutionTranslator().nativeToWeb(contentOffsetY);
            param.put("x", x);
            param.put("y", y);

            double _dx = mPlatformManager.getResolutionTranslator().nativeToWeb(dx);
            double _dy = mPlatformManager.getResolutionTranslator().nativeToWeb(dy);
            param.put("dx", _dx);
            param.put("dy", _dy);

            double _tdx = mPlatformManager.getResolutionTranslator().nativeToWeb(tdx);
            double _tdy = mPlatformManager.getResolutionTranslator().nativeToWeb(tdy);
            param.put("tdx", _tdx);
            param.put("tdy", _tdy);
            param.put(BindingXConstants.KEY_TOKEN, mToken);

            if(extension != null && extension.length > 0 && extension[0] instanceof Map) {
                param.putAll((Map<String,Object>) extension[0]);
            }

            mCallback.callback(param);
            LogProxy.d(">>>>>>>>>>>fire event:(" + state + "," + x + "," + y + ","+ _dx  +","+ _dy +"," + _tdx +"," + _tdy +")");
        }
    }

}
