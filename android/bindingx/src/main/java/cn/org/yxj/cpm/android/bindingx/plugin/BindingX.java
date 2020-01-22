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

import org.apache.weex.WXSDKEngine;
import org.apache.weex.common.WXException;

/**
 * Description:
 *
 * register bindingx module manually
 *
 * Created by rowandjj(chuyi)<br/>
 */

public class BindingX {
    private BindingX(){}

    /**
     * register binding module
     * */
    public static void register() throws WXException{
        WXSDKEngine.registerModule("expressionBinding", WXExpressionBindingModule.class);
        WXSDKEngine.registerModule("binding", WXBindingXModule.class);
        WXSDKEngine.registerModule("bindingx", WXBindingXModule.class);
    }
}
