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
import androidx.annotation.Nullable;

import cn.org.yxj.cpm.android.bindingx.core.internal.ExpressionPair;

import java.util.Map;

/**
 * Description:
 *
 * Interface which define when native will callback js. For example, if you want to receive the event
 * user horizontal scroll 100px. You can define an interceptor as follows:
 *
 *
 * interceptors: {
 *    user_horizontal_scroll_100: {
 *     expression: 'x>100'
 *    }
 * }
 *
 * when the condition return true, the JS will receive an event:
 *
 * {
 *   state: 'interceptor',                     // event type
 *   interceptor: 'user_horizontal_scroll_100' // interceptor name
 * }
 *
 * Created by rowandjj(chuyi)<br/>
 */
public interface IEventInterceptor {

    void setInterceptors(@Nullable Map<String, ExpressionPair> params);

    void performInterceptIfNeeded(@NonNull String interceptorName, @NonNull ExpressionPair condition, @NonNull Map<String, Object> scope);
}
