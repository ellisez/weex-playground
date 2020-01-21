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
package cn.org.yxj.android.bindingx.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

import java.util.Map;

/**
 * this class provides unified interfaces to handle the difference between weex and RN. Plugins such as
 * weex should implement all these interface. You can use {@link Builder} to construct
 * an instance.
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class PlatformManager {

    private IDeviceResolutionTranslator mResolutionTranslator;
    private IViewFinder mViewFinder;
    private IViewUpdater mViewUpdater;

    private PlatformManager() {
    }

    @NonNull
    public IDeviceResolutionTranslator getResolutionTranslator() {
        return mResolutionTranslator;
    }

    @NonNull
    public IViewFinder getViewFinder() {
        return mViewFinder;
    }

    @NonNull
    public IViewUpdater getViewUpdater() {
        return mViewUpdater;
    }

    /**
     * Interface for translate device resolution.
     * */
    public interface IDeviceResolutionTranslator {
        double webToNative(double rawSize, Object... extension);
        double nativeToWeb(double rawSize, Object... extension);
    }

    /**
     * Interface for find {@link View} by reference.
     * */
    public interface IViewFinder {

        @Nullable
        View findViewBy(String ref, Object... extension);
    }

    /**
     * Interface for update {@link View}
     * */
    public interface IViewUpdater {

        /**
         * @param targetView target view that will be updated
         * @param propertyName the property that will be changed by property value
         * @param propertyValue the property value that will changed to
         * @param translator handle device resolution for different platforms
         * @param config additional configuration such as transform origin
         * @param extension extension params. For example, weex instanceId.
         * */
        void synchronouslyUpdateViewOnUIThread(@NonNull View targetView,
                                               @NonNull String propertyName,
                                               @NonNull Object propertyValue,
                                               @NonNull IDeviceResolutionTranslator translator,
                                               @NonNull Map<String, Object> config,
                                               Object... extension);
    }



    /**A helper class to create {@link PlatformManager} */
    public static class Builder {

        private IDeviceResolutionTranslator deviceResolutionTranslator;
        private IViewFinder viewFinder;
        private IViewUpdater viewUpdater;

        public Builder() {}

        public PlatformManager build() {
            PlatformManager factory = new PlatformManager();
            factory.mViewFinder = viewFinder;
            factory.mResolutionTranslator = deviceResolutionTranslator;
            factory.mViewUpdater = viewUpdater;
            return factory;
        }

        public Builder withDeviceResolutionTranslator(@NonNull IDeviceResolutionTranslator translator) {
            this.deviceResolutionTranslator = translator;
            return this;
        }

        public Builder withViewFinder(@NonNull IViewFinder finder) {
            this.viewFinder =  finder;
            return this;
        }

        public Builder withViewUpdater(@NonNull IViewUpdater viewUpdater) {
            this.viewUpdater = viewUpdater;
            return this;
        }
    }


}
