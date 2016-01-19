package com.remidi.cvmig1516.remidi_x;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class XMLTest extends ActionBarActivity {

     XMLFileHandler progress_file;
     int current_image = 0;

     final int PATCH_NEUTRAL = 0;
     final int PATCH_COMPLETE = 1;
     final int PATCH_INCOMPLETE = 2;

     String disease = "Disease";
     Context context;
     File myDirectory;

     @Override
     protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_xmltest);
          context = getApplicationContext();
          myDirectory = new File(context.getFilesDir(), "database");
          if( !myDirectory.exists() ) {
               myDirectory.mkdirs();
          }

     }

     public void tokenizeAddress(View view) {
          // http://0.0.0.0:port/home
          String ipaddress = "";
          String port = "";
          String home = "";

          EditText urlwriter = (EditText)findViewById(R.id.urlwriter);
          String url = urlwriter.getText().toString();
          TextView text = (TextView)findViewById(R.id.xmltester);
          text.setText("Original text: " + url);
          TextView text2 = (TextView)findViewById(R.id.xmltester2);
          StringBuilder sb = new StringBuilder();

          // url = 0.0.0.0:port/home
          StringTokenizer tokenizer = new StringTokenizer(url,":");
          if (tokenizer.countTokens()>1) {
               ipaddress = tokenizer.nextToken();
               url = tokenizer.nextToken();
               // url = port/home
               tokenizer = new StringTokenizer(url,"/");
               port = tokenizer.nextToken();
          }
          else {
               tokenizer = new StringTokenizer(url,"/"); // url = 0.0.0.0/home
               ipaddress = tokenizer.nextToken();
          }
          home = tokenizer.nextToken();

          if (port.equals("")) port = "(No port)";

          sb.append("\nIP Address: " + ipaddress);
          sb.append("\nPort: " + port);
          sb.append("\nHome: " + home);
          text2.setText(sb.toString());
     }

}
