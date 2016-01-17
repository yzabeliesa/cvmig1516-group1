package com.remidi.cvmig1516.remidi_x;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.text.InputFilter;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

     final int DRAW_CLEAR = 0; // erase all
     final int DRAW_CURRENT = 1; // new patch
     final int DRAW_ALL = 2; // delete, patch completion

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

          // Initialize images
          sample_images = initializeImageArray();

          // Load dialog box
          labelDialog = new Dialog(LabelerMalariaMain.this);
          labelDialog.setContentView(R.layout.fragment_labeler_malaria_dialog);

          // Initialize (turn into AsyncTask)
          mVisible = true;
          mControlsView = findViewById(R.id.fullscreen_content_controls);
          mContentView = (ImageView)findViewById(R.id.fullscreen_content);
          new Initializer().execute(disease);

          // ----------------------------------------------

          View.OnTouchListener patchBuilder = new View.OnTouchListener() {
               @Override
               public boolean onTouch(View v, MotionEvent event) {

                    float currentX = event.getX();
                    float currentY = event.getY();

                    // Check if area is patched. If yes, open dialog box.
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
                              new ProgressUpdater().execute();
                         }
                    }
                    return true;
               }
          };

          mContentView.setOnTouchListener(patchBuilder);
          mContentView.setImageDrawable(sample_images[current_image]);
          origBitmap = ((BitmapDrawable)mContentView.getDrawable()).getBitmap();

          //FOR TESTING ONLY
          Patch p = new Patch(context, current_image, patches.size(), 20, 20, 700, 700, disease);
          patches.add(p);

          drawBoxes(DRAW_ALL);

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

     public void initialize(String disease) {

          // Load progress file data
          loadProgressFile();

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

          EditText editText = (EditText)labelDialog.findViewById(R.id.labeler_comments);
          editText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(1000)});

          labelDialog.findViewById(R.id.labeler_dialog_cancel).setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {

                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                              switch (which) {
                                   case DialogInterface.BUTTON_POSITIVE:
                                        Patch patch = patches.get(current_patch);
                                        if (patch.analysis.size() == 0) patch.state = PATCH_INCOMPLETE;
                                        else patch.state = PATCH_COMPLETE;
                                        drawBoxes(DRAW_CURRENT);
                                        labelDialog.hide();
                                        break;
                                   case DialogInterface.BUTTON_NEGATIVE:
                                        break;
                              }
                         }
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setMessage("Any unsaved data will be lost. Are you sure you want to cancel?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
               }
          });


          labelDialog.findViewById(R.id.labeler_dialog_save).setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                    fetchPatchDataFromDialog();
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
          labelDialog.show();

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
          labelDialog.show();

     }

     public void loadNextImage() {
          current_image++;
          current_image%=5; //temp
          mContentView.setImageDrawable(sample_images[current_image]);
          origBitmap = ((BitmapDrawable)mContentView.getDrawable()).getBitmap();
          patches.clear();
          drawBoxes(DRAW_CLEAR);
          new ProgressUpdater().execute();
     }

     public void updateProgress() {
          // updates current image file as well as patches created in image
          /* PROTOCOL:
          current_image$x1|y1|x2|y2|Species (comma-separated)|Remarks$x1|y1|x2|y2|Species (comma-separated)|Remarks
          */
          progress_file.write(current_image + "");
          for (int i = 0; i<patches.size(); i++) {
               Patch patch = patches.get(i);
               progress_file.append("$");
               progress_file.append(patch.x1 + "|" + patch.y1 + "|" + patch.x2 + "|" + patch.y2 + "|");
               for (int j = 0; j<patch.analysis.size(); j++) {
                    if (j>0) progress_file.append(",");
                    progress_file.append(patch.analysis.get(j));
               }
               if (patch.analysis.size() == 0) progress_file.append("###*No data*###");
               progress_file.append("|" + patch.remarks);
               if (patch.remarks.equals("")) progress_file.append("###*No data*###");
          }

     }

     public void loadProgressFile() {

          String contents = progress_file.readContents();
          StringTokenizer tokens = new StringTokenizer(contents, "$");

          current_image = Integer.parseInt(tokens.nextToken());

          while (tokens.hasMoreTokens()) {
               String token = tokens.nextToken();
               StringTokenizer patchData = new StringTokenizer(token, "|");
               float x1 = Float.parseFloat(patchData.nextToken());
               float y1 = Float.parseFloat(patchData.nextToken());
               float x2 = Float.parseFloat(patchData.nextToken());
               float y2 = Float.parseFloat(patchData.nextToken());

               Patch patch = new Patch(context, current_image, patches.size(),x1,y1,x2,y2,disease);
               String analysisToken = patchData.nextToken();
               if (!analysisToken.equals("###*No data*###")) {
                    StringTokenizer analysisData = new StringTokenizer(analysisToken, ",");
                    patch.state = PATCH_COMPLETE;
                    while (analysisData.hasMoreTokens()) {
                         String analysis = analysisData.nextToken();
                         patch.analysis.add(analysis);
                    }
               }
               else patch.state = PATCH_INCOMPLETE;

               String remarks = patchData.nextToken();
               if (!remarks.equals("###*No data*###")) patch.remarks = remarks;

               patches.add(patch);

          }

     }



     /*
      ------------------------------------------------------------------------------------------------------------------
                                                     FUNCTIONALITY METHODS
      ------------------------------------------------------------------------------------------------------------------
     */

     public void showDialogBox() {

          labelDialog.setTitle("Patch " + (current_patch + 1));
          if (currently_new) initializeDialogBox();
          else loadDialogBox(current_patch);

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

          if (patch.analysis.size() == 0) {
               patch.state = PATCH_INCOMPLETE;
               Toast.makeText(context, "Please accomplish patch analysis.", Toast.LENGTH_SHORT).show();
          }
          else {
               patch.remarks = ((EditText)labelDialog.findViewById(R.id.labeler_comments)).getText().toString();
               patch.state = PATCH_COMPLETE;
               drawBoxes(DRAW_CURRENT);
               if (currently_new) Toast.makeText(context, "Patch created!", Toast.LENGTH_SHORT).show();
               else Toast.makeText(context, "Patch modified!", Toast.LENGTH_SHORT).show();
               labelDialog.hide();
               new ProgressUpdater().execute();
          }

     }

     public void createPatch(float x1, float y1, float x2, float y2) {
          final int patchno = patches.size();
          Patch patch = new Patch(context, current_image, patchno, x1, y1, x2, y2, disease);
          patches.add(patch);
          currently_new = true;
          current_patch = patchno;
          drawBoxes(DRAW_CURRENT);
          new ProgressUpdater().execute();
          showDialogBox();
     }

     public void deletePatch() {

          if (patches.size() > 0) {
               int patchno = current_patch;
               patches.remove(patchno);
               for (int i = 0; i < patches.size(); i++) {
                    patches.get(i).patchno = i;
               }
               drawBoxes(DRAW_ALL);
               new ProgressUpdater().execute();
               labelDialog.hide();
               Toast.makeText(context, "Patch deleted!", Toast.LENGTH_SHORT).show();

          }

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

     public void drawBoxes(int state) {

          ImageView imageView = mContentView;

          if (state == DRAW_CLEAR) {
               imageView.setImageBitmap(origBitmap);
               return;
          }

          Bitmap oldBitmap;
          if (state == DRAW_CURRENT) oldBitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
          else oldBitmap = origBitmap;

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
          if (state == DRAW_ALL) {
               for (int i = 0; i < patches.size(); i++) {
                    Patch patch = patches.get(i);
                    if (patch.state == PATCH_NEUTRAL) paint.setColor(Color.WHITE);
                    else if (patch.state == PATCH_COMPLETE)
                         paint.setColor(getResources().getColor(R.color.green));
                    else paint.setColor(getResources().getColor(R.color.red));
                    canvas.drawRoundRect(new RectF(patch.x1, patch.y1, patch.x2, patch.y2), 10, 10, paint);
                    canvas.drawText("" + (i + 1), getMidpoint(patch.x1, patch.x2), getMidpoint(patch.y1, patch.y2), paint);
               }
          }
          else {
               Patch patch = patches.get(current_patch);
               if (patch.state == PATCH_NEUTRAL) paint.setColor(Color.WHITE);
               else if (patch.state == PATCH_COMPLETE)
                    paint.setColor(getResources().getColor(R.color.green));
               else paint.setColor(getResources().getColor(R.color.red));
               canvas.drawRoundRect(new RectF(patch.x1, patch.y1, patch.x2, patch.y2), 10, 10, paint);
               canvas.drawText("" + (patch.patchno + 1), getMidpoint(patch.x1, patch.x2), getMidpoint(patch.y1, patch.y2), paint);
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
               Toast.makeText(context, "Image has no diagnosis.", Toast.LENGTH_SHORT).show();
               return;
          }

          boolean patchDataComplete = true;
          for (int i = 0; i<patches.size(); i++) {
               Patch patch = patches.get(i);
               if (patch.state == PATCH_INCOMPLETE) {
                    patchDataComplete = false;
                    break;
               }
          }

          if (!patchDataComplete) {
               Toast.makeText(context, "Diagnosis is incomplete.", Toast.LENGTH_SHORT).show();
               return;
          }

          // Create xml files for all patches
          for (int i = 0; i<patches.size(); i++) {
               createPatchXML(i);
          }

          // Zip all files
          String imageFolder = progress_file.filefolder;
          String zipPath = progress_file.filefolder + "/img" + patches.get(0).formatImgno() + ".zip";
          createZipfile(imageFolder,zipPath);

          // Send & delete patch data
          uploadZipfile();

          // Load next image
          loadNextImage();
     }

     public void createZipfile(String imageFolder, String zipPath) {

          progress_file.delete();

          try {
               FileOutputStream fos = new FileOutputStream(zipPath);
               ZipOutputStream zos = new ZipOutputStream(fos);
               File srcFile = new File(imageFolder);
               File[] files = srcFile.listFiles();
               for (int i = 0; i < files.length; i++) {
                    String filename = files[i].getPath();
                    String extension = filename.substring(filename.length()-4,filename.length());
                    if (!extension.equals(".zip")) {
                         byte[] buffer = new byte[1024];
                         FileInputStream fis = new FileInputStream(files[i] + "/textData.xml");
                         zos.putNextEntry(new ZipEntry(files[i].getName() + "/textData.xml"));
                         int length;
                         while ((length = fis.read(buffer)) > 0) {
                              zos.write(buffer, 0, length);
                         }
                         zos.closeEntry();
                         fis.close();
                    }
               }
               zos.close();
               Toast.makeText(context, "Zip created!", Toast.LENGTH_SHORT).show(); //test
          } catch (Exception ex) {
               Log.d("",ex.getMessage());
          }

     }

     public void uploadZipfile() {

          // run uploader

          Toast.makeText(context, "Sent image diagnosis!", Toast.LENGTH_SHORT).show();
          for (int i = 0; i<patches.size(); i++) { //deletes patch data
               Patch patch = patches.get(i);
               patch.delete();
               patch.deleteFolder();
          }

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

     private class ProgressUpdater extends AsyncTask<Void, Void, Boolean> {

          @Override
          protected Boolean doInBackground(Void... v) {
               updateProgress();
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
