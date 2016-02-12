package com.remidi.cvmig1516.remidi_x;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;

public class LabelerSettings extends ActionBarActivity implements AdapterView.OnItemSelectedListener {

     Spinner specimen_spinner;
     Spinner disease_spinner;
     String validator = "";

     //public class SpecimenSpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {

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
          setContentView(R.layout.activity_labeler_settings);

          specimen_spinner = (Spinner) findViewById(R.id.specimen_spinner);
          ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, R.array.specimen_array, android.R.layout.simple_spinner_item);
          adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
          specimen_spinner.setAdapter(adapter1);
          specimen_spinner.setOnItemSelectedListener(this);

          disease_spinner = (Spinner) findViewById(R.id.disease_spinner);
          ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.blood_disease_array, android.R.layout.simple_spinner_item);
          adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
          disease_spinner.setAdapter(adapter2);

          validator = "Test";

          Intent myIntent = new Intent(this, LoopService.class);
          getApplicationContext().startService(myIntent);
     }

     public void labelerSettingsOK(View view) {

          Spinner disease_spinner = (Spinner) findViewById(R.id.disease_spinner);
          String disease = disease_spinner.getSelectedItem().toString();

          int disease_num = 1;
          String[] diseases = getResources().getStringArray(R.array.all_diseases);
          for (int i = 0; i<diseases.length; i++) {
               if (diseases[i].equals(disease)) {
                    disease_num = i+1;
                    break;
               }
          }

          String image_directory = getApplicationContext().getFilesDir() + "/disease_" + disease_num;

          File srcFile = new File(image_directory);
          File[] images = srcFile.listFiles();

          while (true) {
               if (images.length > 0) break;
          }

          Intent intent = new Intent(getApplicationContext(), LabelerMalariaMain.class);
          //disease = (disease.toLowerCase()).replace(' ', '_');
          //String address = ((EditText) findViewById(R.id.http_address)).getText().toString();
          intent.putExtra("Disease", disease);
          intent.putExtra("Validator", validator);
          //intent.putExtra("Address", address);
          startActivity(intent);

     }


}
