package com.remidi.cvmig1516.uploadertest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.io.File;

public class DiseaseDetails extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

     Spinner specimen_spinner;
     Spinner disease_spinner;
     String uploader = "";

     public void onItemSelected(AdapterView<?> parent, View view,
                                int pos, long id) {
          // An item was selected. You can retrieve the selected item using
          //String value = (String)parent.getItemAtPosition(pos);

          ArrayAdapter<CharSequence> adapter;

          switch (pos) {
               case 0:
                    adapter = ArrayAdapter.createFromResource(this, R.array.blood_disease_array, android.R.layout.simple_spinner_item);
                    break;
               case 1:
                    adapter = ArrayAdapter.createFromResource(this, R.array.stool_disease_array, android.R.layout.simple_spinner_item);
                    break;
               case 2:
                    adapter = ArrayAdapter.createFromResource(this, R.array.sputum_disease_array, android.R.layout.simple_spinner_item);
                    break;
               case 3:
                    adapter = ArrayAdapter.createFromResource(this, R.array.skinslit_disease_array, android.R.layout.simple_spinner_item);
                    break;
               default:
                    adapter = ArrayAdapter.createFromResource(this, R.array.reprotract_disease_array, android.R.layout.simple_spinner_item);
                    break;
          }
          adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
          disease_spinner.setAdapter(adapter);
     }

     public void onNothingSelected(AdapterView<?> parent) {
          // Another interface callback
     }

     @Override
     protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_disease_details);

          specimen_spinner = (Spinner) findViewById(R.id.specimen_spinner);
          ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, R.array.specimen_array, android.R.layout.simple_spinner_item);
          adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
          specimen_spinner.setAdapter(adapter1);
          specimen_spinner.setOnItemSelectedListener(this);

          disease_spinner = (Spinner) findViewById(R.id.disease_spinner);
          ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.blood_disease_array, android.R.layout.simple_spinner_item);
          adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
          disease_spinner.setAdapter(adapter2);

          uploader = "Test";

     }

     public void diseaseDetailsOK(View view) {

          Spinner disease_spinner = (Spinner) findViewById(R.id.disease_spinner);
          String disease = disease_spinner.getSelectedItem().toString();

          Spinner specimen_spinner = (Spinner) findViewById(R.id.specimen_spinner);
          String specimen = specimen_spinner.getSelectedItem().toString();

          int disease_num = 1;
          String[] diseases = getResources().getStringArray(R.array.all_diseases);
          for (int i = 0; i<diseases.length; i++) {
               if (diseases[i].equals(disease)) {
                    disease_num = i+1;
                    break;
               }
          }

          Intent intent = new Intent(getApplicationContext(), SampleDetails.class);
          intent.putExtra("Specimen", specimen);
          intent.putExtra("Disease", disease);
          intent.putExtra("Uploader", uploader);
          startActivity(intent);

     }
}
