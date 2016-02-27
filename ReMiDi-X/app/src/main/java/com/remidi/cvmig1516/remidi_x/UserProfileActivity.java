package com.remidi.cvmig1516.remidi_x;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class UserProfileActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

     int DISEASE_COUNT = 18;
     int DISEASE_IMAGE_THRESHOLD = 0;
     int POPULATED_IMAGE_THRESHOLD = 1;
     Activity activity;
     Context context;
     ProgressDialog pd;

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

          context = getApplicationContext();

          pd = new ProgressDialog(this);
          pd.setMessage("Retrieving images");
          pd.setCancelable(false);
          pd.setIndeterminate(true);
          pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);

          pd.setOnShowListener(new DialogInterface.OnShowListener() {
               @Override
               public void onShow(DialogInterface dialogInterface) {
                    Thread thread = new Thread() {
                         public void run() {
                              try {
                                   int timeElapsed = 0;
                                   while (true) {

                                        int populated = 0;
                                        //for (int i = 1; i < DISEASE_COUNT+1; i++) {
                                        for (int i = 1; i < 2; i++) { //test only
                                             String image_directory = getApplicationContext().getFilesDir() + "/disease_" + i;

                                             File srcFile = new File(image_directory);
                                             File[] images = srcFile.listFiles();

                                             if (images.length > DISEASE_IMAGE_THRESHOLD) populated++;
                                        }

                                        // Check if no internet
                                        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                                        NetworkInfo ani = cm.getActiveNetworkInfo();
                                        if ((ani == null || !ani.isConnected()) && timeElapsed == 2) this.interrupt();
                                        else timeElapsed += 2;

                                        sleep(2000);
                                        Log.d("main" + ".pd.Thread", "sleep");

                                        if (populated == POPULATED_IMAGE_THRESHOLD || (timeElapsed >=20 && populated == 0)) this.interrupt(); //TEST ONLY
                                   }
                              } catch (InterruptedException e) {
                                   e.printStackTrace();
                              }
                              pd.cancel();
                         }
                    };
                    thread.start();
               }
          });

          pd.setOnDismissListener(new DialogInterface.OnDismissListener() {
               @Override
               public void onDismiss(DialogInterface dialogInterface) {

                    int populated = 0;
                    for (int i = 1; i < DISEASE_COUNT+1; i++) {
                         String image_directory = getApplicationContext().getFilesDir() + "/disease_" + i;

                         File srcFile = new File(image_directory);
                         File[] images = srcFile.listFiles();

                         if (images.length > 0) populated++;
                    }

                    if (populated >= POPULATED_IMAGE_THRESHOLD) { // Retrieving images
                         Intent intent = new Intent(getApplicationContext(), LabelerSettings.class);
                         startActivity(intent);
                    }
                    else { // Retrieving images failed
                         Toast.makeText(getApplicationContext(), "Retrieving images failed. Check your internet connection.", Toast.LENGTH_LONG).show();
                    }

               }
          });



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
                    case 0: // Label images
                         pd.show();
                         break;
                    case 1: // Profile
                         intent = new Intent(getApplicationContext(), UserProfileActivity.class);
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
          int DISEASE_COUNT = 18;
          String labeler_name = "some random labeler";

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


               // Setup count info
               final LinearLayout root = (LinearLayout) rootView.findViewById(R.id.profile_layout);
               Activity activity = getActivity();

               DiseaseCountFile diseaseCountFile = new DiseaseCountFile(activity);
               ArrayList<Integer> counts = diseaseCountFile.disease_counts;
               String[] diseases = getResources().getStringArray(R.array.all_diseases);

               int totalLabeledCount = 0;

               for (int i = 0; i<DISEASE_COUNT; i++) {

                    LinearLayout layout = new LinearLayout(activity);
                    LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layout.setOrientation(LinearLayout.HORIZONTAL);
                    lParams.gravity = Gravity.CENTER_HORIZONTAL;
                    if (i < DISEASE_COUNT - 1) lParams.setMargins(0, 20, 0, 0);
                    else lParams.setMargins(0, 20, 0, 20);

                    ImageButton imageButton = new ImageButton(activity);
                    imageButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    imageButton.setBackgroundColor(getResources().getColor(R.color.pink));

                    LinearLayout.LayoutParams tv_layout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    tv_layout.setMargins(20,0,0,0);

                    int count = counts.get(i);
                    totalLabeledCount+=count;
                    TextView tv = new TextView(activity);
                    tv.setLayoutParams(tv_layout);
                    tv.setTextColor(getResources().getColor(R.color.black_overlay));
                    tv.setText(count + " " + diseases[i] + " images labeled");
                    tv.setPadding(20,0,0,0);

                    layout.addView(imageButton);
                    layout.addView(tv);
                    root.addView(layout);

               }

               LinearLayout.LayoutParams tv_layout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
               tv_layout.setMargins(0, 10, 0, 0);

               TextView tv = new TextView(activity);
               tv.setLayoutParams(tv_layout);
               tv.setText("TOTAL NUMBER OF IMAGES LABELED: " + totalLabeledCount);
               tv.setTextAppearance(activity, android.R.style.TextAppearance_Medium);
               tv.setTextColor(getResources().getColor(R.color.green));

               root.addView(tv);

               // Get labeler name
               TextView name_tv = (TextView)rootView.findViewById(R.id.profile_name_display);
               // labeler_name = something???
               name_tv.setText(labeler_name);

               return rootView;
          }

          @Override
          public void onAttach(Activity activity) {
               super.onAttach(activity);
               ((UserProfileActivity) activity).onSectionAttached(
                       getArguments().getInt(ARG_SECTION_NUMBER));
          }
     }

}