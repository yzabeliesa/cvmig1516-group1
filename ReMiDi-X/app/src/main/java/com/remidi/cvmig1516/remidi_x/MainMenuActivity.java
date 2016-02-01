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

     String labeler_name = "some random labeler";
     int labeled_image_count = 5;
     int validated_image_count = 10;
     int message_count = 15;

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

          /*
               GET NAME AND COUNT INFO HERE



          */
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
                    case 0: // Label images
                         intent = new Intent(getApplicationContext(), LabelerSettings.class);
                         startActivity(intent);
                         break;
                    case 1: // Profile
                         intent = new Intent(getApplicationContext(), MainMenuActivity.class);
                         startActivity(intent);
                         break;
                    case 2: // Settings
                         toast = Toast.makeText(getApplicationContext(), "Feature unavailable", Toast.LENGTH_SHORT);
                         toast.show();
                         break;
                    case 3: // Logout
                         toast = Toast.makeText(getApplicationContext(), "Feature unavailable", Toast.LENGTH_SHORT);
                         toast.show();
                         break;
               }

          }
     }

     public void onSectionAttached(int number) {
          String[] drawer_sections = getResources().getStringArray(R.array.drawer_sections);
          mTitle = drawer_sections[number-1];
     }

     public void restoreActionBar() {
          ActionBar actionBar = getSupportActionBar();
          actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
          actionBar.setDisplayShowTitleEnabled(true);
          actionBar.setTitle(mTitle);
     }

     public void viewMessages(View view) {
          Toast.makeText(getApplicationContext(), "Messages!", Toast.LENGTH_SHORT).show();
     }

     public void viewLabels(View view) {
          Toast.makeText(getApplicationContext(), "Labels!", Toast.LENGTH_SHORT).show();
     }

     public void viewValidation(View view) {
          Toast.makeText(getApplicationContext(), "Validation!", Toast.LENGTH_SHORT).show();
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

}





