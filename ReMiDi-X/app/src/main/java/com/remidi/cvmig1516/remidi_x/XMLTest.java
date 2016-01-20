package com.remidi.cvmig1516.remidi_x;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
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

public class XMLTest extends ActionBarActivity {

     final int DELAY = 300;
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

     String disease = "Disease";
     String validator = "Validator";
     XMLFileHandler progress_file;
     ArrayList<Patch> patches = new ArrayList<>();

     int ctr = 0;
     float initX = 0;
     float initY = 0;
     float scaleFactor = 1;
     float bounds_left = 0;
     float bounds_top = 0;
     Bitmap origBitmap;
     File myDirectory;

     private ImageView mContentView;
     private View mControlsView;
     private boolean mVisible;

     // Web url
     public String HTTP_IP_ADDRESS = "54.179.135.52";
     public String HTTP_HOME = "/api/label/";

     public int HTTP_PORT = 80;
     public boolean isThreadPause = false;
     Uploader uploader;

     @Override
     protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_xmltest);
          context = getApplicationContext();
          //mContentView = (ImageView)findViewById(R.id.imagetest);
          //origBitmap = ((BitmapDrawable)mContentView.getDrawable()).getBitmap();

          final LinearLayout mDrawingPad=(LinearLayout)findViewById(R.id.view_drawing_pad);
          final DrawingView mDrawingView = new DrawingView(this);
          Drawable drawable = getResources().getDrawable(R.drawable.img0000000_000);
          mDrawingView.setImageDrawable(drawable);

          Display display = getWindowManager().getDefaultDisplay();
          Point size = new Point();
          display.getSize(size);
          int width = size.x;
          int height = size.y;

          int idealWidth = width;
          int idealHeight;
          int actualWidth = drawable.getIntrinsicWidth();
          int actualHeight = drawable.getIntrinsicHeight();
          float scaleFactor = ((float)idealWidth)/((float)actualWidth);
          idealHeight = (int)(scaleFactor*actualHeight);

          mDrawingPad.setMinimumWidth(width);

          Button b = new Button(context);
          View.OnClickListener clicker = new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                    patches.remove(0);
                    mDrawingView.resetDraw();
               }
          };
          b.setOnClickListener(clicker);
          b.setText("BUTTON");
          mDrawingPad.addView(b);
          mDrawingPad.addView(mDrawingView);


     }

     class DrawingView extends ImageView {
          Paint       mPaint;
          Paint textPaint;
          Bitmap  mBitmap;
          Canvas  mCanvas;
          Paint   mBitmapPaint;
          private float mX, mY;
          private static final float TOUCH_TOLERANCE = 4;
          float radius = 0;
          float cx = 0;
          float cy = 0;
          int width = 1;
          int height = 1;

          public DrawingView(Context context) {
               super(context);
               // TODO Auto-generated constructor stub
               mPaint = new Paint();
               mPaint.setStrokeWidth(10);
               mPaint.setStyle(Paint.Style.STROKE);
               mPaint.setShadowLayer(5, 2, 2, Color.BLACK);
               mPaint.setTextSize(50);
               mPaint.setAntiAlias(true);
               mPaint.setColor(Color.WHITE);

               //mPath = new Path();
               mBitmapPaint = new Paint();
               mBitmapPaint.setColor(Color.RED);
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
               //canvas.drawCircle(cx, cy, radius, mPaint);
          }

          public void resetDraw() {

               clearDraw();

               for (int i = 0; i<patches.size(); i++) {
                    Patch patch = patches.get(i);

                    mPaint.setStrokeWidth(5);
                    mCanvas.drawCircle(patch.x, patch.y, patch.radius, mPaint);
                    mPaint.setStrokeWidth(3);
                    mCanvas.drawText((patch.patchno+1) + "", patch.x, patch.y, mPaint);
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
          }
          private void touch_move(float x, float y) {
               float dx = Math.abs(x - mX);
               float dy = Math.abs(y - mY);
               if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                    mX = x;
                    mY = y;
               }
          }
          private void touch_up() {

               radius = getRadius(initX, initY, mX, mY);

               if (radius > 4) { //10
                    //createPatch(initX,initY,finalX,finalY);
                    //Toast.makeText(getApplicationContext(), getMidpoint(initX,finalX) + ", " + getMidpoint(initY,finalY), Toast.LENGTH_SHORT).show();
                    cx = getMidpoint(initX, mX);
                    cy = getMidpoint(initY, mY);

                    Patch patch = new Patch(context, current_image, patches.size(),cx,cy,radius,disease);
                    patches.add(patch);
                    mPaint.setStrokeWidth(5);
                    mCanvas.drawCircle(cx, cy, radius, mPaint);
                    mPaint.setStrokeWidth(3);
                    mCanvas.drawText((patch.patchno+1) + "", cx, cy, mPaint);
                    Toast.makeText(context, "Patch exists", Toast.LENGTH_SHORT).show();
                    //new ProgressUpdater().execute();
               }

               /*
               else {
                    // Check if area is patched. If yes, open dialog box.
                    for (int i = 0; i<patches.size(); i++) {
                         Patch patch = patches.get(i);
                         if (isBetween(patch.x1,patch.x2,finalX) && isBetween(patch.y1,patch.y2,finalY)) {
                              current_patch = i;
                              currently_new = false;
                              Toast.makeText(context, "Patch exists", Toast.LENGTH_SHORT).show();
                         }
                    }
               }
               */




               // commit the path to our offscreen
               // mCanvas.drawPath(mPath, mPaint);

               // PAINT OPERATION HERE
               // PUT PATCH SHIZNIZ HERE

               //float radius = 0;
               //float cx = 0;
               //float cy = 0;
          }

          @Override
          public boolean onTouchEvent(MotionEvent event) {
               float x = event.getX();
               float y = event.getY();

               switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                         touch_start(x, y);
                         invalidate();
                         break;
                    case MotionEvent.ACTION_MOVE:
                         touch_move(x, y);
                         invalidate();
                         break;
                    case MotionEvent.ACTION_UP:
                         touch_up();
                         invalidate();
                         break;
               }
               return true;
          }
     }

     public float getRadius(float x1, float y1, float x2, float y2) {
          return (float)(Math.sqrt(((x1-x2)*(x1-x2))+((y1-y2)*(y1-y2)))/2);
     }

     public float getMidpoint(float a, float b) {
          float smaller;
          if (a<b) smaller = a;
          else smaller = b;

          return smaller + (Math.abs(a-b)/2);
     }

     // ==============================================================================================

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
                    canvas.drawCircle(patch.x,patch.y,patch.radius,paint);
                    canvas.drawText("" + (patch.patchno + 1), patch.x, patch.y, paint);
               }
          }
          else {
               Patch patch = patches.get(current_patch);
               if (patch.state == PATCH_NEUTRAL) paint.setColor(Color.WHITE);
               else if (patch.state == PATCH_COMPLETE)
                    paint.setColor(getResources().getColor(R.color.green));
               else paint.setColor(getResources().getColor(R.color.red));
               canvas.drawCircle(patch.x,patch.y,patch.radius,paint);
               canvas.drawText("" + (patch.patchno+1), patch.x, patch.y, paint);
          }

          //Attach the canvas to the ImageView
          imageView.setImageBitmap(newBitmap);

     }

     private class Patch extends XMLFileHandler{

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
               return String.format("%07d", imgno);
          }
          public String formatPatchno() {
               return String.format("%03d", patchno);
          }

     }

}