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
package cn.org.yxj.cpm.android.bindingx.plugin;

import android.content.Context;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import android.text.Layout;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import cn.org.yxj.cpm.android.bindingx.core.BindingXCore;
import cn.org.yxj.cpm.android.bindingx.core.BindingXEventType;
import cn.org.yxj.cpm.android.bindingx.core.IEventHandler;
import cn.org.yxj.cpm.android.bindingx.core.LogProxy;
import cn.org.yxj.cpm.android.bindingx.core.PlatformManager;
import cn.org.yxj.cpm.android.bindingx.core.internal.BindingXConstants;
import cn.org.yxj.cpm.android.bindingx.core.internal.Utils;
import org.apache.weex.WXSDKEngine;
import org.apache.weex.WXSDKInstance;
import org.apache.weex.annotation.JSMethod;
import org.apache.weex.bridge.JSCallback;
import org.apache.weex.bridge.WXBridgeManager;
import org.apache.weex.dom.CSSShorthand;
import org.apache.weex.ui.component.WXComponent;
import org.apache.weex.ui.component.WXText;
import org.apache.weex.ui.view.WXTextView;
import org.apache.weex.ui.view.border.BorderDrawable;
import org.apache.weex.utils.WXViewUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Description:
 *
 * BindingX API
 *
 * Created by rowandjj(chuyi)<br/>
 */
//@WeexModule(name = "bindingx")
public class WXBindingXModule extends WXSDKEngine.DestroyableModule {

    private BindingXCore mBindingXCore;
    private PlatformManager mPlatformManager;

    public WXBindingXModule(){}

    @VisibleForTesting
    /*package*/ WXBindingXModule(BindingXCore core) {
        this.mBindingXCore = core;
    }

    private void prepareInternal() {
        if(mPlatformManager == null) {
            mPlatformManager = createPlatformManager(mWXSDKInstance);
        }
        if (mBindingXCore == null) {
            mBindingXCore = new BindingXCore(mPlatformManager);

            mBindingXCore.registerEventHandler(BindingXEventType.TYPE_SCROLL,
                    new BindingXCore.ObjectCreator<IEventHandler, Context, PlatformManager>() {
                        @Override
                        public IEventHandler createWith(@NonNull Context context,@NonNull PlatformManager manager, Object... extension) {
                            return new BindingXScrollHandler(context, manager, extension);
                        }
                    });

            mBindingXCore.registerEventHandler(BindingXEventType.TYPE_PAN,
                    new BindingXCore.ObjectCreator<IEventHandler, Context, PlatformManager>() {
                @Override
                public IEventHandler createWith(@NonNull Context context, @NonNull PlatformManager manager, Object... extension) {
                    return new BindingXPanHandlerCompat(context, manager, extension);
                }
            });

            mBindingXCore.registerEventHandler(BindingXEventType.TYPE_PINCH,
                    new BindingXCore.ObjectCreator<IEventHandler, Context, PlatformManager>() {
                @Override
                public IEventHandler createWith(@NonNull Context context, @NonNull PlatformManager manager, Object... extension) {
                    return new BindingXPinchHandlerCompat(context, manager, extension);
                }
            });

            mBindingXCore.registerEventHandler(BindingXEventType.TYPE_ROTATION,
                    new BindingXCore.ObjectCreator<IEventHandler, Context, PlatformManager>() {
                @Override
                public IEventHandler createWith(@NonNull Context context, @NonNull PlatformManager manager, Object... extension) {
                    return new BindingXRotationHandlerCompat(context, manager, extension);
                }
            });
        }
    }

    @JSMethod(uiThread = false)
    public void prepare(Map<String, Object> params) {
        prepareInternal();
    }

    @JSMethod(uiThread = false)
    public Map<String, String> bind(Map<String, Object> params, final JSCallback callback) {
        prepareInternal();
        String token = mBindingXCore.doBind(
                mWXSDKInstance == null ? null : mWXSDKInstance.getContext(),
                mWXSDKInstance == null ? null : mWXSDKInstance.getInstanceId(),
                params == null ? Collections.<String, Object>emptyMap() : params,
                new BindingXCore.JavaScriptCallback() {
                    @Override
                    public void callback(Object params) {
                        if (callback != null) {
                            callback.invokeAndKeepAlive(params);
                        }
                    }
                });
        Map<String, String> result = new HashMap<>(2);
        result.put(BindingXConstants.KEY_TOKEN, token);
        return result;
    }

    @JSMethod(uiThread = false)
    public void bindAsync(Map<String, Object> params, final JSCallback callback, JSCallback tokenCallback) {
        Map<String, String> result = bind(params, callback);
        if(tokenCallback != null && result != null) {
            tokenCallback.invoke(result);
        }
    }

    @JSMethod(uiThread = false)
    public void unbind(Map<String, Object> params) {
        if (mBindingXCore != null) {
            mBindingXCore.doUnbind(params);
        }
    }

    @JSMethod(uiThread = false)
    public void unbindAll() {
        if (mBindingXCore != null) {
            mBindingXCore.doRelease();
        }
    }

    @JSMethod(uiThread = false)
    public List<String> supportFeatures() {
        return Arrays.asList("pan", "orientation", "timing", "scroll", "experimentalGestureFeatures");
    }

    @JSMethod(uiThread = false)
    public void getComputedStyleAsync(@Nullable String ref,@Nullable JSCallback callback) {
        Map<String,Object> result = getComputedStyle(ref);
        if(callback != null) {
            callback.invoke(result);
        }
    }

    @JSMethod(uiThread = false)
    public Map<String, Object> getComputedStyle(@Nullable String ref) {
        prepareInternal();
        PlatformManager.IDeviceResolutionTranslator resolutionTranslator = mPlatformManager.getResolutionTranslator();

        WXComponent component = WXModuleUtils.findComponentByRef(mWXSDKInstance.getInstanceId(), ref);
        if (component == null) {
            return Collections.emptyMap();
        }
        View sourceView = component.getHostView();
        if (sourceView == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> map = new HashMap<>();

        map.put("width", resolutionTranslator.nativeToWeb(component.getLayoutWidth()));
        map.put("height", resolutionTranslator.nativeToWeb(component.getLayoutHeight()));

        map.put("padding-left", resolutionTranslator.nativeToWeb(component.getPadding().get(CSSShorthand.EDGE.LEFT)));
        map.put("padding-top", resolutionTranslator.nativeToWeb(component.getPadding().get(CSSShorthand.EDGE.TOP)));
        map.put("padding-right", resolutionTranslator.nativeToWeb(component.getPadding().get(CSSShorthand.EDGE.RIGHT)));
        map.put("padding-bottom", resolutionTranslator.nativeToWeb(component.getPadding().get(CSSShorthand.EDGE.BOTTOM)));

        map.put("margin-left", resolutionTranslator.nativeToWeb(component.getMargin().get(CSSShorthand.EDGE.LEFT)));
        map.put("margin-top", resolutionTranslator.nativeToWeb(component.getMargin().get(CSSShorthand.EDGE.TOP)));
        map.put("margin-right", resolutionTranslator.nativeToWeb(component.getMargin().get(CSSShorthand.EDGE.RIGHT)));
        map.put("margin-bottom", resolutionTranslator.nativeToWeb(component.getMargin().get(CSSShorthand.EDGE.BOTTOM)));

        map.put("translateX", resolutionTranslator.nativeToWeb(sourceView.getTranslationX()));
        map.put("translateY", resolutionTranslator.nativeToWeb(sourceView.getTranslationY()));

        map.put("rotateX", Utils.normalizeRotation(sourceView.getRotationX()));
        map.put("rotateY", Utils.normalizeRotation(sourceView.getRotationY()));
        map.put("rotateZ", Utils.normalizeRotation(sourceView.getRotation()));

        map.put("scaleX", sourceView.getScaleX());
        map.put("scaleY", sourceView.getScaleY());

        map.put("opacity", sourceView.getAlpha());

        Drawable drawable = sourceView.getBackground();
        double topLeft = 0,topRight = 0,bottomLeft = 0,bottomRight = 0;
        if(drawable != null && drawable instanceof BorderDrawable) {
            BorderDrawable borderDrawable = (BorderDrawable) drawable;
            float[] result = borderDrawable.getBorderRadius(new RectF(0,0,sourceView.getWidth(),sourceView.getHeight()));
            if(result.length == 8) {
                topLeft = result[0];
                topRight = result[2];
                bottomLeft = result[6];
                bottomRight = result[4];
            }
        }
        map.put("border-top-left-radius", resolutionTranslator.nativeToWeb(topLeft));
        map.put("border-top-right-radius", resolutionTranslator.nativeToWeb(topRight));
        map.put("border-bottom-left-radius", resolutionTranslator.nativeToWeb(bottomLeft));
        map.put("border-bottom-right-radius", resolutionTranslator.nativeToWeb(bottomRight));

        if (sourceView.getBackground() != null) {
            int backgroundColor = Color.BLACK;
            if (sourceView.getBackground() instanceof ColorDrawable) {
                backgroundColor = ((ColorDrawable) sourceView.getBackground()).getColor();
            } else if (sourceView.getBackground() instanceof BorderDrawable) {
                backgroundColor = ((BorderDrawable) sourceView.getBackground()).getColor();
            }

            double a = Color.alpha(backgroundColor) / 255.0d;
            int r = Color.red(backgroundColor);
            int g = Color.green(backgroundColor);
            int b = Color.blue(backgroundColor);
            map.put("background-color", String.format(Locale.getDefault(), "rgba(%d,%d,%d,%f)", r, g, b, a));
        }

        if (component instanceof WXText && sourceView instanceof WXTextView) {
            Layout layout = ((WXTextView) sourceView).getTextLayout();
            if (layout != null) {
                CharSequence sequence = layout.getText();
                if (sequence != null && sequence instanceof SpannableString) {
                    ForegroundColorSpan[] spans = ((SpannableString) sequence).getSpans(0, sequence.length(), ForegroundColorSpan.class);
                    if (spans != null && spans.length == 1) {
                        int fontColor = spans[0].getForegroundColor();

                        double a = Color.alpha(fontColor) / 255.0d;
                        int r = Color.red(fontColor);
                        int g = Color.green(fontColor);
                        int b = Color.blue(fontColor);
                        map.put("color", String.format(Locale.getDefault(), "rgba(%d,%d,%d,%f)", r, g, b, a));
                    }
                }
            }
        }

        return map;
    }

    @Override
    public void destroy() {
        WXBridgeManager.getInstance().post(new Runnable() {
            @Override
            public void run() {
                if (mBindingXCore != null) {
                    mBindingXCore.doRelease();
                    mBindingXCore = null;
                }
                WXViewUpdateService.clearCallbacks();
            }
        }, null);
    }

    @NonNull
    /*package*/ static PlatformManager createPlatformManager(WXSDKInstance instance) {
        final int viewPort = instance == null ? 750 : instance.getInstanceViewPortWidth();

        return new PlatformManager.Builder()
                .withViewFinder(new PlatformManager.IViewFinder() {
                    @Nullable
                    @Override
                    public View findViewBy(String ref, Object... extension) {
                        if(extension.length <= 0 || !(extension[0] instanceof String)) {
                            return null;
                        }
                        String instanceId = (String) extension[0];
                        return WXModuleUtils.findViewByRef(instanceId, ref);
                    }
                })
                .withViewUpdater(new PlatformManager.IViewUpdater() {

                    @Override
                    public void synchronouslyUpdateViewOnUIThread(@NonNull View targetView,
                                                                  @NonNull String propertyName,
                                                                  @NonNull Object propertyValue,
                                                                  @NonNull PlatformManager.IDeviceResolutionTranslator translator,
                                                                  @NonNull Map<String, Object> config,
                                                                  Object... extension) {
                        if(extension == null
                                || extension.length < 2
                                || !(extension[0] instanceof String)
                                || !(extension[1] instanceof String)) {
                            return;
                        }
                        String ref = (String) extension[0];
                        String instanceId = (String) extension[1];

                        WXComponent targetComponent = WXModuleUtils.findComponentByRef(instanceId, ref);
                        if(targetComponent == null) {
                            LogProxy.e("unexpected error. component not found [ref:"+ref+",instanceId:"+instanceId+"]");
                            return;
                        }
                        WXViewUpdateService.findUpdater(propertyName).update(
                                targetComponent,
                                targetView,
                                propertyValue,
                                translator,
                                config);

                    }
                })
                .withDeviceResolutionTranslator(new PlatformManager.IDeviceResolutionTranslator() {
                    @Override
                    public double webToNative(double rawSize, Object... extension) {
                        return WXViewUtils.getRealPxByWidth((float) rawSize, viewPort);
                    }

                    @Override
                    public double nativeToWeb(double rawSize, Object... extension) {
                        return WXViewUtils.getWebPxByWidth((float) rawSize, viewPort);
                    }
                })
                .build();
    }


    ///////// Lifecycle Callbacks

    @Override
    public void onActivityPause() {
        WXBridgeManager.getInstance().post(new Runnable() {
            @Override
            public void run() {
                if (mBindingXCore != null) {
                    mBindingXCore.onActivityPause();
                }
            }
        }, null);
    }

    @Override
    public void onActivityResume() {
        WXBridgeManager.getInstance().post(new Runnable() {
            @Override
            public void run() {
                if (mBindingXCore != null) {
                    mBindingXCore.onActivityResume();
                }
            }
        }, null);
    }
}
