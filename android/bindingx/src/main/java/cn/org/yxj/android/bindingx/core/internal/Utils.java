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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;

import cn.org.yxj.android.bindingx.core.LogProxy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Utils {

    private Utils(){}

    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        if(object == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> map = new HashMap<>();
        Iterator keys = object.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            map.put(key, fromJson(object.get(key)));
        }
        return map;
    }

    public static List toList(JSONArray array) throws JSONException {
        if(array == null) {
            return Collections.emptyList();
        }
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            list.add(fromJson(array.get(i)));
        }
        return list;
    }

    private static Object fromJson(Object json) throws JSONException {
        if (json == JSONObject.NULL) {
            return null;
        } else if (json instanceof JSONObject) {
            return toMap((JSONObject) json);
        } else if (json instanceof JSONArray) {
            return toList((JSONArray) json);
        } else {
            return json;
        }
    }

    @Nullable
    public static String getStringValue(@NonNull Map<String,Object> params, @NonNull String key) {
        Object value = params.get(key);
        if(value == null) {
            return null;
        }

        if(value instanceof String) {
            return (String) value;
        } else {
            return value.toString();
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getRuntimeProps(@NonNull Map<String,Object> params) {
        Object result = params.get(BindingXConstants.KEY_RUNTIME_PROPS);
        if(result == null) {
            return null;
        }
        try {
            return (List<Map<String,Object>>)result;
        }catch (Exception e) {
            return null;
        }
    }

    /**
     *
     * structure:
     *
     * interceptors: {
     *     interceptor_name: {
     *         expression: {
     *             origin:'',
     *             transformed:''
     *         }
     *     },
     *     ...
     * }
     *
     * for now, each interceptor object include only one k-v . So we simplify it to:
     *
     * interceptors: {
     *     interceptor_name:{
     *         origin:'',
     *         transformed:''
     *     },
     *     ...
     * }
     *
     * */
    @Nullable
    @SuppressWarnings("unchecked")
    public static Map<String,ExpressionPair> getCustomInterceptors(@NonNull Map<String,Object> params) {
        Object value = params.get(BindingXConstants.KEY_INTERCEPTORS);
        if(value == null || !(value instanceof Map)) {
            return null;
        }
        Map<String, ExpressionPair> interceptors = new HashMap<>(8);
        Set<Map.Entry> set =  ((Map)value).entrySet();
        for(Map.Entry entry : set) {
            Object k = entry.getKey();
            Object v = entry.getValue();
            if(k instanceof String && v instanceof Map) {
                //unchecked
                ExpressionPair pair = null;
                try {
                    pair = Utils.getExpressionPair((Map<String, Object>) v, BindingXConstants.KEY_EXPRESSION);
                }catch (Exception e) {// maybe class cast error
                    //ignore
                }
                if(pair != null) {
                    interceptors.put((String) k, pair);
                }
            }
        }
        return interceptors;
    }

    @Nullable
    public static ExpressionPair getExpressionPair(@NonNull Map<String,Object> params, @NonNull String key) {
        Object value = params.get(key);
        if(value == null) {
            return null;
        } else if(value instanceof String) {
            // old fashion
            return ExpressionPair.create(null, (String) value);
        } else if(value instanceof Map) {
            Map map = (Map) value;
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(map);
            }catch (Throwable e) {
                LogProxy.e("unexpected json parse error.", e);
            }
            if(jsonObject == null) {
                return ExpressionPair.create(null, null);
            }
            String origin = jsonObject.optString(BindingXConstants.KEY_ORIGIN,null);
            String transformed = jsonObject.optString(BindingXConstants.KEY_TRANSFORMED,null);
            if(TextUtils.isEmpty(origin) && TextUtils.isEmpty(transformed)) {
                return ExpressionPair.create(null, null);
            } else {
                //new style
                return ExpressionPair.create(origin,transformed);
            }
        } else {
            return null;
        }
    }

    @SafeVarargs
    public static <E> HashSet<E> newHashSet(E... elements) {
        HashSet<E> set = new HashSet<E>(elements.length);
        Collections.addAll(set, elements);
        return set;
    }

    @SafeVarargs
    public static <E> ArrayList<E> newArrayList(E... elements) {
        ArrayList<E> list = new ArrayList<E>(elements.length);
        Collections.addAll(list, elements);
        return list;
    }

    /**
     * parse rotation to (-180, 180]
     * */
    public static float normalizeRotation(float rotation) {
        rotation = rotation % 360;
        float normalizedRotation;
        if(rotation >= 0) {
            if(rotation >= 0 && rotation <= 180) {
                normalizedRotation = rotation;
            } else {
                normalizedRotation = (rotation % 180) - 180;
            }
        } else {
            if(rotation > -180 && rotation < 0) {
                normalizedRotation = rotation;
            } else {
                normalizedRotation = 180 + (rotation % 180);
            }
        }
        return normalizedRotation;
    }

    public static int normalizedPerspectiveValue(@NonNull Context context, int raw) {
        // The following converts the matrix's perspective to a camera distance
        // such that the camera perspective looks the same on Android and iOS
        float scale = context.getApplicationContext().getResources().getDisplayMetrics().density;
        return (int) (scale * raw * 5)/*CAMERA_DISTANCE_NORMALIZATION_MULTIPLIER*/;
    }

    @Nullable
    public static Pair<Float,Float> parseTransformOrigin(@Nullable String value, @NonNull View view) {
        if(TextUtils.isEmpty(value)) {
            return null;
        }
        int firstSpace = value.indexOf(' ');
        if (firstSpace != -1) {
            int i = firstSpace;
            for (; i < value.length(); i++) {
                if (value.charAt(i) != ' ') {
                    break;
                }
            }

            if (i < value.length() && value.charAt(i) != ' ') {
                String x = value.substring(0, firstSpace).trim();
                String y = value.substring(i, value.length()).trim();

                float pivotX,pivotY;
                if("left".equals(x)) {
                    pivotX = 0f;
                } else if("right".equals(x)) {
                    pivotX = view.getWidth();
                } else if("center".equals(x)) {
                    pivotX = view.getWidth()/2;
                } else {
                    pivotX = view.getWidth()/2;
                }

                if("top".equals(y)) {
                    pivotY = 0;
                } else if("bottom".equals(y)) {
                    pivotY = view.getHeight();
                } else if("center".equals(y)) {
                    pivotY = view.getHeight()/2;
                } else {
                    pivotY = view.getHeight()/2;
                }

                return new Pair<>(pivotX,pivotY);
            }
        }
        return null;
    }

    public static double getDoubleValue(Map<String,Object> map, String key, double defaultValue) {
        if(map == null || TextUtils.isEmpty(key)) {
            return defaultValue;
        }
        Object value = map.get(key);
        if(value == null) {
            return defaultValue;
        } else if(value instanceof Double) {
            return (double) value;
        } else if(value instanceof String){
            try {
                return Double.parseDouble((String) value);
            }catch (Throwable e) {
                return defaultValue;
            }
        } else {
            try {
                return Double.valueOf(value.toString());
            }catch (Throwable e) {
                return defaultValue;
            }
        }
    }

    public static double getDoubleValue(Map<String,Object> map, String key) {
        return getDoubleValue(map, key, 0);
    }

    public static boolean getBooleanValue(Map<String,Object> map, String key, boolean defaultValue) {
        if(map == null || TextUtils.isEmpty(key)) {
            return defaultValue;
        }
        Object value = map.get(key);
        if(value == null) {
            return defaultValue;
        } else if(value instanceof Boolean) {
            return (boolean) value;
        } else if(value instanceof String) {
            try {
                return Boolean.parseBoolean((String) value);
            }catch (Throwable e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public static boolean getBooleanValue(Map<String,Object> map, String key) {
        return getBooleanValue(map, key, false);
    }

    @SuppressWarnings("unchecked")
    public static Map<String,Object> getMapValue(Map<String,Object> map, String key) {
        if(map == null || TextUtils.isEmpty(key)) {
            return Collections.emptyMap();
        }
        Object value = map.get(key);
        if(value == null) {
            return Collections.emptyMap();
        } else if(value instanceof Map) {
            return (Map<String, Object>) value;
        } else {
            return Collections.emptyMap();
        }
    }
}
