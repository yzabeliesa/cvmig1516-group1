package com.remidi.cvmig1516.remidi_x;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class ChangePasswordActivity extends ActionBarActivity {

     Context context;

     @Override
     protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_change_password);

          context = getApplicationContext();
          findViewById(R.id.new_password).setVisibility(View.GONE);
          findViewById(R.id.password_button).setVisibility(View.GONE);

          DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                         case DialogInterface.BUTTON_POSITIVE:
                              findViewById(R.id.new_password).setVisibility(View.VISIBLE);
                              findViewById(R.id.password_button).setVisibility(View.VISIBLE);
                              break;
                         case DialogInterface.BUTTON_NEGATIVE:
                              Intent intent = new Intent(getApplicationContext(), MainMenuActivity.class);
                              startActivity(intent);
                              finish();
                              break;
                    }
               }
          };
          AlertDialog.Builder builder = new AlertDialog.Builder(context);
          builder.setMessage("Would you like to change your password?").setPositiveButton("Yes", dialogClickListener)
                  .setNegativeButton("No", dialogClickListener).show();

     }

     public void changePassword(View view) {

          String newPassword = ((EditText)findViewById(R.id.new_password)).getText().toString();
          Toast.makeText(context, "New password: " + newPassword, Toast.LENGTH_SHORT).show(); // test


          // Do server communication & database change shit here


          // Do following once password update is successful:
          DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                         case DialogInterface.BUTTON_NEUTRAL:
                              Intent intent = new Intent(getApplicationContext(), MainMenuActivity.class);
                              startActivity(intent);
                              finish();
                              break;
                    }
               }
          };
          AlertDialog.Builder builder = new AlertDialog.Builder(context);
          builder.setMessage("Reminder: Do not share your password with anyone.").setNeutralButton("OK", dialogClickListener);
     }

}
