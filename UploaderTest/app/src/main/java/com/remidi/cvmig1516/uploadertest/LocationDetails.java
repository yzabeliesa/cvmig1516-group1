package com.remidi.cvmig1516.uploadertest;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class LocationDetails extends AppCompatActivity {

     Spinner region_spinner;
     Spinner province_spinner;
     Spinner municipality_spinner;
     Context context;

     String specimen = "";
     String disease = "";
     String uploader = "";
     String priority = "";
     String remarks = "";

     @Override
     protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_location_details);

          context = getApplicationContext();

          AdapterView.OnItemSelectedListener region_listener = new AdapterView.OnItemSelectedListener() {
               @Override
               public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    ArrayAdapter<CharSequence> adapter;

                    switch (position) {
                         case 0:
                              adapter = ArrayAdapter.createFromResource(context, R.array.region1_provinces, android.R.layout.simple_spinner_item);
                              break;
                         case 1:
                              adapter = ArrayAdapter.createFromResource(context, R.array.region2_provinces, android.R.layout.simple_spinner_item);
                              break;
                         default:
                              adapter = ArrayAdapter.createFromResource(context, R.array.reprotract_disease_array, android.R.layout.simple_spinner_item);
                              break;
                    }
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    province_spinner.setAdapter(adapter);

               }

               @Override
               public void onNothingSelected(AdapterView<?> parent) {

               }
          };

          AdapterView.OnItemSelectedListener province_listener = new AdapterView.OnItemSelectedListener() {
               @Override
               public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    ArrayAdapter<CharSequence> adapter;

                    switch (position) {
                         case 0:
                              adapter = ArrayAdapter.createFromResource(context, R.array.ilocos_norte_municipalities, android.R.layout.simple_spinner_item);
                              break;
                         case 1:
                              adapter = ArrayAdapter.createFromResource(context, R.array.ilocos_sur_municipalities, android.R.layout.simple_spinner_item);
                              break;
                         default:
                              adapter = ArrayAdapter.createFromResource(context, R.array.reprotract_disease_array, android.R.layout.simple_spinner_item);
                              break;
                    }
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    municipality_spinner.setAdapter(adapter);

               }

               @Override
               public void onNothingSelected(AdapterView<?> parent) {

               }
          };

          region_spinner = (Spinner) findViewById(R.id.region_spinner);
          ArrayAdapter<CharSequence> region_adapter = ArrayAdapter.createFromResource(this, R.array.regions, android.R.layout.simple_spinner_item);
          region_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
          region_spinner.setAdapter(region_adapter);
          region_spinner.setOnItemSelectedListener(region_listener);

          province_spinner = (Spinner) findViewById(R.id.province_spinner);
          ArrayAdapter<CharSequence> province_adapter = ArrayAdapter.createFromResource(this, R.array.region1_provinces, android.R.layout.simple_spinner_item);
          province_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
          province_spinner.setAdapter(province_adapter);
          province_spinner.setOnItemSelectedListener(province_listener);

          municipality_spinner = (Spinner) findViewById(R.id.municipality_spinner);
          ArrayAdapter<CharSequence> municipality_adapter = ArrayAdapter.createFromResource(this, R.array.ilocos_norte_municipalities, android.R.layout.simple_spinner_item);
          municipality_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
          municipality_spinner.setAdapter(municipality_adapter);

          Bundle extras = getIntent().getExtras();

          if (extras != null) {
               specimen = extras.getString("Specimen");
               disease = extras.getString("Disease");
               uploader = extras.getString("Uploader");
               priority = extras.getString("Priority");
               remarks = extras.getString("Remarks");
               /*String address = extras.getString("Address");
               tokenizeAddress(address);*/
          }

     }

     public void locationOK(View view) {

          String region = region_spinner.getSelectedItem().toString();
          String province = province_spinner.getSelectedItem().toString();
          String municipality = municipality_spinner.getSelectedItem().toString();

          Intent intent = new Intent(getApplicationContext(), Summary.class);
          intent.putExtra("Specimen", specimen);
          intent.putExtra("Disease", disease);
          intent.putExtra("Uploader", uploader);
          intent.putExtra("Priority", priority);
          intent.putExtra("Remarks", remarks);
          intent.putExtra("Region", region);
          intent.putExtra("Province", province);
          intent.putExtra("Municipality", municipality);

          startActivity(intent);

     }
}
