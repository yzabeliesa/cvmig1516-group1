package com.remidi.cvmig1516.remidi_x;

/*
FUCKING DEPENDENCIES:
compile 'org.apache.httpcomponents:httpclient:4.5.1'
compile 'org.apache.httpcomponents:httpclient-osgi:4.5.1'

*/

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.AbstractHttpParams;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.commons.io.comparator.LastModifiedFileComparator;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class TestActivity extends ActionBarActivity {

     //public static final String FILE_NAME = "testfile.xml";
     //public static final String FILE_NAME = "validation.xml";
     public static final String FILE_NAME = "bago";
     // Local url
     public String HTTP_HOST = "192.168.1.2";
     public String HTTP_HOME = "/data/";
     public int HTTP_PORT = 5000;

     // Web url
     //public String HTTP_HOST = "54.179.135.52";
     //public String HTTP_HOME = "/api/label/";
     //public int HTTP_PORT = 80;


     public String exception_message = "none";
     public String send_result = "";


     public int stat = 0;
     public File myDirectory;
     public File[] myGallery;
     public boolean isThreadPause = false;
     public Context context;

     public String DOWNLOAD_URL = "http://54.179.135.52/pic/1/";
     public int IMG_ID = 0;
     public int DISEASE_ID = 0;
     public String fn = "";
     public int getCurrentCode;

     /**
      * ATTENTION: This was auto-generated to implement the App Indexing API.
      * See https://g.co/AppIndexing/AndroidStudio for more information.
      */

     //------------------------------------------------------------------------------------------------------------------
     //
     //                                             SERVICE METHODS TO OVERRIDE
     //
     //------------------------------------------------------------------------------------------------------------------


     @Override
     protected void onCreate(Bundle savedInstanceState) {

          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_test);
          context = getApplicationContext();

          myDirectory = new File(context.getFilesDir(), "remidiDatabase");
          if( !myDirectory.exists() ) {
               myDirectory.mkdirs();
          }

          myGallery = new File[19];
          for(int x=0; x<19; x++) {
               myGallery[x] = new File(context.getFilesDir(), "disease_" + (x+1));
               if( !myGallery[x].exists() ) {
                    myGallery[x].mkdirs();
               }
          }

         new Thread(null, send, "SendThread").start();
     }

     @Override
     protected void onPause() {
          isThreadPause = true;
          super.onPause();
          Toast.makeText(getApplicationContext(), "Paused", Toast.LENGTH_SHORT).show();
     }

     @Override
     protected void onResume(){
          isThreadPause = false;
          super.onResume();
          Toast.makeText(getApplicationContext(), "Resumed", Toast.LENGTH_SHORT).show();
     }

     //------------------------------------------------------------------------------------------------------------------
     //
     //                                                RUNNABLE THREADS
     //
     //------------------------------------------------------------------------------------------------------------------


     Runnable send = new Runnable() {
          @Override
          public void run() {
               while (true) {
                    if (!isThreadPause) {
                         String urlstr = "http://" + HTTP_HOST + ":" + HTTP_PORT + HTTP_HOME;
                         if (myDirectory.isDirectory()) {
                              File[] xml_files = myDirectory.listFiles();

                              if ((xml_files.length > 0) && (isNetworkAvailable())) {
                                   Arrays.sort(xml_files, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
                                   send_result = Send_File(urlstr, xml_files[0]);
                              }
                         }
                    }
               }
          }
     };

     //------------------------------------------------------------------------------------------------------------------
     //
     //                                                ACCESSORY METHODS
     //
     //------------------------------------------------------------------------------------------------------------------

     public boolean isNetworkAvailable() {
          ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
          NetworkInfo ani = cm.getActiveNetworkInfo();
          return (ani != null && ani.isConnected());
     }

     public String make_xml() {

          StringBuilder xmlfile = new StringBuilder();
          xmlfile.append("<?xml version='1.0' encoding='us-ascii'?> \n");
          xmlfile.append("<!--  A SAMPLE --> \n");
          xmlfile.append("<displayName> My Message </displayName> \n");
          xmlfile.append("<msg> " + " </msg> \n");
          return xmlfile.toString();

     }

     public void getStatus(View view) {
          ((TextView)findViewById(R.id.some_textview)).setText( myDirectory.listFiles().length + "\n" + myGallery[0].listFiles().length + "\n" + myGallery[1].listFiles().length + "\n" + myGallery[2].listFiles().length + "\n" + myGallery[3].listFiles().length + "\n" + myGallery[4].listFiles().length);
          ImageView iv = (ImageView) findViewById(R.id.imageViewId);
          iv.setImageBitmap(BitmapFactory.decodeFile(myGallery[0].listFiles()[0].getAbsolutePath()));
     }

     public void deleteAllFiles(View view) {
          File[] xml_files = myDirectory.listFiles();
          for(int x=0; x<xml_files.length; x++) {
               xml_files[x].delete();
          }
     }

     //------------------------------------------------------------------------------------------------------------------
     //
     //                                  THIS IS FOR SENDING FILES FROM THE LOCAL DATABASE
     //
     //------------------------------------------------------------------------------------------------------------------


     /*public void send_task(View view) {
          String urlstr = "http://" + HTTP_HOST + ":" + HTTP_PORT + HTTP_HOME;
          if (myDirectory.isDirectory()) {
               File[] xml_files = myDirectory.listFiles();

               if ((xml_files.length > 0) && (isNetworkAvailable())) {
                    Arrays.sort(xml_files, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
                    send_result = Send_File(urlstr, xml_files[0]);
               }
          }
     }*/

     public String Send_File(String urlstr, File current_file) {
          String result = null;
          try {

               HttpHost targetHost = null;
               HttpClient hc = null;
               HttpResponse hr = null;
               String boundary = "-------------" + System.currentTimeMillis();
               HttpPost httpPost = new HttpPost(urlstr);
               httpPost.setHeader("ENCTYPE", "multipart/form-data");
               httpPost.setHeader("Accept", "application/xml");
               httpPost.setHeader("Content-type", "multipart/form-data; boundary=" + boundary);

               HttpParams params = new BasicHttpParams();
               params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
               FileBody fb = new FileBody(current_file, ContentType.APPLICATION_XML, current_file.getName());

               MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create();
               multipartEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
               multipartEntity.setBoundary(boundary);

               multipartEntity.addPart("uploaded_file", fb);

               httpPost.setEntity(multipartEntity.build());
               targetHost = new HttpHost(HTTP_HOST, HTTP_PORT, "http");
               hc = new DefaultHttpClient();
               hr = hc.execute(targetHost, httpPost);

               if (hr != null) {
                    BasicResponseHandler responseHandler = new BasicResponseHandler();
                    result = responseHandler.handleResponse(hr);
                    getCurrentCode = hr.getStatusLine().getStatusCode();
                    if (getCurrentCode == 200) current_file.delete();

               } else {
                    result = "Didn't work!";
               }

          } catch (Exception e) {
               e.printStackTrace();
               Log.d("OutputStream", e.getLocalizedMessage());
               result = e.getLocalizedMessage();
          }

          return result;
     }

     //------------------------------------------------------------------------------------------------------------------
     //
     //                                    THIS IS FOR UPLOADING FILES TO THE LOCAL DATABASE
     //
     //------------------------------------------------------------------------------------------------------------------

     public void upload(View view) {
          String baseDir = myDirectory.getAbsolutePath();
          new UploadTask().execute(baseDir);
          Toast.makeText(getApplicationContext(), "Uploaded: " + exception_message, Toast.LENGTH_SHORT).show();
     }

     public String Upload_File(String filepath) {
          //-- make xml file here --//
          File f;
          FileOutputStream fop;
          String msg = "File not created";
          try {
               f = new File(filepath, FILE_NAME + stat + ".xml");
               if( !f.exists() ) {
                    f.createNewFile();
                    msg = "File Created";
               }

               fop = new FileOutputStream(f);
               fop.write(make_xml().getBytes());
               fop.flush();
               fop.close();
               msg = "Save Successful";
          } catch (Exception e) {
               e.printStackTrace();
               msg = "Exception occured";
          }

          stat++;

          return msg;
     }

     private class UploadTask extends AsyncTask<String, Void, String> {
          @Override
          protected String doInBackground(String... params) {
               exception_message = "";
               String str = Upload_File(params[0]);
               exception_message = str;
               return str;
          }

          @Override
          protected void onPostExecute(String result) {
          }
     }

     //------------------------------------------------------------------------------------------------------------------
     //
     //                                      THIS IS FOR GETTING IMAGE FROM GIVEN URL
     //
     //------------------------------------------------------------------------------------------------------------------

     public void download(View view) {
          new DownloadTask().execute(DOWNLOAD_URL);
     }

     public Bitmap Download_Image( String urlstr ) {

          HttpClient hc = new DefaultHttpClient();
          HttpGet httpGet = new HttpGet(urlstr);
          HttpEntity entity = null;
          HttpResponse response = null;
          InputStream is = null;
          Bitmap bitmap = null;

          try {

               response = hc.execute(httpGet);
               int statusCode = response.getStatusLine().getStatusCode();

               if (statusCode == HttpStatus.SC_OK) {
                    entity = response.getEntity();
                    if (entity != null) {
                         try {
                              is = entity.getContent();
                              bitmap = BitmapFactory.decodeStream(is);
                         } finally {
                              if (is != null) {
                                   is.close();
                              }
                              entity.consumeContent();
                         }
                    }
               }

               else {
                    return null;
               }

          } catch (Exception e) {
               httpGet.abort();
               e.printStackTrace();
               Log.d("InputStream", e.getLocalizedMessage());
          }

          return bitmap;
     }

     public class DownloadTask extends AsyncTask<String, Void, Bitmap> {

          @Override
          protected Bitmap doInBackground(String... param) {
               return Download_Image(param[0]);
          }

          @Override
          protected void onPostExecute(Bitmap result) {
               File f;
               FileOutputStream fop;
               String diseaseDir = myGallery[DISEASE_ID].getAbsolutePath();
               fn = "img" + IMG_ID + ".png";
               try {

                    if( result != null ) {
                         f = new File(diseaseDir, fn);
                         fop = new FileOutputStream(f);
                         result.compress(Bitmap.CompressFormat.PNG, 100, fop);
                         fop.flush();
                         fop.close();
                    }

               } catch (Exception e) {
                    e.printStackTrace();
               }
          }
     }
}
