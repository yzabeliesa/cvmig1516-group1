package com.remidi.cvmig1516.remidi_x;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
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

     final int DELAY = 1000;
     final int PATCH_NEUTRAL = 0;
     final int PATCH_COMPLETE = 1;
     final int PATCH_INCOMPLETE = 2;

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

     float initX = 0;
     float initY = 0;
     Bitmap origBitmap;

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

     private ImageView mContentView;
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
          progress_file = new XMLFileHandler(context,"progress.txt", disease, false);
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
          mContentView = (ImageView)findViewById(R.id.fullscreen_content);
          origBitmap = ((BitmapDrawable)mContentView.getDrawable()).getBitmap();

          // ----------------------------------------------

          View.OnTouchListener patchBuilder = new View.OnTouchListener() {
               @Override
               public boolean onTouch(View v, MotionEvent event) {

                    float currentX = event.getX();
                    float currentY = event.getY();

                    // Check if area is patched
                    for (int i = 0; i<patches.size(); i++) {
                         Patch patch = patches.get(i);
                         if (isBetween(currentX,patch.x1,patch.x2) && isBetween(currentY,patch.y1,patch.y2)) {
                              current_patch = i;
                              currently_new = false;
                              showDialogBox();
                              Toast.makeText(context, "Patch existing", Toast.LENGTH_SHORT).show(); //test
                              return true;
                         }
                    }

                    // If area is unpatched, create new patch
                    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                         //Toast.makeText(context, "Start", Toast.LENGTH_SHORT).show(); //test
                         initX = currentX;
                         initY = currentY;
                    }
                    else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                         //Toast.makeText(context, "End", Toast.LENGTH_SHORT).show(); //test
                         float finalX = currentX;
                         float finalY = currentY;
                         if (Math.abs(finalX-initX)>10 && Math.abs(finalY-initY)>10) {
                              //Toast.makeText(context, "Coors 1: " + initX + ", " + initY + "Coors 2: " + finalX + ", " + finalY, Toast.LENGTH_SHORT).show(); //test
                              createPatch(initX, initY, finalX, finalY);
                         }
                    }
                    return true;
               }
          };

          mContentView.setOnTouchListener(patchBuilder);

     }

     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
          super.onPostCreate(savedInstanceState);

     }



     /*
      ------------------------------------------------------------------------------------------------------------------
                                                        PRIVATE CLASSES
      ------------------------------------------------------------------------------------------------------------------
     */

     private class Patch extends XMLFileHandler{

          int imgno;
          int patchno;
          float x1, y1, x2, y2;
          String disease;
          ArrayList<String> analysis = new ArrayList<>();
          String remarks;
          int state;

          Patch(Context context, int imgno, int patchno, float x1, float y1, float x2, float y2, String disease) {

               super(context, "img" + String.format("%07d", imgno) + "_" + String.format("%03d", patchno), disease, true);
               this.imgno = imgno;
               this.patchno = patchno;
               this.x1 = x1;
               this.y1 = y1;
               this.x2 = x2;
               this.y2 = y2;
               this.disease = disease;
               this.remarks = "";
               this.state = PATCH_NEUTRAL;

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
                                                       LOADING METHODS
      ------------------------------------------------------------------------------------------------------------------
     */

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
          current_image++;
          current_image%=5;
          progress_file.write(current_image + "");
     }

     public void initialize(String disease) {
          // Load progress file data
          current_image = Integer.parseInt(progress_file.readContents());

          // Initialize images
          sample_images = initializeImageArray();

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
               rg.addView(cb);
          }

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
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                              switch (which) {
                                   case DialogInterface.BUTTON_POSITIVE:
                                        deletePatch();
                                        break;
                                   case DialogInterface.BUTTON_NEGATIVE:
                                        break;
                              }
                         }
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setMessage("Are you sure you want to permanently delete this patch?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
               }
          });

     }

     public void initializeDialogBox() {

          final RadioGroup rg = (RadioGroup)labelDialog.findViewById(R.id.labeler_species);
          for (int i = 0; i<rg.getChildCount(); i++) {
               ((CheckBox)rg.getChildAt(i)).setChecked(false);
          }

          ((EditText)labelDialog.findViewById(R.id.labeler_comments)).setText("");

     }

     public void loadDialogBox(int patchno) {

          Patch patch = patches.get(patchno);
          final RadioGroup rg = (RadioGroup)labelDialog.findViewById(R.id.labeler_species);
          int j = 0;
          for (int i = 0; i < rg.getChildCount(); i++) {
               CheckBox cb = (CheckBox) rg.getChildAt(i);
               if (j<patch.analysis.size()) {
                    if (patch.analysis.get(j).equals(cb.getText().toString())) {
                         cb.setChecked(true);
                         j++;
                    } else cb.setChecked(false);
               }
          }

          ((EditText)labelDialog.findViewById(R.id.labeler_comments)).setText(patch.remarks);

     }



     /*
      ------------------------------------------------------------------------------------------------------------------
                                                     FUNCTIONALITY METHODS
      ------------------------------------------------------------------------------------------------------------------
     */

     public void showDialogBox() {

          labelDialog.setTitle("Patch " + (current_patch+1));
          if (currently_new) initializeDialogBox();
          else loadDialogBox(current_patch);
          labelDialog.show();

     }

     public void fetchPatchDataFromDialog() {

          int patchno = current_patch;
          Patch patch = patches.get(patchno);

          RadioGroup rg = (RadioGroup)labelDialog.findViewById(R.id.labeler_species);
          for (int i = 0; i<rg.getChildCount(); i++) {
               CheckBox cb = (CheckBox)rg.getChildAt(i);
               if (cb.isChecked()) {
                    patch.analysis.add(cb.getText().toString());
               }
          }

          patch.remarks = ((EditText)labelDialog.findViewById(R.id.labeler_comments)).getText().toString();

          if (currently_new) Toast.makeText(context, "Patch created!", Toast.LENGTH_SHORT).show();
          else Toast.makeText(context, "Patch modified!", Toast.LENGTH_SHORT).show();

     }

     public void deletePatch() {

          if (patches.size() > 0) {
               int patchno = current_patch;
               patches.remove(patchno);
               for (int i = 0; i < patches.size(); i++) {
                    patches.get(i).patchno = i;
               }
               drawBoxes();
               labelDialog.hide();
               Toast.makeText(context, "Patch deleted!", Toast.LENGTH_SHORT).show();

               /*
               final Handler mHideHandler = new Handler();
               final Runnable mHideRunnable = new Runnable() {
                    @Override
                    public void run() {
                         labelDialog.hide();
                         Toast.makeText(context, "Patch deleted!", Toast.LENGTH_SHORT).show();
                    }
               };

               mHideHandler.removeCallbacks(mHideRunnable);
               mHideHandler.postDelayed(mHideRunnable, DELAY);
               */
          }
          else Toast.makeText(context, "There are no patches to delete.", Toast.LENGTH_SHORT).show();

     }

     public void createPatch(float x1, float y1, float x2, float y2) {
          final int patchno = patches.size();
          Patch patch = new Patch(context, current_image, patchno, x1, y1, x2, y2, disease);
          patches.add(patch);
          currently_new = true;
          current_patch = patchno;
          //Toast.makeText(context, "Patch count: " + patches.size(), Toast.LENGTH_SHORT).show(); //test
          drawBoxes();
          showDialogBox();

          /*
          final Handler mHideHandler = new Handler();
          final Runnable mHideRunnable = new Runnable() {
               @Override
               public void run() {
                    showDialogBox();
               }
          };

          mHideHandler.removeCallbacks(mHideRunnable);
          mHideHandler.postDelayed(mHideRunnable, DELAY);
          */

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
          for (int i = 0; i<patch.analysis.size(); i++) {
               patch.append("\n\t\t\t<item>" + patch.analysis.get(i) + "</item>");
          }
          patch.append("\n\t\t</analysis>");
          patch.append("\n\t\t<remarks>" + patch.remarks + "</remarks>");
          patch.append("\n\t</diagnosis>");

          patch.append("\n\t<timestamp>" + new Timestamp(Calendar.getInstance().getTime().getTime()) + "</timestamp>");
          patch.append("\n</validation>");

     }

     public void drawBoxes() {

          ImageView imageView = mContentView;
          Bitmap oldBitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
          Paint paint = new Paint();

          paint.setStrokeWidth(20);
          paint.setStyle(Paint.Style.STROKE);
          paint.setShadowLayer(5, 2, 2, Color.BLACK);
          paint.setTextSize(100);

          //Create a new image bitmap and attach a brand new canvas to it
          Bitmap newBitmap = Bitmap.createBitmap(oldBitmap.getWidth(), oldBitmap.getHeight(), Bitmap.Config.RGB_565);
          Canvas canvas = new Canvas(newBitmap);

          //Draw the image bitmap into the canvas
          canvas.drawBitmap(oldBitmap, 0, 0, null);

          //Draw everything else into the canvas
          for (int i = 0; i<patches.size(); i++) {
               Patch patch = patches.get(i);
               if (patch.state == PATCH_NEUTRAL) paint.setColor(Color.WHITE);
               else if (patch.state == PATCH_COMPLETE) paint.setColor(getResources().getColor(R.color.green));
               else paint.setColor(getResources().getColor(R.color.red));
               canvas.drawRoundRect(new RectF(patch.x1, patch.y1, patch.x2, patch.y2), 10, 10, paint);
               canvas.drawText("" + (i + 1), getMidpoint(patch.x1, patch.x2), getMidpoint(patch.y1,patch.y2), paint);
          }

          //Attach the canvas to the ImageView
          imageView.setImageBitmap(newBitmap);

     }

     public void confirmSendData(View view) {

          // Confirm if send. Return if no, sendData() if yes.
          DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                         case DialogInterface.BUTTON_POSITIVE:
                              sendData();
                              break;
                         case DialogInterface.BUTTON_NEGATIVE:
                              return;
                    }
               }
          };
          AlertDialog.Builder builder = new AlertDialog.Builder(LabelerMalariaMain.this);
          builder.setMessage("Are you sure you want to send the diagnosis?").setPositiveButton("Yes", dialogClickListener)
                  .setNegativeButton("No", dialogClickListener).show();

     }

     public void sendData() {

          // Check if all patches have enough data. Continue if yes, toast and return if no.
          if (patches.size() == 0) {
               Toast.makeText(context, "No patches were created.", Toast.LENGTH_SHORT).show();
               return;
          }

          boolean patchDataComplete = true;
          for (int i = 0; i<patches.size(); i++) {
               Patch patch = patches.get(i);
               if (patch.analysis.size() == 0) {
                    patchDataComplete = false;
                    patch.state = PATCH_INCOMPLETE;
               }
               else patch.state = PATCH_COMPLETE;
          }
          drawBoxes();

          if (!patchDataComplete) {
               Toast.makeText(context, "Some patches have incomplete data.", Toast.LENGTH_SHORT).show();
               return;
          }

          // Create xml files for all patches
          /*
          for (int i = 0; i<patches.size(); i++) {
               createPatchXML(i);
          }
          */

          // Zip all files

          // Send
          uploadZipfile();

          // Update progress
          updateProgress();
     }

     public void uploadZipfile() {
          Toast.makeText(context, "Sent zip lol", Toast.LENGTH_SHORT).show();
     }

     public boolean isBetween(float num, float a, float b) {
          float larger;
          float smaller;
          if (a>b) {
               larger = a;
               smaller = b;
          }
          else {
               larger = b;
               smaller = a;
          }

          if (larger >= num && smaller <= num) return true;
          else return false;

     }

     public float getMidpoint(float a, float b) {
          float smaller;
          if (a<b) smaller = a;
          else smaller = b;

          return smaller + (Math.abs(a-b)/2);
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
