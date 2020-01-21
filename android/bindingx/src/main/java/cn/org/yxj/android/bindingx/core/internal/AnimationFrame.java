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

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.NonNull;
import android.view.Choreographer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Description:
 *
 * Coordinates the timing of animations
 *
 * Created by rowandjj(chuyi)<br/>
 */

abstract class AnimationFrame {

    static AnimationFrame newInstance() {
        AnimationFrame frame = null;
        if(Build.VERSION.SDK_INT >= 16) {
            frame = new ChoreographerAnimationFrameImpl();
        } else {
            frame = new HandlerAnimationFrameImpl();
        }
        return frame;
    }

    abstract void clear();

    abstract void terminate();

    abstract void requestAnimationFrame(@NonNull Callback callback);

    interface Callback {
        void doFrame();
    }

    @TargetApi(16)
    private static class ChoreographerAnimationFrameImpl extends AnimationFrame implements Choreographer.FrameCallback{

        private Choreographer choreographer;
        private Callback callback;
        private boolean isRunning;

        @TargetApi(16)
        ChoreographerAnimationFrameImpl() {
            if(Looper.myLooper() != Looper.getMainLooper()) {
                final CountDownLatch latch = new CountDownLatch(1);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        // always use main looper to init choreographer
                        // this will make animation more smoothly!
                        choreographer = Choreographer.getInstance();
                        latch.countDown();
                    }
                });
                try {
                    boolean result = latch.await(500, TimeUnit.MILLISECONDS);
                    if(!result) {// if timeout
                        choreographer = Choreographer.getInstance();
                    }
                }catch (InterruptedException e) {
                    // ignore
                }
            } else {
                choreographer = Choreographer.getInstance();
            }
        }

        @Override
        void clear() {
            if(choreographer != null) {
                choreographer.removeFrameCallback(this);
            }
            this.isRunning = false;
        }

        @Override
        void terminate() {
            clear();
            choreographer = null;
        }

        @Override
        void requestAnimationFrame(@NonNull Callback callback) {
            this.callback = callback;
            this.isRunning = true;
            if(choreographer != null) {
                choreographer.postFrameCallback(this);
            }
        }

        @Override
        public void doFrame(long frameTimeNanos) {
            if(callback != null) {
                callback.doFrame();
            }
            if(choreographer != null && isRunning) {
                choreographer.postFrameCallback(this);
            }
        }
    }

    private static class HandlerAnimationFrameImpl extends AnimationFrame implements Handler.Callback{

        private Handler mInnerHandler;

        private Callback callback;
        private boolean isRunning;

        private static final int MSG_FRAME_CALLBACK = 100;
        private static final long DEFAULT_DELAY_MILLIS = 16;

        HandlerAnimationFrameImpl() {
            mInnerHandler = new Handler(Looper.getMainLooper(), this);
        }

        @Override
        void clear() {
            if(mInnerHandler != null) {
                mInnerHandler.removeCallbacksAndMessages(null);
            }
            isRunning = false;
        }

        @Override
        void terminate() {
            clear();
            mInnerHandler = null;
        }

        @Override
        void requestAnimationFrame(@NonNull Callback callback) {
            this.callback = callback;
            this.isRunning = true;
            if(mInnerHandler != null) {
                mInnerHandler.sendEmptyMessage(MSG_FRAME_CALLBACK);
            }
        }

        @Override
        public boolean handleMessage(Message msg) {
            if(msg != null && msg.what == MSG_FRAME_CALLBACK && mInnerHandler != null) {
                if(callback != null) {
                    callback.doFrame();
                }
                if(isRunning) {
                    mInnerHandler.sendEmptyMessageDelayed(MSG_FRAME_CALLBACK, DEFAULT_DELAY_MILLIS);
                }
                return true;
            }
            return false;
        }
    }

}
