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
package cn.org.yxj.cpm.android.bindingx.core;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

public class WeakRunnable implements Runnable {
    private final WeakReference<Runnable> mDelegateRunnable;

    public WeakRunnable(@NonNull Runnable runnable) {
        mDelegateRunnable = new WeakReference<>(runnable);
    }

    @Override
    public void run() {
        Runnable runnable = mDelegateRunnable.get();
        if (runnable != null) {
            try {
                runnable.run();
            }catch (Throwable e) {
                LogProxy.e(e.getMessage());
            }
        }
    }
}
