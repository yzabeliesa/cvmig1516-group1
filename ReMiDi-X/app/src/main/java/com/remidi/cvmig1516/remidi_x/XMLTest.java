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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.text.InputFilter;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
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

          DrawingView mDrawingView=new DrawingView(this);
          LinearLayout mDrawingPad=(LinearLayout)findViewById(R.id.view_drawing_pad);
          mDrawingPad.addView(mDrawingView);

          mDrawingPad.setBackground(getResources().getDrawable(R.drawable.img0000000_000));

     }

     class DrawingView extends View {
          Paint       mPaint;
          //MaskFilter  mEmboss;
          //MaskFilter  mBlur;
          Bitmap  mBitmap;
          Canvas  mCanvas;
          Paint   mBitmapPaint;
          private float mX, mY;
          private static final float TOUCH_TOLERANCE = 4;
          float radius = 0;
          float cx = 0;
          float cy = 0;

          public DrawingView(Context context) {
               super(context);
               // TODO Auto-generated constructor stub
               mPaint = new Paint();
               mPaint.setAntiAlias(true);
               mPaint.setDither(true);
               mPaint.setColor(0xFFFF0000);
               mPaint.setStyle(Paint.Style.STROKE);
               mPaint.setStrokeJoin(Paint.Join.ROUND);
               mPaint.setStrokeCap(Paint.Cap.ROUND);
               mPaint.setStrokeWidth(10);

               //mPath = new Path();
               mBitmapPaint = new Paint();
               mBitmapPaint.setColor(Color.RED);
          }
          @Override
          protected void onSizeChanged(int w, int h, int oldw, int oldh) {
               super.onSizeChanged(w, h, oldw, oldh);
               mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
               mCanvas = new Canvas(mBitmap);
          }
          @Override
          public void draw(Canvas canvas) {
               // TODO Auto-generated method stub
               super.draw(canvas);
               canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
               canvas.drawCircle(cx, cy, radius, mPaint);
          }


          private void touch_start(float x, float y) {
               //mPath.reset();
               //mPath.moveTo(x, y);
               initX = x;
               initY = y;
          }
          private void touch_move(float x, float y) {
               float dx = Math.abs(x - mX);
               float dy = Math.abs(y - mY);
               if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                    //mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                    mX = x;
                    mY = y;
               }
          }
          private void touch_up() {
               //mPath.lineTo(mX, mY);
               radius = getRadius(initX, initY, mX, mY);
               cx = getMidpoint(initX, mX);
               cy = getMidpoint(initY, mY);

               // commit the path to our offscreen
               //mCanvas.drawPath(mPath, mPaint);
               mCanvas.drawCircle(cx,cy,radius,mPaint);
               //mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
               // kill this so we don't double draw
               //mPath.reset();
               // mPath= new Path();
               float radius = 0;
               float cx = 0;
               float cy = 0;
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

     class CircleThing {
          float radius;
          float x;
          float y;

          CircleThing(float radius, float x, float y) {
               this.radius = radius;
               this.x = x;
               this.y = y;
          }

     }

}
