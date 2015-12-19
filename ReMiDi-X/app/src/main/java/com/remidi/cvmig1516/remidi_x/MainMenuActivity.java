package com.remidi.cvmig1516.remidi_x;

import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainMenuActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

     /**
      * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
      */
     private NavigationDrawerFragment mNavigationDrawerFragment;

     /**
      * Used to store the last screen title. For use in {@link #restoreActionBar()}.
      */
     private CharSequence mTitle;
     int serverReplies = 0;

     @Override
     protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_main_menu);

          mNavigationDrawerFragment = (NavigationDrawerFragment)
                  getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
          mTitle = getTitle();

          // Set up the drawer.
          mNavigationDrawerFragment.setUp(
                  R.id.navigation_drawer,
                  (DrawerLayout) findViewById(R.id.drawer_layout));

          ListView drawer = (ListView) findViewById(R.id.drawer_menu);
          drawer.setOnItemClickListener(new DrawerItemClickListener());
     }

     @Override
     public void onNavigationDrawerItemSelected(int position) {
          // update the main content by replacing fragments
          FragmentManager fragmentManager = getSupportFragmentManager();
          fragmentManager.beginTransaction()
                  .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                  .commit();
     }

     private class DrawerItemClickListener implements ListView.OnItemClickListener {
          @Override
          public void onItemClick(AdapterView parent, View view, int position, long id) {
               //selectItem(position);
               Intent intent = null;
               Toast toast = null;
               switch (position) {
                    case 0:
                         intent = new Intent(getApplicationContext(), MainMenuActivity.class);
                         startActivity(intent);
                         break;
                    case 1:
                         toast = Toast.makeText(getApplicationContext(), "Feature unavailable", Toast.LENGTH_SHORT);
                         toast.show();
                         break;
                    case 2:
                         intent = new Intent(getApplicationContext(), LabelerSettings.class);
                         startActivity(intent);
                         break;
                    case 3:
                         toast = Toast.makeText(getApplicationContext(), "Feature unavailable", Toast.LENGTH_SHORT);
                         toast.show();
                         break;
                    case 4:
                         toast = Toast.makeText(getApplicationContext(), "Feature unavailable", Toast.LENGTH_SHORT);
                         toast.show();
                         break;
               }

          }
     }

     public void onSectionAttached(int number) {
          switch (number) {
               case 1:
                    mTitle = getString(R.string.title_section1);
                    break;
               case 2:
                    mTitle = getString(R.string.title_section2);
                    break;
               case 3:
                    mTitle = getString(R.string.title_section3);
                    break;
               case 4:
                    mTitle = getString(R.string.title_section4);
                    break;
               case 5:
                    mTitle = getString(R.string.title_section5);
                    break;
          }
     }

     public void restoreActionBar() {
          ActionBar actionBar = getSupportActionBar();
          actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
          actionBar.setDisplayShowTitleEnabled(true);
          actionBar.setTitle(mTitle);
     }

     /**
      * A placeholder fragment containing a simple view.
      */
     public static class PlaceholderFragment extends Fragment {
          /**
           * The fragment argument representing the section number for this
           * fragment.
           */
          private static final String ARG_SECTION_NUMBER = "section_number";

          /**
           * Returns a new instance of this fragment for the given section
           * number.
           */
          public static PlaceholderFragment newInstance(int sectionNumber) {
               PlaceholderFragment fragment = new PlaceholderFragment();
               Bundle args = new Bundle();
               args.putInt(ARG_SECTION_NUMBER, sectionNumber);
               fragment.setArguments(args);
               return fragment;
          }

          public PlaceholderFragment() {
          }

          @Override
          public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                   Bundle savedInstanceState) {
               View rootView = inflater.inflate(R.layout.fragment_main_menu, container, false);
               return rootView;
          }

          @Override
          public void onAttach(Activity activity) {
               super.onAttach(activity);
               ((MainMenuActivity) activity).onSectionAttached(
                       getArguments().getInt(ARG_SECTION_NUMBER));
          }
     }

     public void sendMessageToServer(View view) {
          // send message to server

          // String response_msg = new Sender().execute("http://192.168.1.26:5000/data/").toString();
          //String response_msg = communicate(");
          //Log.d("Read attempt: ", "> " + response_msg);

          EditText field = (EditText) findViewById(R.id.message);
          EditText field2 = (EditText) findViewById(R.id.url);
          String message = field.getText().toString();
          String url_text = field2.getText().toString();




          //Toast toast = Toast.makeText(getApplicationContext(), "put ur message here", Toast.LENGTH_SHORT);
          //toast.show();

          field.setText("");
          field2.setText("");

          //receiveMessageFromServer(message);

     }

     public void getMessage(View view) {
          refreshButton();
          new DownloadTask().execute("http://192.168.1.26:5000/data");
     }

     public void refreshButton() {

          TextView text = (TextView) findViewById(R.id.section_label);
          if (this.isConnected()) {
               text.setText("Connected");
               text.setTextColor(getResources().getColor(R.color.green));
          } else {
               text.setText("No connection");
               text.setTextColor(getResources().getColor(R.color.red));
          }

     }

     public void receiveMessageFromServer(String message) {

          refreshButton();
          serverReplies++;
          TextView replyCount = (TextView) findViewById(R.id.section_label);
          TextView replies = (TextView) findViewById(R.id.server_replies);

          replyCount.setText("Server replies: " + serverReplies);
          replies.append(message + "\n");

     }

     //check internet/network connection
     public boolean isConnected() {
          ConnectivityManager connMngr = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
          NetworkInfo ntwrkInfo = connMngr.getActiveNetworkInfo();

          if (ntwrkInfo != null && ntwrkInfo.isConnected())
               return true;
          else
               return false;
     }

     public String communicate(String urlstr) {
          InputStream is = null;
          String result = "";

          try {
               HttpClient hc = new DefaultHttpClient();
               HttpResponse hr = hc.execute(new HttpGet(urlstr));
               is = hr.getEntity().getContent();

               if (is != null) {
                    BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(is));
                    String line = "";

                    while((line = bufferedReader.readLine()) != null)
                         result += line;

                    is.close();
               }
               else {
                    result = "Didn't work!";
               }

          } catch(Exception e) {
               e.printStackTrace();
               Log.d("InputStream", e.getLocalizedMessage());
          }

          return result;
     }

     private class DownloadTask extends AsyncTask<String, Void, String> {
          @Override
          protected String doInBackground(String... urls) {
               return communicate(urls[0]);
          }

          @Override
          protected void onPostExecute(String result) {
               Toast.makeText(getApplicationContext(), "Received!", Toast.LENGTH_LONG).show();
               receiveMessageFromServer(result);
          }
     }

}





