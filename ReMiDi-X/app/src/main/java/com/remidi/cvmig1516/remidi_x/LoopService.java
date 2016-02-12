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
     public int VALIDATOR_ID = 1;

     public String send_result = "";

     public File myDirectory;
     public File[] myGallery;
     public Context context;

     public int IMG_ID = 0;
     public int DISEASE_ID = 0;
     public String DOWNLOAD_URL = "";
     public String MD5 = "";
     public int IMAGE_COUNTS = 5;
     public String fn = "";
     public int getCurrentCode;

     public int no_more_img = 0;
     public int no_more_disease_space = 0;
     public int tries = 0;
     public long ave_getting_time = 0;

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
          new Thread(null, send, "SendThread").start();

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
                    no_more_img = 0;
                    no_more_disease_space = 0;
                    
                    for(int x=0; x<19; x++) {
                         if( tries == 3 ) { // after 3 tries of getting internet, timeout for 5 mins
                              Thread.sleep(1000 * 60 * 10); // 10 minutes
                              tries = 0;
                         }
                         else {
                              if( isNetworkAvailable() ) { // if there's net edi wow successful try so back to 0
                                   tries = 0;
                                   ave_getting_time = System.currentTimeMillis();
                                   get_images_from_json(x);
                                   ave_getting_time = System.currentTimeMillis() - ave_getting_time;
                                   Thread.sleep(10000); // 10 seconds
                              }

                              else {
                                   // if no net, timeout for 30 seconds increment the number of tries
                                   Thread.sleep(1000 * 30); // 30 seconds
                                   tries++;
                              }
                         }
                    }

                    // after scanning through the disease folders, check if either no more space in all folder 
                    // or no more pictures to retrieve in all disease, 
                    // if yes then, 
                    if( no_more_img >= 18  ||  no_more_disease_space >= 18 ) {  
                         Thread.sleep(1000 * 60 * 60); // 1 hour
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

     public boolean checkNetworkWifi() {
        ConnectivityManager com = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if( wifi.isAvailable() ) {
            return true;
        }

        return false;
     }

     public boolean checkNetwork3g() {
        ConnectivityManager com = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

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
     //                           THIS IS FOR REQUESTING JSON DATA AND DOWNLOADING IMAGE FROM URL
     //
     //------------------------------------------------------------------------------------------------------------------

     public void get_images_from_json(int x) {
          if (myGallery[x].isDirectory()) {
               // url with disease id
               String JSON_URL = "http://" + HTTP_HOST + ":" + HTTP_PORT + "/api/get_img_info/" + (x+1) + "/";

               // url with disease id, number of images and validator id
               // int num = 25;
               // int validator_id = 01
               // String JSON_URL = "http://" + HTTP_HOST + ":" + HTTP_PORT + "/api/get_img_info?" + "disease_id=" + (x+1) + "&labeler_id=" + VALDATOR_ID + "&size=" + IMAGE_COUNTS;
               // "http://54.179.135.52/api/labeler/get_images?disease_id=1&labeler_id=2&size=10"

               if (myGallery[x].listFiles().length <= 10) { // Maximum of 10 images
                    no_more_disease_space = 0;

                    String json_txt = Get_JSON(JSON_URL);
                    try {
                         if( json_txt != null ) {
                              JSONObject jsonObject = new JSONObject(json_txt);
                              IMG_ID = Integer.parseInt(jsonObject.getString("img")); // will be removed
                              DISEASE_ID = Integer.parseInt(jsonObject.getString("disease"));
                              DOWNLOAD_URL = jsonObject.getString("img_url");
                              // MD5 = jsonObject.getString("md5sum");
                         }
                    } catch(Exception e) {
                         e.printStackTrace();
                    } finally {
                         System.out.println("Success");
                    }

                    if ( !DOWNLOAD_URL.equals("") ) {
                         no_more_img = 0;
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
                    else {
                         no_more_img++;
                    }

                    /*
                    if ( !DOWNLOAD_URL.equals("") ) {
                         no_more_img = 0;
                         boolean success = false;
                         String diseaseDir = myGallery[DISEASE_ID-1].getAbsolutePath();
                         File zipfile = Download_Zip(DOWNLOAD_URL, diseaseDir);

                         if( !isCorrupted(zipFile) ) { // unzip then save the file to disease number
                              unzip( zipfile.getAbsolutePath() , diseaseDir);
                              success = true;
                         }
                         
                         success_response(success, zipfile, DOWNLOAD_URL, diseaseDir); // return response and delete the zipfile

                         DOWNLOAD_URL = "";
                         DISEASE_ID = 0;
                         MD5 = "";
                    }
                    else {
                         no_more_img++;
                    }
                    */
               }
               else {
                    no_more_disease_space++;
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

     public File Download_Zip( String urlstr,  String targetLocation ) {
          HttpClient hc = new DefaultHttpClient();
          HttpGet httpGet = new HttpGet(urlstr);
          HttpEntity entity = null;
          HttpResponse response = null;
          InputStream is = null;
          File file = null;
          filename = "zip_imgs.zip"

          try {

               response = hc.execute(httpGet);
               int statusCode = response.getStatusLine().getStatusCode();

               if (statusCode == HttpStatus.SC_OK) {
                    entity = response.getEntity();
                    if (entity != null) {
                         is = entity.getContent();
                         BufferedInputStream bis = new BufferedInputStream(is);

                         file = new File(targetLocation, filename);
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
          String result = "";
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
                         for( int x=0; x<imgFiles.length; x++ ) {
                              imgList.put(imgFiles[x].getName());
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
               inputStream = httpResponse.getEntity().getContent();
 

               if(inputStream != null)
                    result = convertInputStreamToString(inputStream);
               else
                    result = "Did not work!";
 
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

                    //create dir if required while unzipping
                    /*
                    // skipping the directories

                    if(ze.isDirectory()) { 
                         continue; 
                    }
                    
                    */

                    FileOutputStream fout = new FileOutputStream(targetLocation + ze.getName());
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
}
