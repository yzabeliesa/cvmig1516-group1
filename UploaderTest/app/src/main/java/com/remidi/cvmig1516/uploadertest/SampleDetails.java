package com.remidi.cvmig1516.uploadertest;

import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class SampleDetails extends AppCompatActivity {

     String specimen = "";
     String disease = "";
     String uploader = "";

     Context context;

     @Override
     protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_sample_details);

          context = getApplicationContext();

          // Get intent data
          Bundle extras = getIntent().getExtras();

          if (extras != null) {
               specimen = extras.getString("Specimen");
               disease = extras.getString("Disease");
               uploader = extras.getString("Uploader");
               /*String address = extras.getString("Address");
               tokenizeAddress(address);*/
          }

     }

     public void infoOK(View view) {

          RadioButton high_rb = (RadioButton)findViewById(R.id.priority_high);
          RadioButton normal_rb = (RadioButton)findViewById(R.id.priority_normal);

          if (high_rb.isChecked() || normal_rb.isChecked()) {

               String priority;
               if (high_rb.isChecked()) priority = "High";
               else priority = "Normal";

               String remarks = ((TextView)findViewById(R.id.remarks_field)).getText().toString();
               if (remarks.equals("")) remarks = "--";

               Intent intent = new Intent(getApplicationContext(), LocationDetails.class);
               intent.putExtra("Specimen", specimen);
               intent.putExtra("Disease", disease);
               intent.putExtra("Uploader", uploader);
               intent.putExtra("Priority", priority);
               intent.putExtra("Remarks", remarks);
               startActivity(intent);

          }
          else Toast.makeText(context, "Priority must be set.", Toast.LENGTH_SHORT).show();

     }

}
