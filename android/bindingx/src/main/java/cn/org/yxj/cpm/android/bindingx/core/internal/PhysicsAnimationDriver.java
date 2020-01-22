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

import androidx.annotation.NonNull;
import android.view.animation.AnimationUtils;

import java.util.Map;

/**
 * Description:
 *
 *
 * Created by rowandjj(chuyi)<br/>
 */
/*package*/ abstract class PhysicsAnimationDriver implements AnimationFrame.Callback{

    interface OnAnimationUpdateListener {
        void onAnimationUpdate(@NonNull PhysicsAnimationDriver driver, double value, double velocity);
    }

    interface OnAnimationEndListener {
        void onAnimationEnd(@NonNull PhysicsAnimationDriver driver, double value, double velocity);
    }

    private AnimationFrame mAnimationFrame;

    protected OnAnimationUpdateListener mAnimationUpdateListener;
    protected OnAnimationEndListener mAnimationEndListener;

    protected double mValue;
    protected double mVelocity;
    protected boolean mHasFinished;

    abstract void onAnimationStart(@NonNull Map<String,Object> configMap);
    abstract void runAnimationStep(long frameTimeMillis);
    abstract boolean isAtRest();

    void setOnAnimationUpdateListener(OnAnimationUpdateListener listener) {
        this.mAnimationUpdateListener = listener;
    }

    void setOnAnimationEndListener(OnAnimationEndListener listener) {
        this.mAnimationEndListener = listener;
    }

    void start(@NonNull Map<String,Object> configMap) {
        onAnimationStart(configMap);
        if(mAnimationFrame == null) {
            mAnimationFrame = AnimationFrame.newInstance();
        }
        mAnimationFrame.requestAnimationFrame(this);
    }

    void cancel() {
        if(mAnimationFrame != null) {
            mAnimationFrame.clear();
        }
        mHasFinished = false;
    }

    boolean hasFinished() {
        return mHasFinished;
    }

    double getCurrentValue() {
        return mValue;
    }

    double getCurrentVelocity() {
        return mVelocity;
    }

    @Override
    public void doFrame() {
        runAnimationStep(AnimationUtils.currentAnimationTimeMillis());
        if(mAnimationUpdateListener != null) {
            mAnimationUpdateListener.onAnimationUpdate(this, mValue, mVelocity);
        }
        if(hasFinished()) {
            if(mAnimationEndListener != null) {
                mAnimationEndListener.onAnimationEnd(this, mValue, mVelocity);
            }
            if(mAnimationFrame != null) {
                mAnimationFrame.clear();
            }
        }
    }
}
