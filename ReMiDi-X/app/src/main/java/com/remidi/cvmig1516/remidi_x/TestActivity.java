package com.remidi.cvmig1516.remidi_x;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class TestActivity extends ActionBarActivity {

     public static final String FILE_NAME = "testfile.xml";
     XMLFileHandler xmlfh = null;

     @Override
     protected void onCreate(Bundle savedInstanceState) {

          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_test);
          xmlfh = new XMLFileHandler(getApplicationContext(), FILE_NAME);
          xmlfh.readContents();

     }

     public void readFile(View view) {
          TextView text = (TextView)findViewById(R.id.file_contents);
          text.setText(xmlfh.readContents());
     }

     public void writeToFile(View view) {
          EditText field = (EditText)findViewById(R.id.test_message);
          String toWrite = field.getText().toString();
          field.setText("");
          xmlfh.append(toWrite);
          readFile(view);
     }



}
