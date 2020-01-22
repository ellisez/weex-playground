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

import android.view.MotionEvent;

/**
 * Detects rotation gestures using the supplied {@link MotionEvent}s.
 * The {@link OnRotationGestureListener} callback will notify users when a particular
 * gesture event has occurred.
 *
 * Created by rowandjj(chuyi)<br/>
 * */
public class RotationGestureDetector {

  public interface OnRotationGestureListener {

    void onRotation(RotationGestureDetector detector);

    void onRotationBegin(RotationGestureDetector detector);

    void onRotationEnd(RotationGestureDetector detector);
  }

  private long mCurrTime;
  private long mPrevTime;
  private double mPrevAngle;
  private double mAngleDiff;
  private float mAnchorX;
  private float mAnchorY;

  private boolean mInProgress;

  private int mPointerIds[] = new int[2];

  private OnRotationGestureListener mListener;

  public RotationGestureDetector(OnRotationGestureListener listener) {
    mListener = listener;
  }

  private void updateCurrent(MotionEvent event) {
    mPrevTime = mCurrTime;
    mCurrTime = event.getEventTime();

    int firstPointerIndex = event.findPointerIndex(mPointerIds[0]);
    int secondPointerIndex = event.findPointerIndex(mPointerIds[1]);

    if(firstPointerIndex == MotionEvent.INVALID_POINTER_ID || secondPointerIndex == MotionEvent.INVALID_POINTER_ID) {
      return;
    }

    float firstPtX = event.getX(firstPointerIndex);
    float firstPtY = event.getY(firstPointerIndex);
    float secondPtX = event.getX(secondPointerIndex);
    float secondPtY = event.getY(secondPointerIndex);

    float vectorX = secondPtX - firstPtX;
    float vectorY = secondPtY - firstPtY;

    mAnchorX = (firstPtX + secondPtX) * 0.5f;
    mAnchorY = (firstPtY + secondPtY) * 0.5f;

    double angle = -Math.atan2(vectorY, vectorX);

    if (Double.isNaN(mPrevAngle)) {
      mAngleDiff = 0.;
    } else {
      mAngleDiff = mPrevAngle - angle;
    }
    mPrevAngle = angle;

    if (mAngleDiff > Math.PI) {
      mAngleDiff -= Math.PI;
    } else if (mAngleDiff < -Math.PI) {
      mAngleDiff += Math.PI;
    }

    if (mAngleDiff > Math.PI / 2.) {
      mAngleDiff -= Math.PI;
    } else if (mAngleDiff < -Math.PI / 2.) {
      mAngleDiff += Math.PI;
    }
  }

  private void finish() {
    if (mInProgress) {
      mInProgress = false;
      mPointerIds[0] = MotionEvent.INVALID_POINTER_ID;
      mPointerIds[1] = MotionEvent.INVALID_POINTER_ID;
      if (mListener != null) {
        mListener.onRotationEnd(this);
      }

      mAngleDiff = 0;
      mPrevAngle = 0;
    }
  }

  public boolean onTouchEvent(MotionEvent event) {
    switch (event.getActionMasked()) {

      case MotionEvent.ACTION_DOWN:
        mInProgress = false;
        mPointerIds[0] = event.getPointerId(event.getActionIndex());
        mPointerIds[1] = MotionEvent.INVALID_POINTER_ID;
        break;

      case MotionEvent.ACTION_POINTER_DOWN:
        if (!mInProgress) {
          mPointerIds[1] = event.getPointerId(event.getActionIndex());
          mInProgress = true;
          mPrevTime = event.getEventTime();
          mPrevAngle = Double.NaN;
          updateCurrent(event);
          if (mListener != null) {
            mListener.onRotationBegin(this);
          }
        }
        break;

      case MotionEvent.ACTION_MOVE:
        if (mInProgress && mPointerIds[0] != MotionEvent.INVALID_POINTER_ID && mPointerIds[1] != MotionEvent.INVALID_POINTER_ID) {
          updateCurrent(event);
          if (mListener != null && getRotationInDegrees() != 0) {
            mListener.onRotation(this);
          }
        }
        break;

      case MotionEvent.ACTION_POINTER_UP:
        if (mInProgress) {
          int pointerId = event.getPointerId(event.getActionIndex());
          if (pointerId == mPointerIds[0] || pointerId == mPointerIds[1]) {
            // 只要有一根手指抬起，就认为rotation手势结束
            finish();
          }
        }
        break;

      case MotionEvent.ACTION_UP:
        finish();
        break;
    }
    return true;
  }

  public double getRotation() {
    return mAngleDiff;
  }

  public double getRotationInDegrees() {
    return Math.toDegrees(getRotation());
  }

  public long getTimeDelta() {
    return mCurrTime - mPrevTime;
  }

  public float getAnchorX() {
    return mAnchorX;
  }

  public float getAnchorY() {
    return mAnchorY;
  }
}
