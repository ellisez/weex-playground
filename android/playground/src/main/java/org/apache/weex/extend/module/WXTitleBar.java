/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.weex.extend.module;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import com.alibaba.fastjson.JSONObject;
import org.apache.weex.R;
import org.apache.weex.annotation.JSMethod;
import org.apache.weex.common.WXModule;
import org.apache.weex.utils.WXResourceUtils;


/**
 * Created by moxun on 12/01/2018.
 */

public class WXTitleBar extends WXModule {
  @JSMethod
  public void setTitle(String title) {
    ActionBar actionBar = getActionBar();
    if (actionBar != null) {
      actionBar.setTitle(String.valueOf(title));
    }
  }

  @JSMethod
  public void setStyle(JSONObject object) {
    String bgColor = object.getString("backgroundColor");
    String color = object.getString("foregroundColor");
    ActionBar actionBar = getActionBar();
    if (actionBar != null) {
      if (bgColor != null) {
        int c = WXResourceUtils.getColor(bgColor);
        actionBar.setBackgroundDrawable(new ColorDrawable(c));
      }

      if (color != null) {
        int c = WXResourceUtils.getColor(color);

        Toolbar toolbar = (Toolbar) ((AppCompatActivity) mWXSDKInstance.getContext()).findViewById(R.id.toolbar);
        if (toolbar != null) {
          toolbar.setTitleTextColor(c);
          toolbar.setSubtitleTextColor(c);

          Drawable upNavigation = toolbar.getNavigationIcon();
          if (null != upNavigation) {
            upNavigation = DrawableCompat.wrap(upNavigation);
            upNavigation = upNavigation.mutate();
            DrawableCompat.setTint(upNavigation, c);
            toolbar.setNavigationIcon(upNavigation);
          }

          Drawable overflowIcon = toolbar.getOverflowIcon();
          if (null != overflowIcon) {
            overflowIcon = DrawableCompat.wrap(overflowIcon);
            overflowIcon = overflowIcon.mutate();
            DrawableCompat.setTint(overflowIcon, c);
            toolbar.setOverflowIcon(overflowIcon);
          }

          Menu menu = toolbar.getMenu();
          if (menu != null && menu.size() > 0) {
            for (int i = 0; i < menu.size(); i++) {
              MenuItem item = menu.getItem(i);
              if (item != null && item.getIcon() != null) {
                Drawable drawable = item.getIcon();
                if (null != drawable) {
                  drawable = DrawableCompat.wrap(drawable);
                  drawable = drawable.mutate();
                  DrawableCompat.setTint(drawable, c);
                  item.setIcon(drawable);
                }
              }
            }
            ((AppCompatActivity) mWXSDKInstance.getContext()).invalidateOptionsMenu();
          }
        }
      }
    }
  }

  @JSMethod
  public void showTitleBar(String isShow) {
    ActionBar actionBar = getActionBar();
    if (actionBar != null) {
      if ("true".equals(isShow) && !actionBar.isShowing()) {
        actionBar.show();
      }

      if ("false".equals(isShow) && actionBar.isShowing()) {
        actionBar.hide();
      }
    }
  }

  private ActionBar getActionBar() {
    if (mWXSDKInstance.getContext() instanceof AppCompatActivity) {
      return ((AppCompatActivity) mWXSDKInstance.getContext()).getSupportActionBar();
    }
    return null;
  }
}
