/*
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
package org.apache.weex;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.zxing.integration.android.IntentIntegrator;
import org.apache.weex.WXSDKEngine;
import org.apache.weex.WXSDKInstance;
import org.apache.weex.common.WXErrorCode;
import org.apache.weex.utils.WXFileUtils;
import org.apache.weex.utils.WXSoInstallMgrSdk;
import org.apache.weex.commons.AbstractWeexActivity;
import org.apache.weex.update.CheckForUpdateUtil;

public class IndexActivity extends AbstractWeexActivity {

  private static final String TAG = "IndexActivity";
  private static final int CAMERA_PERMISSION_REQUEST_CODE = 0x1;
  private static final int WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 0x2;
  private static final String DEFAULT_IP = "your_current_IP";
  private static String sCurrentIp = DEFAULT_IP; // your_current_IP

  private ProgressBar mProgressBar;
  private TextView mTipView;

  private BroadcastReceiver mReloadReceiver;


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_index);
    setContainer((ViewGroup) findViewById(R.id.index_container));
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getWindow().setFormat(PixelFormat.TRANSLUCENT);

    mProgressBar = (ProgressBar) findViewById(R.id.index_progressBar);
    mTipView = (TextView) findViewById(R.id.index_tip);
    mProgressBar.setVisibility(View.VISIBLE);
    mTipView.setVisibility(View.VISIBLE);


    if (!WXSoInstallMgrSdk.isCPUSupport()) {
      mProgressBar.setVisibility(View.INVISIBLE);
      mTipView.setText(R.string.cpu_not_support_tip);
      return;
    }

    if (TextUtils.equals(sCurrentIp, DEFAULT_IP)) {
      renderPage(WXFileUtils.loadAsset("landing.weex.js", this), getIndexUrl());
//      renderPageByURL("http://dotwe.org/raw/dist/2bbe1860da4669a68595c1aed01d7fd2.bundle.wx");
    } else {
      renderPageByURL(getIndexUrl());
    }


    mReloadReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        createWeexInstance();
        if (TextUtils.equals(sCurrentIp, DEFAULT_IP)) {
          renderPage(WXFileUtils.loadAsset("landing.weex.js", getApplicationContext()), getIndexUrl());
        } else {
          renderPageByURL(getIndexUrl());
        }
        mProgressBar.setVisibility(View.VISIBLE);
      }
    };

    LocalBroadcastManager.getInstance(this).registerReceiver(mReloadReceiver, new IntentFilter(WXSDKEngine.JS_FRAMEWORK_RELOAD));

    CheckForUpdateUtil.checkForUpdate(this);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    if (TextUtils.equals(sCurrentIp, DEFAULT_IP)) {
      getMenuInflater().inflate(R.menu.main_scan, menu);
    } else {
      getMenuInflater().inflate(R.menu.main, menu);
    }
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_refresh:
        if (!TextUtils.equals(sCurrentIp, DEFAULT_IP)) {
          createWeexInstance();
          renderPageByURL(getIndexUrl());
          mProgressBar.setVisibility(View.VISIBLE);
        }
        break;
      case R.id.action_scan:
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
          if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            Toast.makeText(this, "please give me the permission", Toast.LENGTH_SHORT).show();
          } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
          }
        } else {
          new IntentIntegrator(this).setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                  .setBeepEnabled(false).setCaptureActivity(CustomCaptureActivity.class)
                  .initiateScan();
     //     startActivity(new Intent(this, CaptureActivity.class));
        }
        break;
      default:
        break;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        new IntentIntegrator(this).setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                .setBeepEnabled(false).setCaptureActivity(CustomCaptureActivity.class)
                .initiateScan();
      } else {
        Toast.makeText(this, "request camara permission fail!", Toast.LENGTH_SHORT).show();
      }
    } else if (requestCode == WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE
            && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
    }
  }

  @Override
  public void onRenderSuccess(WXSDKInstance wxsdkInstance, int i, int i1) {
    super.onRenderSuccess(wxsdkInstance,i,i1);
    mProgressBar.setVisibility(View.GONE);
    mTipView.setVisibility(View.GONE);
  }

  @Override
  public void onException(WXSDKInstance wxsdkInstance, String s, String s1) {
    super.onException(wxsdkInstance,s,s1);
    mProgressBar.setVisibility(View.GONE);
    mTipView.setVisibility(View.VISIBLE);
    if (TextUtils.equals(s, WXErrorCode.WX_DEGRAD_ERR_NETWORK_BUNDLE_DOWNLOAD_FAILED.getErrorCode())) {
      mTipView.setText(R.string.index_tip);
    } else {
      mTipView.setText("network render error:" + s1);
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    LocalBroadcastManager.getInstance(this).unregisterReceiver(mReloadReceiver);
  }

  @Override
  public void onPause() {
    super.onPause();
//    WXSDKManager.getInstance().takeJSHeapSnapshot("/sdcard/weex/");
  }

  private static String getIndexUrl() {
    return "http://" + sCurrentIp + ":12580/examples/build/index.js";
  }
}

