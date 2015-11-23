package com.remidi.cvmig1516.remidi_x;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class LabelerSettings extends ActionBarActivity implements AdapterView.OnItemSelectedListener {

     Spinner specimen_spinner;
     Spinner disease_spinner;

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
     //}

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
     }


}


