package com.remidi.cvmig1516.remidi_x;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ChangePasswordActivity extends ActionBarActivity {

     Context context;
     File myUserDirectory;
     int VALIDATOR_ID;
     String old_password;

     public String HTTP_HOST = ""; // Retrieved upon start
     public String HTTP_HOME = ""; // Retrieved upon start
     public String HTTP_PORT = ""; // Retrieved upon start


     @Override
     protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_change_password);

          myUserDirectory = new File(getApplicationContext().getFilesDir(), "labelerInfo");
          if( !myUserDirectory.exists() ) {
               myUserDirectory.mkdirs();
          }

          FileHandler fh = new FileHandler(myUserDirectory.getAbsolutePath(), "labeler_id.txt");
          String vi = fh.readContents();
          if(!vi.equals("") && Integer.parseInt(vi) >= 0 ) {
               VALIDATOR_ID = Integer.parseInt(vi);
          }

          FileHandler fh1 = new FileHandler(myUserDirectory.getAbsolutePath(), "labeler_password.txt");
          old_password = fh1.readContents();

          HTTP_HOST = getString(R.string.server_address);
          HTTP_HOME = getString(R.string.api_label);
          HTTP_PORT = "80";

          context = getApplicationContext();
          findViewById(R.id.new_password).setVisibility(View.GONE);
          findViewById(R.id.retype_password).setVisibility(View.GONE);
          findViewById(R.id.password_button).setVisibility(View.GONE);

          DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                         case DialogInterface.BUTTON_POSITIVE:
                              findViewById(R.id.new_password).setVisibility(View.VISIBLE);
                              findViewById(R.id.retype_password).setVisibility(View.VISIBLE);
                              findViewById(R.id.password_button).setVisibility(View.VISIBLE);
                              break;
                         case DialogInterface.BUTTON_NEGATIVE:
                              Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
                              startActivity(intent);
                              finish();
                              break;
                    }
               }
          };
          AlertDialog.Builder builder = new AlertDialog.Builder(ChangePasswordActivity.this);
          builder.setMessage("Would you like to change your password?").setPositiveButton("Yes", dialogClickListener)
                  .setNegativeButton("No", dialogClickListener).show();

     }

     public void changePassword(View view) {

          String newPassword = ((EditText)findViewById(R.id.new_password)).getText().toString();
          String confirmPassword = ((EditText)findViewById(R.id.retype_password)).getText().toString();

          if (!newPassword.equals(confirmPassword)) {
               Toast.makeText(context, "Passwords don't match.", Toast.LENGTH_SHORT).show();
               return;
          }

          //Toast.makeText(context, "New password: " + newPassword, Toast.LENGTH_SHORT).show(); // test


          // Do server communication & database change shit here
          String urlstr = "http://" + HTTP_HOST + ":" + HTTP_PORT + "/labeler/change_password/";
          if( verify_change(old_password, newPassword, urlstr) ) {
               // Do following once password update is successful:
               FileHandler fh2 = new FileHandler(myUserDirectory.getAbsolutePath(), "labeler_password.txt");
               fh2.write("" + newPassword);

               DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                         switch (which) {
                              case DialogInterface.BUTTON_POSITIVE:
                                   Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
                                   startActivity(intent);
                                   finish();
                                   break;
                         }
                    }
               };
               AlertDialog.Builder builder = new AlertDialog.Builder(this);
               builder.setTitle("Reminder");
               builder.setMessage("Do not share your password with anyone.").setPositiveButton("OK", dialogClickListener);
               builder.show();
          }
     }

     public boolean verify_change(String old_password, String new_password, String urlstr) {
          // Create a new HttpClient and Post Header
          HttpClient httpclient = new DefaultHttpClient();
          HttpPost httppost = new HttpPost(urlstr);

          try {
               // Add your data
               List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
               nameValuePairs.add(new BasicNameValuePair("labeler_id", "" + VALIDATOR_ID));
               nameValuePairs.add(new BasicNameValuePair("old_password", old_password));
               nameValuePairs.add(new BasicNameValuePair("new_password", new_password));
               httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

               // Execute HTTP Post Request
               HttpResponse response = httpclient.execute(httppost);
               if(response != null) {
                    int status = response.getStatusLine().getStatusCode();
                    if (status == HttpStatus.SC_OK) {
                         String result = EntityUtils.toString(response.getEntity());
                         int success = Integer.parseInt(result);

                         if ( success == -1 )
                              return false;
                         else
                              return true;
                    }
                    else {
                         return false;
                    }
               }
               else {
                    return false;
               }

          } catch (Exception e) {
               e.printStackTrace();
               //Log.d("OutputStream", e.getLocalizedMessage());
               return false;
          }
     }

}
