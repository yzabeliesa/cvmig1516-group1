package com.remidi.cvmig1516.remidi_x;

/*
FUCKING DEPENDENCIES:
compile 'org.apache.httpcomponents:httpclient:4.5.1'
compile 'org.apache.httpcomponents:httpclient-osgi:4.5.1'

*/

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TestActivity extends ActionBarActivity {

     //public static final String FILE_NAME = "testfile.xml";
     //public static final String FILE_NAME = "validation.xml";
     public static final String FILE_NAME = "bago";
     // Local url
     //public String HTTP_HOST = "192.168.1.2";
     //public String HTTP_HOME = "/data/";
     //public int HTTP_PORT = 5000;
     public int VALIDATOR_ID = 1;
     public String MD5 = "";
     public int IMAGE_COUNTS = 10;
     public String RESPONDED = "";

     // Web url
     public String HTTP_HOST = "54.179.135.52";
     public String HTTP_HOME = "/api/label/";
     public int HTTP_PORT = 80;
     public boolean CONFIRM = false;


     public String exception_message = "none";
     public String send_result = "";


     public int stat = 0;
     public int ctr = 0;
     public File myDirectory;
     public File[] myGallery;
     public Context context;

     public int DISEASE_ID = 1;
     public String DOWNLOAD_URL = "";
     public String fn = "";
     public int getCurrentCode;


     public int no_more_img = 0;
     public int no_more_disease_space = 0;
     public int tries = 0;
     public long ave_getting_time = 0;

     public int disease_count_id = 1;
     public int DISEASE_COUNT = 18;


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

          myGallery = new File[DISEASE_COUNT];
          for(int x=0; x<DISEASE_COUNT; x++) {
               myGallery[x] = new File(context.getFilesDir(), "disease_" + (x+1));
               if( !myGallery[x].exists() ) {
                    myGallery[x].mkdirs();
               }
          }

         // Intent myIntent = new Intent(this, LoopService.class);
         // context.startService(myIntent);
         // new Thread(null, send, "SendThread").start();
         new Thread(null, receive_zip, "GetZipThread").start();
     }

     //-----------------------------------------------------------------------------------------------------------------
     //
     //                                                RUNNABLE THREADS
     //
     //------------------------------------------------------------------------------------------------------------------


     Runnable send = new Runnable() {
          @Override
          public void run() {
               while (true) {
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
     };

     Runnable receive_zip = new Runnable() {
          @Override
          public void run() {
               while (true) {
                    try {
                         no_more_img = 0;
                         no_more_disease_space = 0;

                         for(int x=0; x<DISEASE_COUNT; x++) {
                              if (tries == 3) { // after 3 tries of getting internet, timeout for 5 mins
                                   //Thread.sleep(1000 * 60 * 10); // 10 minutes
                                   tries = 0;
                              } else {
                                   if (isNetworkAvailable()) { // if there's net edi wow successful try so back to 0
                                        tries = 0;
                                        ave_getting_time = System.currentTimeMillis();
                                        get_images_from_json(x);
                                        ave_getting_time = System.currentTimeMillis() - ave_getting_time;
                                        //Thread.sleep(10000); // 10 seconds
                                   } else {
                                        // if no net, timeout for 30 seconds increment the number of tries
                                        //Thread.sleep(1000 * 30); // 30 seconds
                                        tries++;
                                   }
                              }
                         }

                         // after scanning through the disease folders, check if either no more space in all folder
                         // or no more pictures to retrieve in all disease,
                         // if yes then,
                         if (no_more_img >= 18 || no_more_disease_space >= 18) {
                              //Thread.sleep(1000 * 60 * 60); // 1 hour
                         }
                    } catch(Exception e) {
                         e.printStackTrace();
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
          /*((TextView)findViewById(R.id.some_textview)).setText(myDirectory.listFiles().length + "\n"
                          + myGallery[0].listFiles().length + " " + myGallery[1].listFiles().length + " "
                          + myGallery[2].listFiles().length + " " + myGallery[3].listFiles().length + " "
                          + myGallery[4].listFiles().length + " " + myGallery[5].listFiles().length + " "
                          + myGallery[6].listFiles().length + " " + myGallery[7].listFiles().length + " "
                          + myGallery[8].listFiles().length + " " + myGallery[9].listFiles().length + " "
                          + myGallery[10].listFiles().length + " " + myGallery[11].listFiles().length + " "
                          + myGallery[12].listFiles().length + " " + myGallery[13].listFiles().length + " "
                          + myGallery[14].listFiles().length + " " + myGallery[15].listFiles().length + " "
                          + myGallery[16].listFiles().length + " " + myGallery[17].listFiles().length + " "
                          + myGallery[18].listFiles().length );*/

          /*ImageView iv = (ImageView) findViewById(R.id.imageViewId);
          iv.setImageBitmap(BitmapFactory.decodeFile(myGallery[0].listFiles()[0].getAbsolutePath()));*/
          ((TextView)findViewById(R.id.some_textview)).setText("disease1_length: " + myGallery[DISEASE_ID-1].listFiles().length
                          + "\nresponded: " + RESPONDED + "\ndisease: " + DISEASE_ID + "\nlabeler: " + VALIDATOR_ID
                          + "\nmid5sum: " + MD5 + "\nsize: " + IMAGE_COUNTS + "\nurl: " + DOWNLOAD_URL  + "\nctr: " + ctr);
     }

     public void deleteAllFiles(View view) {
          File[] xml_files = myDirectory.listFiles();
          for(int x=0; x<xml_files.length; x++) {
               xml_files[x].delete();
          }
     }

     public boolean checkNetworkWifi() {
          ConnectivityManager com = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
          NetworkInfo wifi = com.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

          if( wifi.isAvailable() ) {
               return true;
          }

          return false;
     }

     public boolean checkNetwork3g() {
          ConnectivityManager com = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
          NetworkInfo mobile = com.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

          if( mobile.isAvailable() ) {
               return true;
          }

          return false;
     }

     //------------------------------------------------------------------------------------------------------------------
     //
     //                                  THIS IS FOR SENDING FILES FROM THE LOCAL DATABASE
     //
     //------------------------------------------------------------------------------------------------------------------

     public String Send_File(String urlstr, File current_file) {
          String result = null;
          try {

               HttpHost targetHost = null;
               HttpClient hc = null;
               HttpResponse hr = null;
               String boundary = "-------------" + System.currentTimeMillis();
               HttpPost httpPost = new HttpPost(urlstr);
               httpPost.setHeader("ENCTYPE", "multipart/form-data");
               httpPost.setHeader("Accept", "application/octet-stream");
               httpPost.setHeader("Content-type", "multipart/form-data; boundary=" + boundary);

               HttpParams params = new BasicHttpParams();
               params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
               FileBody fb = new FileBody(current_file, ContentType.APPLICATION_OCTET_STREAM, current_file.getName());

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
     //                          THIS IS FOR UPLOADING FILES TO THE LOCAL DATABASE ( Palitan nung ZIP file )
     //
     //------------------------------------------------------------------------------------------------------------------

     public void download(View view) {
          int x = 0;
          if (myGallery[x].isDirectory()) {
               // url with disease id, number of images and validator id
               // int num = 25;
               // int validator_id = 01
               String JSON_URL = "http://" + HTTP_HOST + ":" + HTTP_PORT + "/api/labeler/get_images?" + "disease_id=" + (x+1) + "&labeler_id=" + VALIDATOR_ID + "&size=" + IMAGE_COUNTS;
               // "http://54.179.135.52/api/labeler/get_images?disease_id=1&labeler_id=2&size=10"

               if (myGallery[x].listFiles().length <= 10) { // Maximum of 10 images
                    //no_more_disease_space = 0;
                    new DownloadTask().execute(JSON_URL);
               }
               else {
                    //no_more_disease_space++;
               }
          }

          Toast.makeText(getApplicationContext(), "Uploaded: " + exception_message, Toast.LENGTH_SHORT).show();
     }


     private class DownloadTask extends AsyncTask<String, Void, String> {
          @Override
          protected String doInBackground(String... params) {
               String json_txt = Get_JSON(params[0]);
               String str = "";
               try {
                    if( json_txt != null ) {
                         JSONObject jsonObject = new JSONObject(json_txt);
                         DISEASE_ID = Integer.parseInt(jsonObject.getString("disease"));
                         DOWNLOAD_URL = jsonObject.getString("url");
                         MD5 = jsonObject.getString("md5sum");
                    }
               } catch(Exception e) {
                    e.printStackTrace();
               } finally {
                   str = "Success";
               }

               str = "nuuuu";
               if ( !DOWNLOAD_URL.equals("") ) {
                    //no_more_img = 0;
                    boolean success = false;
                    String diseaseDir = myGallery[DISEASE_ID-1].getAbsolutePath();
                    File zipfile = Download_Zip(DOWNLOAD_URL, diseaseDir);

                    if( (zipfile != null) && (!isCorrupted(zipfile)) ) { // unzip then save the file to disease number
                         unzip( zipfile.getAbsolutePath() , diseaseDir);
                         success = true;
                    }

                    success_response(success, zipfile, params[0], diseaseDir); // return response and delete the zipfile

                    DOWNLOAD_URL = "";
                    DISEASE_ID = 1;
                    MD5 = "";
                    str = "success";
               }
               else {
                    //no_more_img++;
               }
               return str;
          }

          @Override
          protected void onPostExecute(String result) {
          }
     }

     //------------------------------------------------------------------------------------------------------------------
     //
     //                           THIS IS FOR REQUESTING JSON DATA AND DOWNLOADING IMAGE FROM URL
     //
     //------------------------------------------------------------------------------------------------------------------

     public void get_images_from_json(int x) {
          if (myGallery[x].isDirectory()) {
               // url with disease id, number of images and validator id
               String JSON_URL = "http://" + HTTP_HOST + ":" + HTTP_PORT + "/api/labeler/get_images?" + "disease_id=" + (x+1) + "&labeler_id=" + VALIDATOR_ID + "&size=" + IMAGE_COUNTS;
               // "http://54.179.135.52/api/labeler/get_images?disease_id=1&labeler_id=2&size=10"

               if (myGallery[x].listFiles().length <= 10) { // Maximum of 10 images
                    //no_more_disease_space = 0;

                    String json_txt = Get_JSON(JSON_URL);
                    try {
                         if( json_txt != null ) {
                              JSONObject jsonObject = new JSONObject(json_txt);
                              DISEASE_ID = Integer.parseInt(jsonObject.getString("disease"));
                              DOWNLOAD_URL = jsonObject.getString("url");
                              MD5 = jsonObject.getString("md5sum");
                         }
                    } catch(Exception e) {
                         e.printStackTrace();
                    }

                    if ( !DOWNLOAD_URL.equals("") ) {
                         //no_more_img = 0;
                         boolean success = false;
                         String diseaseDir = myGallery[DISEASE_ID-1].getAbsolutePath();
                         File zipfile = Download_Zip(DOWNLOAD_URL, diseaseDir);

                         if( (zipfile != null) && (!isCorrupted(zipfile)) ) { // unzip then save the file to disease number
                              unzip( zipfile.getAbsolutePath() , diseaseDir);
                              success = true;
                         }

                         success_response(success, zipfile, JSON_URL, diseaseDir); // return response and delete the zipfile

                         DOWNLOAD_URL = "";
                         DISEASE_ID = 1;
                         MD5 = "";
                    }
                    else {
                         //no_more_img++;
                    }
               }
               else {
                    //no_more_disease_space++;
               }
          }
     }

     public String Get_JSON(String urlstr){
          StringBuilder builder = new StringBuilder();
          HttpClient hc = new DefaultHttpClient();
          HttpGet httpGet = new HttpGet(urlstr);
          HttpEntity entity = null;
          BufferedReader reader = null;
          HttpResponse response = null;

          try{
               response = hc.execute(httpGet);
               int statusCode = response.getStatusLine().getStatusCode();

               if(statusCode == HttpStatus.SC_OK){
                    entity = response.getEntity();
                    InputStream is = entity.getContent();
                    reader = new BufferedReader(new InputStreamReader(is));

                    String line;
                    while((line = reader.readLine()) != null){
                         builder.append(line);
                    }
               }

               else {
                    return null;
               }

          } catch(Exception e){
               e.printStackTrace();
               Log.d("InputStream", e.getLocalizedMessage());
          }

          return builder.toString();
     }

     public File Download_Zip( String urlstr,  String targetLocation ) {
          HttpClient hc = new DefaultHttpClient();
          HttpGet httpGet = new HttpGet(urlstr);
          HttpEntity entity = null;
          HttpResponse response = null;
          InputStream is = null;
          File file = null;
          String fn = "zip_imgs.zip";

          try {

               response = hc.execute(httpGet);
               int statusCode = response.getStatusLine().getStatusCode();

               if (statusCode == HttpStatus.SC_OK) {
                    entity = response.getEntity();
                    if (entity != null) {
                         is = entity.getContent();
                         BufferedInputStream bis = new BufferedInputStream(is);

                         file = new File(targetLocation, fn);
                         BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));

                         int inByte;
                         while((inByte = bis.read()) != -1) {
                              bos.write(inByte);
                         }

                         bis.close();
                         bos.close();
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

          return file;
     }

     public boolean isCorrupted( File zipfile ) {
          if ( fileToMD5(zipfile.getAbsolutePath()).equals(MD5) )
               return false;
          else
               return true;
     }

     public String fileToMD5(String filePath) {
          InputStream is = null;
          try {
               is = new FileInputStream(filePath);
               byte[] buffer = new byte[1024];
               MessageDigest digest = MessageDigest.getInstance("MD5");

               int numRead = 0;
               while (numRead != -1) {
                    numRead = is.read(buffer);
                    if (numRead > 0) {
                         digest.update(buffer, 0, numRead);
                    }
               }

               byte[] md5Bytes = digest.digest();

               return convertHashToString(md5Bytes);
          } catch (Exception e) {
               return null;
          } finally {
               if (is != null) {
                    try {
                         is.close();
                    } catch (Exception e) { }
               }
          }
     }

     private String convertHashToString(byte[] md5Bytes) {
          String md5str = "";
          for (int i = 0; i < md5Bytes.length; i++) {
               md5str += Integer.toString(( md5Bytes[i] & 0xff ) + 0x100, 16).substring(1);
          }

          return md5str;
     }

     public void success_response( boolean response, File zipfile, String urlstr, String diseaseDir ) {
          InputStream is = null;
          String result = null;
          try {
               zipfile.delete();

               HttpClient httpclient = new DefaultHttpClient();
               HttpPost httpPost = new HttpPost(urlstr);
               String json = "";

               JSONObject imgList = new JSONObject();
               File imgDir = new File(diseaseDir);
               if (imgDir.isDirectory()) {
                    File[] imgFiles = imgDir.listFiles();
                    if (imgFiles.length > 0) {
                         for( int x=0; x<imgFiles.length; x++) {
                              imgList.put("img" + (x+1), imgFiles[x].getName());
                         }
                    }
               }

               JSONObject jsonResponse = new JSONObject();
               jsonResponse.put("validator_id", VALIDATOR_ID);
               jsonResponse.put("disease_id", DISEASE_ID);
               jsonResponse.put("success", response);
               jsonResponse.put("received", imgList);

               json = jsonResponse.toString();
               StringEntity se = new StringEntity(json);

               httpPost.setEntity(se);
               httpPost.setHeader("Accept", "application/json");
               httpPost.setHeader("Content-type", "application/json");


               HttpResponse httpResponse = httpclient.execute(httpPost);
               is = httpResponse.getEntity().getContent();


               if (httpResponse != null) {
                    /*BasicResponseHandler responseHandler = new BasicResponseHandler();
                    result = responseHandler.handleResponse(httpResponse);*/
                    int statusCode = httpResponse.getStatusLine().getStatusCode();

                    if (statusCode == HttpStatus.SC_OK)
                         RESPONDED = "Success"; //convertInputStreamToString(is);
                    else
                         RESPONDED = "response: " + statusCode;
               }
               else {
                    RESPONDED = "Did not work!";
               }


          } catch (Exception e) {
               Log.d("InputStream", e.getLocalizedMessage());
          }
     }

     public void unzip( String zipFile, String targetLocation ) {

          try {
               FileInputStream fin = new FileInputStream(zipFile);
               ZipInputStream zin = new ZipInputStream(fin);
               ZipEntry ze = null;

               while ((ze = zin.getNextEntry()) != null) {
                    if(ze.isDirectory()) {
                         continue;
                    }

                    FileOutputStream fout = new FileOutputStream(targetLocation + File.separator + ze.getName());
                    int c = zin.read();

                    while(c != -1) {
                         fout.write(c);
                         c = zin.read();
                    }

                    zin.closeEntry();
                    fout.close();
               }

               zin.close();
          } catch (Exception e) {
               System.out.println(e);
          }
     }

     public void loadCountFile() {

          TextView tv = (TextView)findViewById(R.id.file_contents);
          DiseaseCountFile diseaseCountFile = new DiseaseCountFile(getApplicationContext());

          StringBuilder sb = new StringBuilder();
          for (int i = 0; i<DISEASE_COUNT; i++) {
               int count = diseaseCountFile.disease_counts.get(i);
               if (i != 0) sb.append("\n");
               sb.append("[" + (i+1) + "]: " + count);
          }

          tv.setText(sb.toString());

     }

     public void addCount(View view) {

          DiseaseCountFile diseaseCountFile = new DiseaseCountFile(getApplicationContext());
          diseaseCountFile.incrementCount(disease_count_id);

          disease_count_id++;
          if (disease_count_id > DISEASE_COUNT) disease_count_id = 1;

          loadCountFile();

     }

}
