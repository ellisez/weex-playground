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
package org.apache.weex.uitest.TC_AHref;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.InstrumentationTestCase;
import android.test.TouchUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import org.apache.weex.R;
import org.apache.weex.WXPageActivity;
import org.apache.weex.WeappJsBaseTestCase;
import org.apache.weex.constants.Constants;
import org.apache.weex.ui.view.WXTextView;
import org.apache.weex.util.ScreenShot;
import org.apache.weex.util.ViewUtil;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by admin on 16/3/23.
 */
public class WeexUiTestCaseTCAHrefUpdate extends ActivityInstrumentationTestCase2<WXPageActivity> {
    public final  String TAG = "TestScript_Guide==";
    public WeappJsBaseTestCase weappApplication;
    public WXPageActivity waTestPageActivity;
    public WXPageActivity waTestPageActivity2;

    public ViewGroup mViewGroup;
    public Application mApplication;
    public Instrumentation mInstrumentation;

    public  ArrayList<View> mCaseListIndexView = new ArrayList<View>();
    private boolean targetComponetNotFound;

    public WeexUiTestCaseTCAHrefUpdate() {
        super(WXPageActivity.class);
    }

    public void setUp() throws Exception{

        Log.e("TestScript_Guide", "setUp  test!!");
        setActivityInitialTouchMode(false);
        weappApplication = new WeappJsBaseTestCase();
        mInstrumentation = getInstrumentation();

        Intent intent = new Intent();
        intent.putExtra("bundleUrl", Constants.BUNDLE_URL);
        launchActivityWithIntent("org.apache.weex", WXPageActivity.class, intent);
        waTestPageActivity = getActivity();
//        waTestPageActivity.getIntent().getData().toString();
        Log.e(TAG, "activity1=" + waTestPageActivity.toString());
        Thread.sleep(3000);

        mViewGroup = (ViewGroup) waTestPageActivity.findViewById(R.id.container);
        setViewGroup(mViewGroup);

        mCaseListIndexView = ViewUtil.findViewWithText(mViewGroup, "TC_");
        setUpToFindComponet("TC_", this);
        Thread.sleep(2000);
    }

//    public void testPreConditions()
//    {
//        assertNotNull(waTestPageActivity);
//        assertNotNull(mViewGroup);
//        assertNotNull(mCaseListIndexView);
//
//    }

    public void testAherf(){
        findTargetComponetIfNotFound("TC_", this);
        for(final View caseView : mCaseListIndexView){
           if (((WXTextView)caseView).getText().toString().equals("TC_A")){
               Log.e(TAG, "TC_AHref find");

               final WXTextView inputView  = (WXTextView)caseView;
                mInstrumentation.runOnMainSync(new Runnable() {
                    @Override
                    public void run() {
                        inputView.requestFocus();
                        inputView.performClick();
                    }
                });

               sleep(2000);

               setActivity(WXPageActivity.wxPageActivityInstance);
               AppCompatActivity activity2 = getActivity();
               Log.e(TAG, "activity2 = " + activity2.toString());

               ViewGroup myGroup = (ViewGroup)(activity2.findViewById(R.id.container));
               Log.e(TAG, myGroup.toString());

               ArrayList<View> inputListView = new ArrayList<View>();
               inputListView = ViewUtil.findViewWithText(myGroup, "TC_A_Update");
               Log.e(TAG, "TC_AHref_Update size== " + inputListView.size());
               sleep(2000);

               if(inputListView.size()!=0){
                   final WXTextView inputTypeView = (WXTextView)inputListView.get(0);

                   mInstrumentation.runOnMainSync(new Runnable() {
                       @Override
                       public void run() {
                           inputTypeView.requestFocus();
                           inputTypeView.performClick();
                           Log.e(TAG, "TC_AHref_Update clcik!");
                       }
                   });

                   sleep(3000);
                   Log.e(TAG, "TC_AHref_Update_01_init snap!");
                   screenShot("TC_AHref_Update_01_init");
                   sleep(3000);
               }

               setActivity(WXPageActivity.wxPageActivityInstance);
               activity2 = getActivity();
               Log.e(TAG, "activity2 = " + activity2.toString());

               myGroup = (ViewGroup)(activity2.findViewById(R.id.container));
               ArrayList<View> inputListView11 = new ArrayList<View>();
               sleep(3000);

               inputListView11 = ViewUtil.getAllChildViews(myGroup);

               String aherfText = "";
               for(View view :inputListView11){
                   if(view instanceof WXTextView){
                       aherfText = ((WXTextView) view).getText().toString();
                       if(aherfText.contains("Change href")){
                           final FrameLayout aView = (FrameLayout)view.getParent();
                           mInstrumentation.runOnMainSync(new Runnable() {
                               @Override
                               public void run() {
                                   aView.requestFocus();
                                   aView.performClick();
                                   Log.e(TAG, "TC_AHref_Update_02_changeHref clcik!");

                               }
                           });

                           sleep(2000);
                           Log.e(TAG, "TC_AHref_Update_02_changeHref snap!");
                           screenShot("TC_AHref_Update_02_changeHref");
                           sleep(2000);
                       }

                       else if(aherfText.contains("Change width and height")){
                           final FrameLayout aView = (FrameLayout)view.getParent();
                           mInstrumentation.runOnMainSync(new Runnable() {
                               @Override
                               public void run() {
                                   aView.requestFocus();
                                   aView.performClick();
                                   Log.e(TAG, "TC_AHref_Update_03_changeWH clcik!");

                               }
                           });

                           sleep(2000);
                           Log.e(TAG, "TC_AHref_Update_03_changeWH snap!");
                           screenShot("TC_AHref_Update_03_changeWH");
                           sleep(2000);
                       }
                   }
               }
           }
           else{
               targetComponetNotFound = true;
           }
        }

    }



    /**
     * get tc list by text
     * @param byText
     * @return
     * @throws InterruptedException
     */
    public ArrayList<View> getTestCaseListViewByText(String byText) throws InterruptedException {
        Log.e("TestScript_Guide", "byText ==" + byText);

        if(TextUtils.isEmpty(byText)){
            return null;
        }
        ArrayList<View> outViews = new ArrayList<View>();

        mViewGroup.findViewsWithText(outViews, byText, View.FIND_VIEWS_WITH_TEXT);

        for (View view :  outViews){
            String viewText = ((WXTextView)view).getText().toString();
            Log.e(TAG, "viewText ==" + viewText);


        }
        return outViews;
    }

    /**
     * findMyCaseByText
     */
    public View findMyCaseByText(String caseText){
        if (mCaseListIndexView.size() == 0) return null;

        WXTextView view = null;
        for(int i=0; i<mCaseListIndexView.size();i++){

            view = (WXTextView)mCaseListIndexView.get(i);

            if (view.getText().toString().toLowerCase().contains(caseText.toLowerCase())){
                return view;
            }

        }
        return view;
    }

    /**
     * sleep
     */
    public void sleep(long time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * snapshot
     */
    public void screenShot(String shotName) {
        try {
            ScreenShot.shoot(WXPageActivity.wxPageActivityInstance, shotName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setViewGroup(ViewGroup viewGroup){
        mViewGroup = viewGroup;
    }

    /**
     *
     */
    public void findTargetComponetIfNotFound(String target, InstrumentationTestCase test){
        if(mCaseListIndexView.size() ==1 || targetComponetNotFound){
            if(((WXTextView)mCaseListIndexView.get(0))
                    .getText()
                    .toString()
                    .equals("TC__Home")){
                sleep(2000);

                TouchUtils.dragQuarterScreenUp(test,WXPageActivity.wxPageActivityInstance );
                mViewGroup = (ViewGroup) WXPageActivity.wxPageActivityInstance.findViewById(R.id.container);
                mCaseListIndexView = ViewUtil.findViewWithText(mViewGroup, target);
            }
            sleep(2000);

            TouchUtils.dragQuarterScreenUp(test, WXPageActivity.wxPageActivityInstance );
            mViewGroup = (ViewGroup) WXPageActivity.wxPageActivityInstance.findViewById(R.id.container);
            mCaseListIndexView = ViewUtil.findViewWithText(mViewGroup, target);
        }
    }

    /**
     *
     */
    public void setUpToFindComponet(String target, InstrumentationTestCase test){
        int max = 60;
        int count = 0;
        while(mCaseListIndexView.size() == 0){

            if (count < max){
                TouchUtils.dragQuarterScreenUp(test, WXPageActivity.wxPageActivityInstance );
                mViewGroup = (ViewGroup) WXPageActivity.wxPageActivityInstance.findViewById(R.id.container);
                mCaseListIndexView = ViewUtil.findViewWithText(mViewGroup, target);
                count ++;
            }
            else{
                targetComponetNotFound = true;
            }

        }
    }
}
