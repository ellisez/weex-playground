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

import java.util.Map;

/**
 * Description:
 *
 * Created by rowandjj(chuyi)<br/>
 */
/*package*/ class SpringAnimationDriver extends PhysicsAnimationDriver {

    // maximum amount of time to simulate per physics iteration in seconds (4 frames at 60 FPS)
    private static final double MAX_DELTA_TIME_SEC = 0.064;

    // storage for the current and prior physics state while integration is occurring
    private static class PhysicsState {
        double position;
        double velocity;
    }

    private long mLastTime;
    private boolean mSpringStarted;

    // configuration
    private double mSpringStiffness;
    private double mSpringDamping;
    private double mSpringMass;
    private double mInitialVelocity;
    private boolean mOvershootClampingEnabled;

    private final PhysicsState mCurrentState = new PhysicsState();

    private double mStartValue;
    private double mEndValue;

    // thresholds for determining when the spring is at rest
    private double mRestSpeedThreshold;
    private double mDisplacementFromRestThreshold;
    private double mTimeAccumulator;

    @Override
    void onAnimationStart(@NonNull Map<String,Object> configMap) {
        mVelocity = mCurrentState.velocity = Utils.getDoubleValue(configMap,"initialVelocity",0);
        mSpringStiffness = Utils.getDoubleValue(configMap,"stiffness",100);
        mSpringDamping = Utils.getDoubleValue(configMap, "damping",10);
        mSpringMass = Utils.getDoubleValue(configMap, "mass",1);
        mInitialVelocity = mCurrentState.velocity;

        mValue = Utils.getDoubleValue(configMap,"fromValue", 0);// 起始值默认为0
        mEndValue = Utils.getDoubleValue(configMap,"toValue",1); // 结束值默认为1

        mRestSpeedThreshold = Utils.getDoubleValue(configMap,"restSpeedThreshold",0.001d);
        mDisplacementFromRestThreshold = Utils.getDoubleValue(configMap,"restDisplacementThreshold",0.001d);
        mOvershootClampingEnabled = Utils.getBooleanValue(configMap,"overshootClamping", false);
        mHasFinished = false;
        mTimeAccumulator = 0;
        mSpringStarted = false;
    }

    @Override
    void runAnimationStep(long frameTimeMillis) {
        if (!mSpringStarted) {
            mStartValue = mCurrentState.position = mValue;
            mLastTime = frameTimeMillis;
            mTimeAccumulator = 0.0;
            mSpringStarted = true;
        }
        advance((frameTimeMillis - mLastTime) / 1000.0);
        mLastTime = frameTimeMillis;

        mValue = mCurrentState.position;
        mVelocity = mCurrentState.velocity;

        if (isAtRest()) {
            mHasFinished = true;
        }
    }

    @Override
    boolean isAtRest() {
        return Math.abs(mCurrentState.velocity) <= mRestSpeedThreshold &&
                (getDisplacementDistanceForState(mCurrentState) <= mDisplacementFromRestThreshold ||
                        mSpringStiffness == 0);
    }

    private double getDisplacementDistanceForState(PhysicsState state) {
        return Math.abs(mEndValue - state.position);
    }

    private boolean isOvershooting() {
        return mSpringStiffness > 0 &&
                ((mStartValue < mEndValue && mCurrentState.position > mEndValue) ||
                        (mStartValue > mEndValue && mCurrentState.position < mEndValue));
    }

    private void advance(double realDeltaTime) {
        if (isAtRest()) {
            return;
        }

        // clamp the amount of realTime to simulate to avoid stuttering in the UI. We should be able
        // to catch up in a subsequent advance if necessary.
        double adjustedDeltaTime = realDeltaTime;
        if (realDeltaTime > MAX_DELTA_TIME_SEC) {
            adjustedDeltaTime = MAX_DELTA_TIME_SEC;
        }

        mTimeAccumulator += adjustedDeltaTime;

        double c = mSpringDamping;
        double m = mSpringMass;
        double k = mSpringStiffness;
        double v0 = -mInitialVelocity;

        double zeta = c / (2 * Math.sqrt(k * m ));
        double omega0 = Math.sqrt(k / m);
        double omega1 = omega0 * Math.sqrt(1.0 - (zeta * zeta));
        double x0 = mEndValue - mStartValue;

        double velocity;
        double position;
        double t = mTimeAccumulator;
        if (zeta < 1) {
            // Under damped
            double envelope = Math.exp(-zeta * omega0 * t);
            position =
                    mEndValue -
                            envelope *
                                    ((v0 + zeta * omega0 * x0) / omega1 * Math.sin(omega1 * t) +
                                            x0 * Math.cos(omega1 * t));
            // This looks crazy -- it's actually just the derivative of the
            // oscillation function
            velocity =
                    zeta *
                            omega0 *
                            envelope *
                            (Math.sin(omega1 * t) * (v0 + zeta * omega0 * x0) / omega1 +
                                    x0 * Math.cos(omega1 * t)) -
                            envelope *
                                    (Math.cos(omega1 * t) * (v0 + zeta * omega0 * x0) -
                                            omega1 * x0 * Math.sin(omega1 * t));
        } else {
            // Critically damped spring
            double envelope = Math.exp(-omega0 * t);
            position = mEndValue - envelope * (x0 + (v0 + omega0 * x0) * t);
            velocity =
                    envelope * (v0 * (t * omega0 - 1) + t * x0 * (omega0 * omega0));
        }

        mCurrentState.position = position;
        mCurrentState.velocity = velocity;

        // End the spring immediately if it is overshooting and overshoot clamping is enabled.
        // Also make sure that if the spring was considered within a resting threshold that it's now
        // snapped to its end value.
        if (isAtRest() || (mOvershootClampingEnabled && isOvershooting())) {
            // Don't call setCurrentValue because that forces a call to onSpringUpdate
            if (mSpringStiffness > 0) {
                mStartValue = mEndValue;
                mCurrentState.position = mEndValue;
            } else {
                mEndValue = mCurrentState.position;
                mStartValue = mEndValue;
            }
            mCurrentState.velocity = 0;
        }
    }
}
