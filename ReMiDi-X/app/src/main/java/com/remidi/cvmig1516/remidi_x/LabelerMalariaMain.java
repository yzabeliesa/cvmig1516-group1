package com.remidi.cvmig1516.remidi_x;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */

// File naming convention: img1234567_123

     /*

     #0. Initialize
          #- Get intent data
          #- Load images
          #- Load dialog box contents based on disease
               - Set species RadioGroup dependency on diagnosis RadioGroup
               - Set species contents based on disease
               - Set diagnosis based on disease
     1. Create box
          - Touch listener: Action up & down
          - Get 2 points from Action up & down
          - Create box based on 2 points
               - fetchDialogDataFromPatch(int patchno)
               - createPatch(float x1, float y1, float x2, float y2)
               - Display patch number in middle
          - Create new patch
          - Show label dialog
               - If tap + coors are within patches, show label dialog only (modify)
     2. Show label dialog for patch
          - Show patch number in title
          - Cancel
               - Close label dialog
          - Delete
               - Remove patch from patches
               - Delete patch xml file if exists
               - Reset patch numbers
                    - Reset displayed patch numbers
               - Close label dialog
               - Toast: Patch deleted
          - Save
               #- Save data in patch
               #- Close label dialog
               #- Toast: Patch created/modified
     3. Send patches
          - Create xml files for all patches
          - Zip all xml files
          - Send zip file

      */

public class LabelerMalariaMain extends ActionBarActivity {

     Drawable[] sample_images;
     Dialog labelDialog;
     int current_image = 0;
     int current_patch = 0;
     boolean currently_new = true;
     Context context;

     String disease = "";
     String validator = "";
     XMLFileHandler progress_file;
     ArrayList<Patch> patches = new ArrayList<>();

     /**
      * Whether or not the system UI should be auto-hidden after
      * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
      */
     private static final boolean AUTO_HIDE = true;

     /**
      * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
      * user interaction before hiding the system UI.
      */
     private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

     /**
      * Some older devices needs a small delay between UI widget updates
      * and a change of the status and navigation bar.
      */
     private static final int UI_ANIMATION_DELAY = 300;

     private View mContentView;
     private View mControlsView;
     private boolean mVisible;

     @Override
     protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_labeler_malaria_main);

          context = getApplicationContext();

          // Get intent data
          Bundle extras = getIntent().getExtras();

          if (extras != null) {
               disease = extras.getString("Disease");
               validator = extras.getString("Validator");
          }

          // Create handler for progress file
          progress_file = new XMLFileHandler(context,"progress.txt");
          if (progress_file.readContents() == "") progress_file.write("0");

          // Set Activity title
          this.setTitle("Labeler: " + disease);

          // Load dialog box
          labelDialog = new Dialog(LabelerMalariaMain.this);
          labelDialog.setContentView(R.layout.fragment_labeler_malaria_dialog);

          // Initialize (turn into AsyncTask)
          new Initializer().execute(disease);


          // ----------------------------------------------

          mVisible = true;
          mControlsView = findViewById(R.id.fullscreen_content_controls);
          mContentView = findViewById(R.id.fullscreen_content);


          // Set up the user interaction to manually show or hide the system UI.
          /*mContentView.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                    toggle();
               }
          });

          // Upon interacting with UI controls, delay any scheduled hide()
          // operations to prevent the jarring behavior of controls going away
          // while interacting with the UI.
          findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
          /*
          final RadioGroup rg1 = (RadioGroup)findViewById(R.id.labeler_species);
          for(int i = 0; i < rg1.getChildCount(); i++){
               (rg1.getChildAt(i)).setEnabled(false);
          }

          RadioGroup radioGroup = (RadioGroup)findViewById(R.id.labeler_diagnosis);
          radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
               @Override
               public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId==R.id.labeler_positive) {
                         for(int i = 0; i < rg1.getChildCount(); i++){
                              (rg1.getChildAt(i)).setEnabled(true);
                         }
                    }
                    else {
                         for(int i = 0; i < rg1.getChildCount(); i++){
                              (rg1.getChildAt(i)).setEnabled(false);
                         }
                    }
               }
          });
          */
     }

     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
          super.onPostCreate(savedInstanceState);

          // Trigger the initial hide() shortly after the activity has been
          // created, to briefly hint to the user that UI controls
          // are available.
          //delayedHide(100);
     }

     /**
      * Touch listener to use for in-layout UI controls to delay hiding the
      * system UI. This is to prevent the jarring behavior of controls going away
      * while interacting with activity UI.
      */
     /*
     private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
          @Override
          public boolean onTouch(View view, MotionEvent motionEvent) {
               if (AUTO_HIDE) {
                    delayedHide(AUTO_HIDE_DELAY_MILLIS);
               }
               return false;
          }
     };

     private void toggle() {
          if (mVisible) {
               hide();
          } else {
               show();
          }
     }

     private void hide() {
          // Hide UI first
          ActionBar actionBar = getSupportActionBar();
          if (actionBar != null) {
               actionBar.hide();
          }
          mControlsView.setVisibility(View.GONE);
          mVisible = false;

          // Schedule a runnable to remove the status and navigation bar after a delay
          mHideHandler.removeCallbacks(mShowPart2Runnable);
          mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
     }

     private final Runnable mHidePart2Runnable = new Runnable() {
          @SuppressLint("InlinedApi")
          @Override
          public void run() {
               // Delayed removal of status and navigation bar

               // Note that some of these constants are new as of API 16 (Jelly Bean)
               // and API 19 (KitKat). It is safe to use them, as they are inlined
               // at compile-time and do nothing on earlier devices.
               mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                       | View.SYSTEM_UI_FLAG_FULLSCREEN
                       | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                       | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                       | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                       | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
          }
     };

     @SuppressLint("InlinedApi")
     private void show() {
          // Show the system bar
          mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                  | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
          mVisible = true;

          // Schedule a runnable to display UI elements after a delay
          mHideHandler.removeCallbacks(mHidePart2Runnable);
          mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
     }

     private final Runnable mShowPart2Runnable = new Runnable() {
          @Override
          public void run() {
               // Delayed display of UI elements
               ActionBar actionBar = getSupportActionBar();
               if (actionBar != null) {
                    actionBar.show();
               }
               mControlsView.setVisibility(View.VISIBLE);
          }
     };

     private final Handler mHideHandler = new Handler();
     private final Runnable mHideRunnable = new Runnable() {
          @Override
          public void run() {
               hide();
          }
     };
     */

     /**
      * Schedules a call to hide() in [delay] milliseconds, canceling any
      * previously scheduled calls.
      */
     /*
     private void delayedHide(int delayMillis) {
          mHideHandler.removeCallbacks(mHideRunnable);
          mHideHandler.postDelayed(mHideRunnable, delayMillis);
     }
     */

     public void labelImage(View view) {

          labelDialog = new Dialog(LabelerMalariaMain.this);
          labelDialog.setTitle("Label Image");
          labelDialog.setContentView(R.layout.fragment_labeler_malaria_dialog);
          Button b = (Button)labelDialog.findViewById(R.id.labeler_dialog_save);
          b.setOnClickListener(new View.OnClickListener() {

               @Override
               public void onClick(View v) {
                    ImageView image = (ImageView)findViewById(R.id.fullscreen_content);
                    current_image++;
                    current_image%=5;
                    image.setImageDrawable(sample_images[current_image]);
                    labelDialog.hide();
               }
          });
          labelDialog.show();
          updateProgress();
     }

     public Drawable[] initializeImageArray() {
          Drawable[] images = new Drawable[5];

          images[0] = getResources().getDrawable(R.drawable.img0000000_000);
          images[1] = getResources().getDrawable(R.drawable.img0000000_001);
          images[2] = getResources().getDrawable(R.drawable.img0000000_002);
          images[3] = getResources().getDrawable(R.drawable.img0000000_003);
          images[4] = getResources().getDrawable(R.drawable.img0000000_004);

          return images;
     }

     public void updateProgress() {

          progress_file.write(current_image + "");

     }

     private class Patch extends XMLFileHandler{

          int imgno;
          int patchno;
          float x1, y1, x2, y2;
          String disease;
          String analysis;
          ArrayList<String> species = new ArrayList<>();
          String remarks;

          Patch(int imgno, int patchno, float x1, float y1, float x2, float y2, String disease) {

               super(context, disease + "-img" + String.format("%07d", imgno) + "_" + String.format("%03d", patchno));
               this.imgno = imgno;
               this.patchno = patchno;
               this.x1 = x1;
               this.y1 = y1;
               this.x2 = x2;
               this.y2 = y2;
               this.disease = disease;
               this.analysis = "";
               this.remarks = "";

          }

          public String formatImgno() {
               return String.format("%07d", imgno);
          }
          public String formatPatchno() {
               return String.format("%03d", patchno);
          }

     }


     /*
      ------------------------------------------------------------------------------------------------------------------
                                                            METHODS
      ------------------------------------------------------------------------------------------------------------------
     */

     public void showDialogBox(View view) { // For testing only
          createPatch(1,2,3,4);
          Toast.makeText(context, "New patch!", Toast.LENGTH_SHORT).show();
          labelDialog.setTitle("Patch " + current_patch + 1);
          /*Button b = (Button)labelDialog.findViewById(R.id.labeler_dialog_save);
          b.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                    fetchPatchDataFromDialog();
                    labelDialog.hide();
               }
          });*/
          labelDialog.show();
     }

     public void initialize(String disease) {
          // Load progress file data
          current_image = Integer.parseInt(progress_file.readContents());

          // Initialize images
          sample_images = initializeImageArray();

          final RadioButton positive = (RadioButton)labelDialog.findViewById(R.id.labeler_positive);
          final RadioButton negative = (RadioButton)labelDialog.findViewById(R.id.labeler_negative);
          positive.setText(disease + " Infected");
          negative.setText("Not " + disease + " Infected");

          String[] species;

          switch (disease) {
               case "Malaria":
                    species = getResources().getStringArray(R.array.malaria_species);
                    break;
               case "Filariasis":
                    species = getResources().getStringArray(R.array.filariasis_species);
                    break;
               case "Ascariasis":
                    species = getResources().getStringArray(R.array.ascariasis_species);
                    break;
               case "Trichuriasis":
                    species = getResources().getStringArray(R.array.trichuriasis_species);
                    break;
               case "Hookworm infection":
                    species = getResources().getStringArray(R.array.hookworm_species);
                    break;
               case "Schistosomiasis":
                    species = getResources().getStringArray(R.array.schistosomiasis_species);
                    break;
               case "Taeniasis":
                    species = getResources().getStringArray(R.array.taeniasis_species);
                    break;
               case "Heterophyidiasis":
                    species = getResources().getStringArray(R.array.heterophyidiasis_species);
                    break;
               case "Paragonimiasis":
                    species = getResources().getStringArray(R.array.paragonimiasis_species);
                    break;
               case "Echinostomiasis":
                    species = getResources().getStringArray(R.array.echinostomiasis_species);
                    break;
               case "Capillariasis":
                    species = getResources().getStringArray(R.array.capillariasis_species);
                    break;
               case "Giardiasis":
                    species = getResources().getStringArray(R.array.giardiasis_species);
                    break;
               case "Tuberculosis":
                    species = getResources().getStringArray(R.array.tuberculosis_species);
                    break;
               case "Leprosy":
                    species = getResources().getStringArray(R.array.leprosy_species);
                    break;
               case "Gonorrhea":
                    species = getResources().getStringArray(R.array.gonorrhea_species);
                    break;
               case "Bacterial Vaginosis":
                    species = getResources().getStringArray(R.array.vaginosis_species);
                    break;
               default:
                    species = getResources().getStringArray(R.array.candidiasis_species);
                    break;
          }

          final RadioGroup rg = (RadioGroup)labelDialog.findViewById(R.id.labeler_species);
          rg.removeAllViews();

          for (int i = 0; i<species.length; i++) {
               CheckBox cb = new CheckBox(context);
               cb.setText(species[i]);
               cb.setTextColor(getResources().getColor(R.color.black_overlay));
               cb.setEnabled(false);
               rg.addView(cb);
          }

          positive.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                    if (positive.isChecked()) {
                         for (int i = 0; i < rg.getChildCount(); i++) {
                              rg.getChildAt(i).setEnabled(true);
                         }
                    }
               }
          });

          negative.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                    if (negative.isChecked()) {
                         for (int i = 0; i < rg.getChildCount(); i++) {
                              rg.getChildAt(i).setEnabled(false);
                         }
                    }
               }
          });

          labelDialog.findViewById(R.id.labeler_dialog_cancel).setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                    labelDialog.hide();
               }
          });


          labelDialog.findViewById(R.id.labeler_dialog_save).setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                    fetchPatchDataFromDialog();
                    labelDialog.hide();
               }
          });

          labelDialog.findViewById(R.id.labeler_dialog_delete).setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                    deletePatch(v);
                    labelDialog.hide();
               }
          });


     }

     public void fetchPatchDataFromDialog() {

          Toast.makeText(context, "Fetch", Toast.LENGTH_SHORT).show();
          int patchno = current_patch;
          Patch patch = patches.get(patchno);

          if (((RadioButton)labelDialog.findViewById(R.id.labeler_positive)).isChecked())
               patch.analysis = "Infected";
          else patch.analysis = "Not infected";

          RadioGroup rg = (RadioGroup)labelDialog.findViewById(R.id.labeler_species);
          for (int i = 0; i<rg.getChildCount(); i++) {
               CheckBox cb = (CheckBox)rg.getChildAt(i);
               if (cb.isChecked()) {
                    patch.species.add(cb.getText().toString());
               }
          }

          patch.remarks = ((EditText)labelDialog.findViewById(R.id.labeler_comments)).getText().toString();

          if (currently_new) Toast.makeText(context, "Patch created!", Toast.LENGTH_SHORT).show();
          else Toast.makeText(context, "Patch modified!", Toast.LENGTH_SHORT).show();

     }

     public void deletePatch(View view) {
          Toast.makeText(context, "Delete", Toast.LENGTH_SHORT).show();
          if (patches.size() != 0) {
               int patchno = current_patch;
               patches.remove(patchno);
               for (int i = 0; i < patches.size(); i++) {
                    patches.get(i).patchno = i;
               }
          }
          // reset displayed patch number
     }

     public void createPatch(float x1, float y1, float x2, float y2) {
          final int patchno = patches.size();
          Patch patch = new Patch(current_image, patchno, x1, y1, x2, y2, disease);
          patches.add(patch);
          currently_new = true;
          current_patch = patchno;
     }

     public void createPatchXML(int patchno) {

          Patch patch = patches.get(patchno);

          patch.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>");

          patch.append("\n\n<validation>");
          patch.append("\n\t<patchno>" + patch.patchno +"</patchno>");
          patch.append("\n\t<validator>" + validator + "</validator>");
          patch.append("\n\t<imageno>" + patch.imgno + "</imageno>");

          patch.append("\n\n\t<ulcoordinate>");
          patch.append("\n\t\t<x>" + patch.x1 + "</x>");
          patch.append("\n\t\t<y>" + patch.y1 + "</y>");
          patch.append("\n\t</ulcoordinate>");

          patch.append("\n\n\t<lrcoordinate>");
          patch.append("\n\t\t<x>" + patch.x2 + "</x>");
          patch.append("\n\t\t<y>" + patch.y2 + "</y>");
          patch.append("\n\t</lrcoordinate>");

          patch.append("\n\n\t<disease>" + patch.disease + "</disease>");
          patch.append("\n\t<diagnosis>");
          patch.append("\n\t\t<analysis>");
          for (int i = 0; i<patch.species.size(); i++) {
               patch.append("\n\t\t\t<item>" + patch.species.get(i) + "</item>");
          }
          patch.append("\n\t\t</analysis>");
          patch.append("\n\t\t<remarks>" + patch.remarks + "</remarks>");
          patch.append("\n\t</diagnosis>");

          patch.append("\n\t<timestamp>" + new Timestamp(Calendar.getInstance().getTime().getTime()) + "</timestamp>");
          patch.append("\n</validation>");

          //XMLFileHandler xmlFile = new XMLFileHandler(context,"img" + imgno + "_" + patchno + ".xml");
          //xmlFile.write("<?xml version=\"1.0\" encoding=\"utf-8\">");
          /*
          java.util.Date date= new java.util.Date();
	 System.out.println(new Timestamp(date.getTime()));
	 new Timestamp(new Date().getTime());
	 Date date = Timestamp(Calendar.getInstance().getTime().getTime());

           */

     }



     /*
      ------------------------------------------------------------------------------------------------------------------
                                                           ASYNCTASK
      ------------------------------------------------------------------------------------------------------------------
     */

     private class Initializer extends AsyncTask<String, Void, Boolean> {

          @Override
          protected Boolean doInBackground(String... params) {
               initialize(params[0]);
               return true;
          }

          @Override
          protected void onPostExecute(Boolean result) {
          }

          @Override
          protected void onPreExecute() {
          }

     }


}
