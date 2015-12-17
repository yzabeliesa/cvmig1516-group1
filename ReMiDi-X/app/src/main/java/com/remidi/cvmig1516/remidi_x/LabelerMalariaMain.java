package com.remidi.cvmig1516.remidi_x;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class LabelerMalariaMain extends ActionBarActivity {

     Drawable[] sample_images;
     Dialog labeldialog;
     int pos;

     /**
      * Whether or not the system UI should be auto-hidden after
      * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
      */
     private static final boolean AUTO_HIDE = true;

     /**
      * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
      * user interaction before hiding the system UI.
      */
     private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

     /**
      * Some older devices needs a small delay between UI widget updates
      * and a change of the status and navigation bar.
      */
     private static final int UI_ANIMATION_DELAY = 300;

     private View mContentView;
     private View mControlsView;
     private boolean mVisible;

     @Override
     protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);

          setContentView(R.layout.activity_labeler_malaria_main);

          sample_images = initializeImageArray();
          pos = 0;

          mVisible = true;
          mControlsView = findViewById(R.id.fullscreen_content_controls);
          mContentView = findViewById(R.id.fullscreen_content);


          // Set up the user interaction to manually show or hide the system UI.
          mContentView.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                    toggle();
               }
          });

          // Upon interacting with UI controls, delay any scheduled hide()
          // operations to prevent the jarring behavior of controls going away
          // while interacting with the UI.
          findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
          /*
          final RadioGroup rg1 = (RadioGroup)findViewById(R.id.labeler_species);
          for(int i = 0; i < rg1.getChildCount(); i++){
               (rg1.getChildAt(i)).setEnabled(false);
          }

          RadioGroup radioGroup = (RadioGroup)findViewById(R.id.labeler_diagnosis);
          radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
               @Override
               public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId==R.id.labeler_positive) {
                         for(int i = 0; i < rg1.getChildCount(); i++){
                              (rg1.getChildAt(i)).setEnabled(true);
                         }
                    }
                    else {
                         for(int i = 0; i < rg1.getChildCount(); i++){
                              (rg1.getChildAt(i)).setEnabled(false);
                         }
                    }
               }
          });
          */
     }

     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
          super.onPostCreate(savedInstanceState);

          // Trigger the initial hide() shortly after the activity has been
          // created, to briefly hint to the user that UI controls
          // are available.
          delayedHide(100);
     }

     /**
      * Touch listener to use for in-layout UI controls to delay hiding the
      * system UI. This is to prevent the jarring behavior of controls going away
      * while interacting with activity UI.
      */
     private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
          @Override
          public boolean onTouch(View view, MotionEvent motionEvent) {
               if (AUTO_HIDE) {
                    delayedHide(AUTO_HIDE_DELAY_MILLIS);
               }
               return false;
          }
     };

     private void toggle() {
          if (mVisible) {
               hide();
          } else {
               show();
          }
     }

     private void hide() {
          // Hide UI first
          ActionBar actionBar = getSupportActionBar();
          if (actionBar != null) {
               actionBar.hide();
          }
          mControlsView.setVisibility(View.GONE);
          mVisible = false;

          // Schedule a runnable to remove the status and navigation bar after a delay
          mHideHandler.removeCallbacks(mShowPart2Runnable);
          mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
     }

     private final Runnable mHidePart2Runnable = new Runnable() {
          @SuppressLint("InlinedApi")
          @Override
          public void run() {
               // Delayed removal of status and navigation bar

               // Note that some of these constants are new as of API 16 (Jelly Bean)
               // and API 19 (KitKat). It is safe to use them, as they are inlined
               // at compile-time and do nothing on earlier devices.
               mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                       | View.SYSTEM_UI_FLAG_FULLSCREEN
                       | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                       | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                       | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                       | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
          }
     };

     @SuppressLint("InlinedApi")
     private void show() {
          // Show the system bar
          mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                  | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
          mVisible = true;

          // Schedule a runnable to display UI elements after a delay
          mHideHandler.removeCallbacks(mHidePart2Runnable);
          mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
     }

     private final Runnable mShowPart2Runnable = new Runnable() {
          @Override
          public void run() {
               // Delayed display of UI elements
               ActionBar actionBar = getSupportActionBar();
               if (actionBar != null) {
                    actionBar.show();
               }
               mControlsView.setVisibility(View.VISIBLE);
          }
     };

     private final Handler mHideHandler = new Handler();
     private final Runnable mHideRunnable = new Runnable() {
          @Override
          public void run() {
               hide();
          }
     };

     /**
      * Schedules a call to hide() in [delay] milliseconds, canceling any
      * previously scheduled calls.
      */
     private void delayedHide(int delayMillis) {
          mHideHandler.removeCallbacks(mHideRunnable);
          mHideHandler.postDelayed(mHideRunnable, delayMillis);
     }

     public void labelImage(View view) {
          labeldialog = new Dialog(LabelerMalariaMain.this);
          labeldialog.setTitle("Label Image");
          labeldialog.setContentView(R.layout.fragment_labeler_malaria_dialog);
          Button b = (Button)labeldialog.findViewById(R.id.labeler_send_button);
          b.setOnClickListener(new View.OnClickListener() {

               @Override
               public void onClick(View v) {
                    ImageView image = (ImageView)findViewById(R.id.fullscreen_content);
                    pos++;
                    pos%=5;
                    image.setImageDrawable(sample_images[pos]);
                    labeldialog.hide();
               }
          });
          labeldialog.show();
     }

     public Drawable[] initializeImageArray() {
          Drawable[] images = new Drawable[5];

          images[0] = getResources().getDrawable(R.drawable.img0000000_000);
          images[1] = getResources().getDrawable(R.drawable.img0000000_001);
          images[2] = getResources().getDrawable(R.drawable.img0000000_002);
          images[3] = getResources().getDrawable(R.drawable.img0000000_003);
          images[4] = getResources().getDrawable(R.drawable.img0000000_004);

          return images;
     }

     public void createFile() {
          
     }


}
