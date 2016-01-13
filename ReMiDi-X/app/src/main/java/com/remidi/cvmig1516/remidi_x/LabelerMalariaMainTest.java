package com.remidi.cvmig1516.remidi_x;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Basic Malaria labeler without patching. For demo purposes only.
 */

// File naming convention: img1234567_123

public class LabelerMalariaMainTest extends ActionBarActivity {

     public String HTTP_HOST = "192.168.1.26";
     public String HOME = "/data/";
     public int HTTP_PORT = 5000;

     Drawable[] sample_images;
     Dialog labeldialog;
     int current_image = 0;
     ArrayList patch_coordinates = new ArrayList<int[]>();
     XMLFileHandler progress_file = new XMLFileHandler(getApplicationContext(),"progress.txt");

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

          updateProgress();
          sample_images = initializeImageArray();
          current_image = Integer.parseInt(progress_file.readContents());

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
          findViewById(R.id.labeler_send).setOnTouchListener(mDelayHideTouchListener);
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

          labeldialog = new Dialog(LabelerMalariaMainTest.this);
          labeldialog.setTitle("Label Image");
          labeldialog.setContentView(R.layout.fragment_labeler_malaria_dialog);
          Button b = (Button)labeldialog.findViewById(R.id.labeler_send);
          b.setOnClickListener(new View.OnClickListener() {

               @Override
               public void onClick(View v) {
                    ImageView image = (ImageView) findViewById(R.id.fullscreen_content);
                    String imgno = String.format("%07d", current_image);
                    String patchno = String.format("%03d", 0);
                    XMLFileHandler xmlFile = new XMLFileHandler(getApplicationContext(),"img" + imgno + "_" + patchno + ".xml");
                    createXMLFile(imgno,patchno,xmlFile);
                    sendXMLFile(xmlFile);
                    current_image++;
                    current_image %= 5;
                    image.setImageDrawable(sample_images[current_image]);
                    labeldialog.hide();
                    updateProgress();
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

     public void createXMLFile(String imgno, String patchno, XMLFileHandler xmlFile) {

          xmlFile.write("<?xml version=\"1.0\" encoding=\"utf-8\">\n\n");
          xmlFile.write("\n\n<validation>");
          xmlFile.write("\n\t<filename>" + "img" + imgno + ".png</filename>");

          String diagnosis = "";
          if ((findViewById(R.id.labeler_negative)).isEnabled()) diagnosis = "Not malaria infected";
          else diagnosis = "Malaria infected";
          xmlFile.write("\n\t<diagnosis>" + diagnosis  + "</diagnosis>");
          xmlFile.write("\n\t<species>");

          RadioGroup rgroup = (RadioGroup)findViewById(R.id.labeler_species);
          for (int i = 0 ; i<rgroup.getChildCount(); i++) {
               RadioButton child = (RadioButton)rgroup.getChildAt(i);
               if (child.isEnabled()) xmlFile.write("\n\t\t<s" + (i + 1) + ">" + child.getText() + "</s" + (i + 1) + ">");
          }
          xmlFile.write("\n\t</species>");

          xmlFile.write("\n\t<comments>" + ((TextView)findViewById(R.id.labeler_comments)).getText() + "</comments>");
          xmlFile.write("\n\t<timestamp>" +  (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format((Calendar.getInstance().getTime())) + "</timestamp>");
          xmlFile.write("\n</validation>");

     }

     public void sendXMLFile(XMLFileHandler xmlfh) {

          //URLSERVER -----> "http://" + HTTP_HOST + ":" + HTTP_PORT + HOME
          new Uploader().execute("http://" + HTTP_HOST + ":" + HTTP_PORT + HOME, xmlfh.filename, xmlfh.filepath);

     }

     private class Uploader extends AsyncTask <String, Void, Boolean> {

          @Override
          protected Boolean doInBackground(String... params) {
               String urlServer = params[0];
               String filename = params[1];
               String filepath = params[2];

               String key = "uploaded_file";
               HttpURLConnection conn = null;
               DataOutputStream os = null;

               String lineEnd = "\r\n";
               String twoHyphens = "--";
               String boundary =  "*****";
               int bytesRead, bytesAvailable, bufferSize, bytesUploaded = 0;
               byte[] buffer;
               int maxBufferSize = 1024*1024;

               String uploadname = filename.substring(0,filename.length()-5);

               try {
                    File file = new File(filepath);
                    FileInputStream fis = new FileInputStream(file);
                    URL url = new URL(urlServer);
                    conn = (HttpURLConnection) url.openConnection();

                    // POST settings.
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary="+boundary);
                    conn.setRequestProperty(key, filename);
                    conn.setRequestProperty("Content-Length",String.valueOf(file.length()));

                    os = new DataOutputStream(conn.getOutputStream());
                    os.writeBytes(twoHyphens + boundary + lineEnd);
                    os.writeBytes("Content-Disposition: form-data; name=\"" + key + "\";filename=\"" + uploadname +"\"" + lineEnd);
                    os.writeBytes("Content-Type: " + HttpURLConnection.guessContentTypeFromName(filepath) + lineEnd);
                    os.writeBytes(lineEnd);

                    bytesAvailable = fis.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    bytesRead = fis.read(buffer, 0, bufferSize);
                    bytesUploaded += bytesRead;

                    while (bytesRead > 0)
                    {
                         os.write(buffer, 0, bufferSize);
                         bytesAvailable = fis.available();
                         bufferSize = Math.min(bytesAvailable, maxBufferSize);
                         buffer = new byte[bufferSize];
                         bytesRead = fis.read(buffer, 0, bufferSize);
                         bytesUploaded += bytesRead;
                    }

                    os.writeBytes(lineEnd);
                    os.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    // Responses from the server (code and message)

                    conn.setConnectTimeout(10000); // allow 10 seconds timeout.
                    int rcode = conn.getResponseCode();
                    fis.close();
                    os.flush();
                    os.close();
                    if (rcode != 200) return false;

               }
               catch (Exception ex) {
                    return false;
               }
               return true;
          }

          @Override
          protected void onPostExecute(Boolean result) {
          }

          @Override
          protected void onPreExecute() {
          }
     }

     public void updateProgress() {

          progress_file.write(current_image + "");

     }


}
