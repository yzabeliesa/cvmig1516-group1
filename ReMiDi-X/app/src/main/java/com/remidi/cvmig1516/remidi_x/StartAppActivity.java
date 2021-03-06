package com.remidi.cvmig1516.remidi_x;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ProgressBar;

import java.io.File;

public class StartAppActivity extends ActionBarActivity {

     final int HALF_SECONDS = 6; // 3 seconds

     @Override
     protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_start_app);

          final Context context = getApplicationContext();

          final ProgressBar progressBar = (ProgressBar)findViewById(R.id.startProgressBar);
          progressBar.setMax(100);

          final File file = new File(context.getFilesDir() + "/disease-count.txt");

          Thread thread = new Thread() {
               public void run() {
                    try {
                         int timeElapsed = 0;
                         while (timeElapsed <= HALF_SECONDS) {
                              sleep(500);
                              progressBar.setProgress(timeElapsed*(100/(HALF_SECONDS)));
                              timeElapsed+=1;
                         }
                         sleep(500);
                         if (file.exists()) {
                              Intent intent = new Intent(context, UserProfileActivity.class);
                              startActivity(intent);
                         }
                         else {
                              Intent intent = new Intent(context, LoginActivity.class);
                              startActivity(intent);
                         }
                         finish();

                    } catch (Exception e) {
                         e.printStackTrace();
                    }
               }
          };
          thread.start();

     }

}