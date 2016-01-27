package com.remidi.cvmig1516.remidi_x;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;

import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * Created by Abbey on 21/01/2016.
 */
public class LoopService extends Service {

     // Local url
     //public String HTTP_HOST = "192.168.1.9";
     //public String HTTP_HOME = "/data/";
     //public int HTTP_PORT = 5000;

     // Web url
     public String HTTP_HOST = "54.179.135.52";
     public String HTTP_HOME = "/api/label/";
     public int HTTP_PORT = 80;

     public String send_result = "";

     public File myDirectory;
     public File[] myGallery;
     public Context context;

     public int IMG_ID = 0;
     public int DISEASE_ID = 0;
     public String DOWNLOAD_URL = "";
     public String fn = "";
     public int getCurrentCode;

//------------------------------------------------------------------------------------------------------------------
//
//                                             SERVICE METHODS TO OVERRIDE
//
//------------------------------------------------------------------------------------------------------------------
     @Override
     public void onCreate() {
          super.onCreate();
     }

     @Override
     public IBinder onBind(Intent intent) {
          return null;
     }

     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {

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

          new Thread(null, receive_img, "ReceiveImageThread").start();
         // new Thread(null, send, "SendThread").start();

          super.onStartCommand(intent, flags, startId);
          return 0;
     }

     @Override
     public void onDestroy() {
          super.onDestroy();
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

     Runnable receive_img = new Runnable() {
          @Override
          public void run() {
               while (true) {
                    for(int x=0; x<19; x++) {
                         get_image_from_json(x);
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
     //                           THIS IS FOR REQUESTING JSON DATA AND DOWNLOADING IMAGE FROM URL
     //
     //------------------------------------------------------------------------------------------------------------------

     public void get_image_from_json(int x) {
          if (myGallery[x].isDirectory()) {
               String JSON_URL = "http://" + HTTP_HOST + ":" + HTTP_PORT + "/api/get_img_info/" + (x+1) + "/";

               if ((myGallery[x].listFiles().length < 10) && (isNetworkAvailable())) {
                    String json_txt = Get_JSON(JSON_URL);
                    try {
                         if( json_txt != null ) {
                              JSONObject jsonObject = new JSONObject(json_txt);
                              IMG_ID = Integer.parseInt(jsonObject.getString("img"));
                              DISEASE_ID = Integer.parseInt(jsonObject.getString("disease"));
                              DOWNLOAD_URL = jsonObject.getString("img_url");
                         }
                    } catch(Exception e) {
                         e.printStackTrace();
                    } finally {
                         System.out.println("Success");
                    }

                    if ( !DOWNLOAD_URL.equals("") ) {
                         Bitmap result = Download_Image(DOWNLOAD_URL);

                         File f;
                         FileOutputStream fop;
                         String diseaseDir = myGallery[DISEASE_ID-1].getAbsolutePath();
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

                         DOWNLOAD_URL = "";
                         IMG_ID = 0;
                         DISEASE_ID = 0;
                    }
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
}
