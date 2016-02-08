package com.remidi.cvmig1516.uploadertest;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Summary extends AppCompatActivity {

     String uploader = "";
     String disease = "";
     String specimen = "";
     String priority = "";
     String remarks = "";
     String region = "";
     String province = "";
     String municipality = "";

     Context context;

     @Override
     protected void onCreate(Bundle savedInstanceState) {

          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_summary);

          context = getApplicationContext();

          Bundle extras = getIntent().getExtras();

          if (extras != null) {
               specimen = extras.getString("Specimen");
               disease = extras.getString("Disease");
               uploader = extras.getString("Uploader");
               priority = extras.getString("Priority");
               remarks = extras.getString("Remarks");
               region = extras.getString("Region");
               province = extras.getString("Province");
               municipality = extras.getString("Municipality");
          }

          String latitude = "14.6381278"; // update to GPS latitude in future
          String longitude = "121.0534326"; // update to GPS longitude in future

          //Timestamp timestamp = new Timestamp(Calendar.getInstance().getTime().getTime());

          Date cal = (Calendar.getInstance()).getTime();
          String date = new SimpleDateFormat("yyyy/MM/dd").format(cal);
          String time = new SimpleDateFormat("HH:mm:ss").format(cal);

          ((TextView)findViewById(R.id.summary_date)).setText(date);
          ((TextView)findViewById(R.id.summary_time)).setText(time);
          ((TextView)findViewById(R.id.summary_latitude)).setText(latitude);
          ((TextView)findViewById(R.id.summary_longitude)).setText(longitude);
          ((TextView)findViewById(R.id.summary_specimen)).setText(specimen);
          ((TextView)findViewById(R.id.summary_disease)).setText(disease);
          ((TextView)findViewById(R.id.summary_priority)).setText(priority);
          ((TextView)findViewById(R.id.summary_remarks)).setText(remarks);
          ((TextView)findViewById(R.id.summary_region)).setText(region);
          ((TextView)findViewById(R.id.summary_province)).setText(province);
          ((TextView)findViewById(R.id.summary_municipality)).setText(municipality);

     }

     public void summaryOK(View view) {

          Toast.makeText(context, "Great!", Toast.LENGTH_SHORT).show();
          Intent intent = new Intent(getApplicationContext(), DiseaseDetails.class);
          startActivity(intent);

     }

}
