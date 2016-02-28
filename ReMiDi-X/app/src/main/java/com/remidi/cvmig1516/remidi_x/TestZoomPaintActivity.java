package com.remidi.cvmig1516.remidi_x;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

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

public class TestZoomPaintActivity extends ActionBarActivity {

     final int DELAY = 300;
     final int PATCH_NEUTRAL = 0;
     final int PATCH_COMPLETE = 1;
     final int PATCH_INCOMPLETE = 2;

     final int DRAW_CLEAR = 0; // erase all
     final int DRAW_CURRENT = 1; // new patch
     final int DRAW_ALL = 2; // delete, patch completion

     Drawable[] sample_images;
     Dialog labelDialog;
     int current_image = 0; // Actual image number
     int current_patch = 0;
     boolean currently_new = true;
     Context context;
     Activity activity;

     String disease = "";
     String validator = "";
     FileHandler progress_file;
     ArrayList<Patch> patches = new ArrayList<>();

     float initX = 0;
     float initY = 0;
     float bounds_left = 0;
     float bounds_top = 0;
     float scaleFactor = 1;
     Bitmap origBitmap;
     File myDirectory;

     //private ImageView mContentView;
     private View mControlsView;
     private boolean mVisible;
     int screen_width = 0;

     // Local url
     //public String HTTP_IP_ADDRESS = "192.168.1.10";
     //public String HTTP_HOME = "/data/";

     // Web url
     public String HTTP_IP_ADDRESS = ""; // Retrieved upon start
     public String HTTP_HOME = ""; // Retrieved upon start
     public int HTTP_PORT = 80;

     public boolean isThreadPause = false;

     Uploader uploader;
     Drawable drawable;
     DrawingView mContentView;
     ZoomImageView zoomContentView;
     LinearLayout mDrawingPad;
     int mContentView_top = 0;
     int mContentView_bottom = 0;

     final int MODE_PATCH = 0;
     final int MODE_ZOOM = 1;
     int mode = MODE_PATCH;

     String image_directory = ""; // Directory of where image to be patched are located

     DiseaseCountFile disease_count_file;
     int disease_num = 1;

     @Override
     protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_test_zoom_paint);

          HTTP_IP_ADDRESS = getString(R.string.server_address);
          HTTP_HOME = getString(R.string.api_label);

          context = getApplicationContext();
          myDirectory = new File(context.getFilesDir(), "remidiDatabase");

          if( !myDirectory.exists() ) {
               myDirectory.mkdirs();
          }

          // Get intent data
          /* // UNCOMMENT ME WHEN NO LONGER TESTING
          Bundle extras = getIntent().getExtras();

          if (extras != null) {
               disease = extras.getString("Disease");
               validator = extras.getString("Validator");
          }
          */
          disease = "Malaria"; // HERE IN TESTZOOM ONLY
          validator = "some validator"; // HERE IN TESTZOOM ONLY

          // Load uploader
          String[] diseases = getResources().getStringArray(R.array.all_diseases);
          for (int i = 0; i<diseases.length; i++) {
               if (diseases[i].equals(disease)) {
                    disease_num = i+1;
                    break;
               }
          }

          image_directory = context.getFilesDir() + "/disease_" + disease_num;

          // Set Activity title
          this.setTitle("Test Labeler: " + disease); // HERE IN TESTZOOM ONLY
          // this.setTitle("Labeler: " + disease); // UNCOMMENT ME IN MAIN

          // Initialize images // TEST ONLY
          sample_images = initializeImageArray(); // HERE IN TESTZOOM ONLY

          // Get image file from folder
          /* //UNCOMMENT ME IN MAIN

          File[] images = new File(image_directory).listFiles();

          if (images.length == 0) {
               Intent intent = new Intent(getApplicationContext(), NoImagesActivity.class);
               startActivity(intent);
               progress_file.delete();
               finish();
          }

          File image = images[0]; // Gets first image in image_directory
          current_image = tokenizeImageNum(image);
          Bitmap imageBitmap = BitmapFactory.decodeFile(image.getAbsolutePath());

          */

          current_image = 0; // HERE IN TESTZOOM ONLY
          BitmapDrawable bitmapDrawable = (BitmapDrawable)sample_images[0]; // HERE IN TESTZOOM ONLY
          Bitmap imageBitmap = bitmapDrawable.getBitmap(); // HERE IN TESTZOOM ONLY

          // Create handler for progress file
          progress_file = new FileHandler(context,disease_num + "-progress.txt", disease, false);
          if (progress_file.readContents() == "") progress_file.write(current_image + "");

          // Initialize disease count file
          disease_count_file = new DiseaseCountFile(context);

          // Load dialog box
          labelDialog = new Dialog(TestZoomPaintActivity.this);
          labelDialog.setContentView(R.layout.fragment_labeler_dialog);

          // Initialize uploader
          uploader = new Uploader(context,myDirectory, disease_num, HTTP_IP_ADDRESS, HTTP_PORT, HTTP_HOME);

          // Load custom display image
          Display display = getWindowManager().getDefaultDisplay();
          Point size = new Point();
          display.getSize(size);
          screen_width = size.x;

          mVisible = true;
          mContentView = new DrawingView(this, getScaledImage(imageBitmap,screen_width), mDrawingPad);
          mContentView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
          mContentView.setAdjustViewBounds(true);
          mContentView.setScaleType(ImageView.ScaleType.FIT_CENTER);
          mContentView.invalidate();
          mContentView.setMaxZoom(4f);
          //drawable = mContentView.getDrawable();

          mDrawingPad=(LinearLayout)findViewById(R.id.drawing_pad_test);
          mDrawingPad.addView(mContentView);

          scaleFactor = getScaleFactor(mContentView,screen_width);

          // Initialize UI
          initializeUI(disease);

          /*
          // Initialize (turn into AsyncTask)
          mVisible = true;
          mContentView = new DrawingView(this);
          initializeUI(disease);
          mContentView.setImageBitmap(imageBitmap);
          mContentView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
          mContentView.setAdjustViewBounds(true);
          mContentView.setScaleType(ImageView.ScaleType.FIT_CENTER);
          drawable = mContentView.getDrawable();

          zoomContentView = new ZoomImageView(this, getScaledImage(mContentView,screen_width), mDrawingPad);
          zoomContentView.setMaxZoom(4f);

          mDrawingPad=(LinearLayout)findViewById(R.id.drawing_pad_test);
          mDrawingPad.addView(mContentView);
          mContentView.invalidate();

          final Button button = (Button)findViewById(R.id.button_test);
          final TextView text = (TextView)findViewById(R.id.text_instructions_test);

          View.OnClickListener clickListener = new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                    if (mode == MODE_PATCH) {
                         mode = MODE_ZOOM;
                         // view location in 2
                         mDrawingPad.removeView(mContentView);
                         drawable = mContentView.getDrawable();
                         mDrawingPad.addView(zoomContentView);
                         button.setText("Zoom mode");
                         text.setText(getString(R.string.zoom_instructions));
                    }
                    else {
                         mode = MODE_PATCH;
                         mDrawingPad.removeView(zoomContentView);
                         mDrawingPad.addView(mContentView);
                         button.setText("Patch mode");
                         text.setText(getString(R.string.labeler_instructions));
                    }
               }
          };

          button.setOnClickListener(clickListener);
          */

     }

     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
          super.onPostCreate(savedInstanceState);

          int[] img_coordinates = new int[2];
          mContentView.getLocationOnScreen(img_coordinates);
          bounds_left = img_coordinates[0];
          bounds_top = img_coordinates[1];
          //

          drawable = mContentView.getDrawable();
          mContentView_top = drawable.getBounds().top;
          mContentView_bottom = drawable.getBounds().bottom;
          //Toast.makeText(getApplicationContext(), "Top: "+ mContentView_top + "\nBottom: " + mContentView_bottom, Toast.LENGTH_SHORT).show();

     }

     private Bitmap getScaledImage(Bitmap bitmap, int boundBoxInDp)
     {
          // Get the ImageView and its bitmap
          //Drawable drawing = view.getDrawable();
          //Bitmap bitmap = ((BitmapDrawable)drawing).getBitmap();

          // Get current dimensions
          int width = bitmap.getWidth();
          int height = bitmap.getHeight();

          // Determine how much to scale: the dimension requiring less scaling is
          // closer to the its side. This way the image always stays inside your
          // bounding box AND either x/y axis touches it
          float scale = getScaleFactor(bitmap, boundBoxInDp);

          // Create a matrix for the scaling and add the scaling data
          Matrix matrix = new Matrix();
          matrix.postScale(scale, scale);

          // Create a new bitmap and convert it to a format understood by the ImageView
          Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
          return scaledBitmap;
     }

     private float getScaleFactor(Bitmap bitmap, int boundBoxInDp) {

          // Get current dimensions
          int width = bitmap.getWidth();
          int height = bitmap.getHeight();

          // Determine how much to scale: the dimension requiring less scaling is
          // closer to the its side. This way the image always stays inside your
          // bounding box AND either x/y axis touches it.
          float xScale = ((float) boundBoxInDp) / width ;
          float yScale = ((float) boundBoxInDp) / height;
          float scale = (xScale <= yScale) ? xScale : yScale;

          return scale;

     }

     private float getScaleFactor(ImageView view, int boundBoxInDp) {

          // Get the ImageView and its bitmap
          Drawable drawing = view.getDrawable();
          Bitmap bitmap = ((BitmapDrawable)drawing).getBitmap();

          // Get current dimensions
          int width = bitmap.getWidth();
          int height = bitmap.getHeight();

          // Determine how much to scale: the dimension requiring less scaling is
          // closer to the its side. This way the image always stays inside your
          // bounding box AND either x/y axis touches it.
          float xScale = ((float) boundBoxInDp) / width ;
          float yScale = ((float) boundBoxInDp) / height;
          float scale = (xScale <= yScale) ? xScale : yScale;

          return scale;

     }

     private int dpToPx(int dp)
     {
          float density = getApplicationContext().getResources().getDisplayMetrics().density;
          return Math.round((float)dp * density);
     }


     /*
      ------------------------------------------------------------------------------------------------------------------
                                                        PRIVATE CLASSES
      ------------------------------------------------------------------------------------------------------------------
     */

     private class Patch extends FileHandler {

          int imgno;
          int patchno;
          float x, y, radius;
          String disease;
          ArrayList<String> analysis = new ArrayList<>();
          String remarks;
          int state;

          Patch(Context context, int imgno, int patchno, float x, float y, float radius, String disease) {

               super(context, "img" + String.format("%07d", imgno) + "_" + String.format("%03d", patchno), disease, true);
               this.imgno = imgno;
               this.patchno = patchno;
               this.x = x;
               this.y = y;
               this.radius = radius;
               this.disease = disease;
               this.remarks = "";
               this.state = PATCH_NEUTRAL;

          }

          public String formatImgno() {
               return imgno + "";
          }
          public String formatPatchno() {
               return String.format("%03d", patchno);
          }

     }

     class DrawingView extends ZoomImageView {
          Paint       mPaint;
          Bitmap  mBitmap;
          Canvas  mCanvas;
          Paint   mBitmapPaint;
          private float mX, mY;
          private static final float TOUCH_TOLERANCE = 10;
          float radius = 0;
          float cx = 0;
          float cy = 0;
          int width = 1;
          int height = 1;
          boolean isPatching = false;
          boolean initialized = false;

          final int CIRCLE_STROKE = 7;
          final int TEXT_STROKE = 5;
          final int OFFSET_CONSTANT = 10;

          public DrawingView(Context context) {
               super(context);
               // TODO Auto-generated constructor stub
               mPaint = new Paint();
               mPaint.setStrokeWidth(10);
               mPaint.setStyle(Paint.Style.STROKE);
               mPaint.setShadowLayer(5, 2, 2, Color.BLACK);
               mPaint.setTextSize(50);
               mPaint.setTextAlign(Paint.Align.CENTER);
               mPaint.setAntiAlias(true);
               mPaint.setColor(Color.WHITE);

               //mPath = new Path();
               mBitmapPaint = new Paint();
               mBitmapPaint.setColor(getResources().getColor(R.color.teal));
          }

          public DrawingView(Context context, Bitmap bitmap, ViewGroup vg) {
               super(context,bitmap,vg);
               mPaint = new Paint();
               mPaint.setStrokeWidth(10);
               mPaint.setStyle(Paint.Style.STROKE);
               mPaint.setShadowLayer(5, 2, 2, Color.BLACK);
               mPaint.setTextSize(50);
               mPaint.setTextAlign(Paint.Align.CENTER);
               mPaint.setAntiAlias(true);
               mPaint.setColor(Color.WHITE);

               //mPath = new Path();
               mBitmapPaint = new Paint();
               mBitmapPaint.setColor(getResources().getColor(R.color.teal));
          }


          @Override
          protected void onSizeChanged(int w, int h, int oldw, int oldh) {
               super.onSizeChanged(w, h, oldw, oldh);
               mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
               mCanvas = new Canvas(mBitmap);
               width = w;
               height = h;
          }
          @Override
          public void draw(Canvas canvas) {
               // TODO Auto-generated method stub
               super.draw(canvas);
               canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

               if (!initialized) {
                    for (int i = 0; i < patches.size(); i++) {
                         Patch patch = patches.get(i);

                         if (patch.state == PATCH_NEUTRAL) mPaint.setColor(Color.WHITE);
                         else if (patch.state == PATCH_COMPLETE)
                              mPaint.setColor(getResources().getColor(R.color.green));
                         else mPaint.setColor(getResources().getColor(R.color.red));

                         mPaint.setStrokeWidth(CIRCLE_STROKE);
                         mCanvas.drawCircle(patch.x, patch.y, patch.radius, mPaint);

                         String text = (patch.patchno + 1) + "";
                         mPaint.setStrokeWidth(TEXT_STROKE);
                         mCanvas.drawText(text, patch.x, patch.y - ((mPaint.descent() + mPaint.ascent()) / 2) , mPaint);
                    }
                    initialized = true;
               }

          }

          public void resetDraw() {

               clearDraw();

               for (int i = 0; i<patches.size(); i++) {
                    Patch patch = patches.get(i);

                    if (patch.state == PATCH_NEUTRAL) mPaint.setColor(Color.WHITE);
                    else if (patch.state == PATCH_COMPLETE)
                         mPaint.setColor(getResources().getColor(R.color.green));
                    else mPaint.setColor(getResources().getColor(R.color.red));

                    mPaint.setStrokeWidth(CIRCLE_STROKE);
                    mCanvas.drawCircle(patch.x, patch.y, patch.radius, mPaint);

                    String text = (patch.patchno + 1) + "";
                    mPaint.setStrokeWidth(TEXT_STROKE);
                    mCanvas.drawText(text, patch.x, patch.y - ((mPaint.descent() + mPaint.ascent()) / 2), mPaint);
               }

          }

          public void clearDraw() {

               mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
               mCanvas = new Canvas(mBitmap);
               invalidate();

          }


          private void touch_start(float x, float y) {
               initX = x;
               initY = y;

               // Check if area is patched. If yes, open dialog box.
               isPatching = true;
               /*
               for (int i = 0; i<patches.size(); i++) {
                    Patch patch = patches.get(i);
                    if (isBetween(patch.x,patch.y,x,y,patch.radius)) {
                         current_patch = i;
                         currently_new = false;
                         isPatching = false;
                         showDialogBox();
                         //Toast.makeText(context, "Patch exists", Toast.LENGTH_SHORT).show();
                         break;
                    }
               }
               */

          }

          private void touch_up(float x, float y) {

               mX = x;
               mY = y;
               radius = getRadius(initX, initY, mX, mY);

               if (radius > TOUCH_TOLERANCE && isPatching) { //10

                    cx = getMidpoint(initX, mX);
                    cy = getMidpoint(initY, mY);
                    getDrawBounds();
                    int offset = (getBitmapOffset(false))[0];

                    if ((cy-radius) > offset && (cy+radius) < (mContentView.getHeight()-offset)) {
                         //Toast.makeText(context, "Must create patch!!!", Toast.LENGTH_SHORT).show();
                         createPatch(cx, cy, radius);

                         Patch patch = patches.get(current_patch);

                         if (patch.state == PATCH_NEUTRAL) mPaint.setColor(Color.WHITE);
                         else if (patch.state == PATCH_COMPLETE)
                              mPaint.setColor(getResources().getColor(R.color.green));
                         else mPaint.setColor(getResources().getColor(R.color.red));

                         mPaint.setStrokeWidth(CIRCLE_STROKE);
                         mCanvas.drawCircle(cx, cy, radius, mPaint);

                         String text = (current_patch + 1) + "";
                         mPaint.setStrokeWidth(TEXT_STROKE);
                         mCanvas.drawText(text, cx, cy - ((mPaint.descent() + mPaint.ascent()) / 2), mPaint);

                         //Toast.makeText(context, "Patch created! \\:D/\nX: " + patch.x + "\nY: " + patch.y + "\nRadius: " + patch.radius, Toast.LENGTH_LONG).show();

                         updateProgress();
                    }
                    else Toast.makeText(context, "Patch must not exceed image bounds.", Toast.LENGTH_SHORT).show();

                    isPatching = false;
               }

               else {
                    // Check if within patch area. If yes, open dialog box.
                    int i;
                    for (i = 0; i<patches.size(); i++) {
                         Patch patch = patches.get(i);
                         if (isBetween(patch.x,patch.y,x,y,patch.radius)) {
                              current_patch = i;
                              currently_new = false;
                              isPatching = false;
                              showDialogBox();
                              //Toast.makeText(context, "Patch exists", Toast.LENGTH_SHORT).show();
                              break;
                         }
                    }
               }

          }

          @Override
          public boolean onTouchEvent(MotionEvent event) {
               float x = event.getX();
               float y = event.getY();
               Toast.makeText(context, "TOUCH", Toast.LENGTH_SHORT).show();

               switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                         touch_start(x, y);
                         invalidate();
                         break;
                    /*case MotionEvent.ACTION_MOVE:
                         touch_move(x, y);
                         invalidate();y
                         break;
                    */
                    case MotionEvent.ACTION_UP:
                         touch_up(x,y);
                         invalidate();
                         break;
               }
               return true;
          }

          public void getDrawBounds() {
               drawable = mContentView.getDrawable();
               mContentView_top = drawable.getBounds().top;
               mContentView_bottom = drawable.getBounds().bottom;
          }

          public int[] getBitmapOffset(boolean includeLayout) {
               int[] offset = new int[2];
               float[] values = new float[9];

               Matrix m = getImageMatrix();
               m.getValues(values);

               offset[0] = (int) values[5];
               offset[1] = (int) values[2];

               if (includeLayout) {
                    ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) getLayoutParams();
                    int paddingTop = getPaddingTop();
                    int paddingLeft = getPaddingLeft();

                    offset[0] += paddingTop + lp.topMargin;
                    offset[1] += paddingLeft + lp.leftMargin;
               }
               return offset;
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

     public void initializeUI(String disease) {

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
               case "Unspecified Infection":
                    species = getResources().getStringArray(R.array.unspecified_species);
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
                                        //drawBoxes(DRAW_CURRENT);
                                        mContentView.resetDraw();
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

     public int tokenizeImageNum(File image) {
          StringTokenizer token1 = new StringTokenizer(image.getName(),"image");

          String[] image_file_types = getResources().getStringArray(R.array.image_file_types);
          StringTokenizer token2 = token1;
          for (int i = 0; i<image_file_types.length; i++) {
               String file_type = image_file_types[i];
               if (file_type.contains(file_type)) token2 = new StringTokenizer(token1.nextToken(), "." + file_type);
          }

          return Integer.parseInt(token2.nextToken());
     }

     public void loadNextImage() {

          /*
          File[] images = (new File(image_directory)).listFiles();


           // UNCOMMENT ME IN MAIN
          if (images.length == 0) {
               Intent intent = new Intent(getApplicationContext(), NoImagesActivity.class);
               startActivity(intent);
               progress_file.delete();
               finish();
          }

          File image = images[0]; // Gets first image in image_directory
          //Toast.makeText(context, "Retrieved image: " + image.getAbsolutePath(), Toast.LENGTH_SHORT).show();
          current_image = tokenizeImageNum(image);
          Bitmap imageBitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
          */

          current_image++; // HERE IN TESTZOOM ONLY
          if (current_image == 5) { // HERE IN TESTZOOM ONLY
               Intent intent = new Intent(getApplicationContext(), NoImagesActivity.class); // HERE IN TESTZOOM ONLY
               startActivity(intent); // HERE IN TESTZOOM ONLY
               progress_file.delete(); // HERE IN TESTZOOM ONLY
               finish(); // HERE IN TESTZOOM ONLY
          } // HERE IN TESTZOOM ONLY

          BitmapDrawable bitmapDrawable = (BitmapDrawable)sample_images[current_image]; // HERE IN TESTZOOM ONLY
          Bitmap imageBitmap = bitmapDrawable.getBitmap(); // HERE IN TESTZOOM ONLY

          progress_file.write(current_image + "");
          initializeUI(disease);
          mContentView.setImageBitmap(imageBitmap);
          drawable = mContentView.getDrawable();
          mContentView_top = drawable.getBounds().top;
          mContentView_bottom = drawable.getBounds().bottom;

          patches.clear();
          mContentView.clearDraw();
          updateProgress();

          // Load custom display image
          Display display = getWindowManager().getDefaultDisplay();
          Point size = new Point();
          display.getSize(size);
          screen_width = size.x;

          mVisible = true;
          mContentView = new DrawingView(this, getScaledImage(imageBitmap,screen_width), mDrawingPad);
          mContentView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
          mContentView.setAdjustViewBounds(true);
          mContentView.setScaleType(ImageView.ScaleType.FIT_CENTER);
          mContentView.invalidate();
          mContentView.setMaxZoom(4f);
          //drawable = mContentView.getDrawable();

          mDrawingPad=(LinearLayout)findViewById(R.id.drawing_pad_test);
          mDrawingPad.addView(mContentView);

          scaleFactor = getScaleFactor(mContentView,screen_width);

          /*
          mContentView.setImageDrawable(sample_images[current_image]);
          drawable = mContentView.getDrawable();
          mContentView.resetDraw();
          mContentView.invalidate();

          scaleFactor = getScaleFactor(mContentView,screen_width);

          zoomContentView = new ZoomImageView(this, getScaledImage(mContentView,screen_width), mDrawingPad);
          //zoomContentView.setImageBitmap(getScaledImage(mContentView,screen_width));

          if (mode == MODE_ZOOM) {
               mDrawingPad.removeView(zoomContentView);
               mDrawingPad.addView(mContentView);
               mode = MODE_PATCH;
          }
          */

          disease_count_file.incrementCount(disease_num);

     }

     public void updateProgress() {
          //Updates current image file as well as patches created in image
          /* PROTOCOL:
          current_image$x1|y1|x2|y2|Species (comma-separated)|Remarks$x1|y1|x2|y2|Species (comma-separated)|Remarks
          */
          progress_file.write(current_image + "");
          for (int i = 0; i<patches.size(); i++) {
               Patch patch = patches.get(i);
               progress_file.append("$");
               progress_file.append(patch.x + "|" + patch.y + "|" + patch.radius + "|");
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


          /* UNCOMMENT IN MAIN

          File[] images = (new File(image_directory)).listFiles();
          File image = images[0];
          Bitmap imageBitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
          //Toast.makeText(context, "Retrieved image: " + image.getAbsolutePath(), Toast.LENGTH_SHORT).show();

          */

          BitmapDrawable bitmapDrawable = (BitmapDrawable)sample_images[current_image]; // HERE IN TESTZOOM ONLY
          Bitmap imageBitmap = bitmapDrawable.getBitmap(); // HERE IN TESTZOOM ONLY

          mContentView.setImageBitmap(imageBitmap);

          while (tokens.hasMoreTokens()) {
               String token = tokens.nextToken();
               StringTokenizer patchData = new StringTokenizer(token, "|");
               float x = Float.parseFloat(patchData.nextToken());
               float y = Float.parseFloat(patchData.nextToken());
               float radius = Float.parseFloat(patchData.nextToken());

               Patch patch = new Patch(context, current_image, patches.size(),x,y,radius,disease);
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
          Handler handler = new Handler();
          handler.postDelayed(new Runnable() {

               @Override
               public void run() {
                    labelDialog.show();
               }

          }, DELAY);

     }

     public void fetchPatchDataFromDialog() {

          int patchno = current_patch;
          Patch patch = patches.get(patchno);

          RadioGroup rg = (RadioGroup)labelDialog.findViewById(R.id.labeler_species);
          patch.analysis.clear();
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
               mContentView.resetDraw();
               if (currently_new) Toast.makeText(context, "Patch created!", Toast.LENGTH_SHORT).show();
               else Toast.makeText(context, "Patch modified!", Toast.LENGTH_SHORT).show();
               labelDialog.hide();
               updateProgress();
          }

     }

     public void createPatch(float x, float y, float radius) {
          final int patchno = patches.size();
          Patch patch = new Patch(context, current_image, patchno, x, y, radius, disease);
          patches.add(patch);
          currently_new = true;
          current_patch = patchno;
          updateProgress();
          showDialogBox();
     }

     public void deletePatch() {

          if (patches.size() > 0) {

               int patchno = current_patch;
               Patch patch = patches.get(patchno);
               patch.delete();
               patch.deleteFolder();
               patches.remove(patchno);
               for (int i = 0; i < patches.size(); i++) {
                    patches.get(i).patchno = i;
               }
               mContentView.resetDraw();
               updateProgress();
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

          patch.append("\n\n\t<coordinates>");
          patch.append("\n\t\t<x>" + patch.x + "</x>");
          patch.append("\n\t\t<y>" + patch.y + "</y>");
          patch.append("\n\t\t<radius>" + patch.radius + "</radius>");
          patch.append("\n\t</coordinates>");

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

     public void confirmSendData(View view) {

          // Confirm if send. Return if no, sendData() if yes.
          if (patches.size() == 0) {
               DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                         switch (which) {
                              case DialogInterface.BUTTON_POSITIVE:
                                   // Send diagnosis
                                   //sendData(false);
                                   loadNextImage();
                                   break;
                              case DialogInterface.BUTTON_NEGATIVE:
                                   // Cancel
                                   break;
                         }
                    }
               };
               AlertDialog.Builder builder = new AlertDialog.Builder(TestZoomPaintActivity.this);
               builder.setMessage("Sending a diagnosis without creating any patches would mean this sample is not infected. Are you sure you want to send this diagnosis?").setPositiveButton("Yes", dialogClickListener)
                       .setNegativeButton("No", dialogClickListener)
                       .setTitle("Send diagnosis without patches").show();
          }
          else {

               DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                         switch (which) {
                              case DialogInterface.BUTTON_POSITIVE:
                                   sendData(true);
                                   break;
                              case DialogInterface.BUTTON_NEGATIVE:
                                   return;
                         }
                         }
               };
               AlertDialog.Builder builder = new AlertDialog.Builder(TestZoomPaintActivity.this);
               builder.setMessage("Are you sure you want to send the diagnosis?").setPositiveButton("Yes", dialogClickListener)
                       .setNegativeButton("No", dialogClickListener)
                       .setTitle("Send diagnosis").show();

          }
     }

     public void sendData(boolean infected) {

          // Check if all patches have enough data. Continue if yes, toast and return if no.
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
          for (int i = 0; i < patches.size(); i++) {
               createPatchXML(i);
          }

          // Compress all files in zip, send zip file, delete patch data files
          String imageFolder = context.getFilesDir() + "/" + disease;
          String zipPath = context.getFilesDir() + "/" + disease + "/img" + patches.get(0).formatImgno() + ".zip";
          uploadZipfile(imageFolder, zipPath); //bring me to life
          //Toast.makeText(context, "SENTTT!!!", Toast.LENGTH_SHORT).show(); //test

          // Load next image
          loadNextImage();
     }

     public void uploadXMLFiles() {

          // Delete progress_file
          //progress_file.delete();

          // StringBuilder sb = new StringBuilder("PATCH MSG:"); //test
          // Delete patch data files
          for (int i = 0; i<patches.size(); i++) { //deletes patch data
               Patch patch = patches.get(i);
               String msg = uploader.uploadFile(patch.file,patch.formatImgno()+"_"+patch.formatPatchno()+".xml",false);
               // sb.append("\n" + msg); //test
               patch.delete();
               patch.deleteFolder();
          }
          //Toast.makeText(context, sb.toString(), Toast.LENGTH_SHORT).show(); // test

     }

     public void uploadZipfile(String imageFolder, String zipPath) {

          // Delete progress_file
          //progress_file.delete();

          // Compress files in zip
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
          } catch (Exception ex) {
               Log.d("",ex.getMessage());
          }

          File file = new File(zipPath);

          String msg = uploader.uploadFile(file,zipPath,true);
          Toast.makeText(context, msg, Toast.LENGTH_SHORT).show(); //test

          // Delete patch data files
          for (int i = 0; i<patches.size(); i++) { //deletes patch data
               Patch patch = patches.get(i);
               patch.delete();
               patch.deleteFolder();
          }

     }

     public void testonly(View view) {
          StringBuilder sb = new StringBuilder("MYDIRECTORY CONTENTS");
          File[] files = myDirectory.listFiles();
          for (int i = 0; i<files.length; i++) {
               sb.append("\n" + files[i].getPath() + ": " + uploader.getCurrentCode);
          }
          if (files.length == 0) sb.append("\nNo files");
          //Toast.makeText(context, sb.toString(), Toast.LENGTH_SHORT).show();
     }

     public void tokenizeAddress(String url) {
          StringTokenizer tokenizer = new StringTokenizer(url,":");
          if (tokenizer.countTokens()>1) {
               HTTP_IP_ADDRESS = tokenizer.nextToken();
               url = tokenizer.nextToken();
               tokenizer = new StringTokenizer(url,"/");
               String port = tokenizer.nextToken();
               if (!port.equals("")) HTTP_PORT = Integer.parseInt(port);
               else HTTP_PORT = -1;
          }
          else {
               tokenizer = new StringTokenizer(url,"/");
               HTTP_IP_ADDRESS = tokenizer.nextToken();
          }
          HTTP_HOME = tokenizer.nextToken();
          //Toast.makeText(context, "IP address: " + HTTP_IP_ADDRESS + "\nPort: " + HTTP_PORT + "\nHome: " + HTTP_HOME, Toast.LENGTH_SHORT).show();
     }

     public boolean isBetween(float cx, float cy, float x, float y, float radius) {
          return (getRadius(cx,cy,x,y) <= radius);
     }

     public float getMidpoint(float a, float b) {
          float smaller;
          if (a<b) smaller = a;
          else smaller = b;

          return smaller + (Math.abs(a-b)/2);
     }

     public float getRadius(float x1, float y1, float x2, float y2) {
          return (float)((Math.sqrt(((x1-x2)*(x1-x2))+((y1-y2)*(y1-y2))))/2)-10;
     }



     /*
      ------------------------------------------------------------------------------------------------------------------
                                                           ASYNCTASK
      ------------------------------------------------------------------------------------------------------------------
     */

     private class Initializer extends AsyncTask<String, Void, Boolean> {

          @Override
          protected Boolean doInBackground(String... params) {
               initializeUI(params[0]);
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