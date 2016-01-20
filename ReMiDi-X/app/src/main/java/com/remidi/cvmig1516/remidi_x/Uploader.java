package com.remidi.cvmig1516.remidi_x;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.View;

import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
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

import java.io.File;
import java.util.Arrays;

/**
 * Created by Abbey on 18/01/2016.
 */

public class Uploader {

     File myDirectory;
     Context context;
     String HTTP_HOST;
     int HTTP_PORT;
     String HOME;
     int disease;

     public boolean hasNet = false;
     public boolean isThreadPause = false;
     public int stat = 0;
     String sent_filename;
     String send_result;
     int getCurrentCode = -1;

     Uploader(Context context, File directory, int disease, String host, int port, String home) {

          this.context = context;
          this.disease = disease;
          this.myDirectory = directory;
          this.HTTP_HOST = host;
          this.HTTP_PORT = port;
          this.HOME = home;

          Runnable send = new Runnable() {
               @Override
               public void run() {
                    while (true) {
                         if (!isThreadPause) {
                              //String urlstr = HTTP_HOST + HOME;
                              String urlstr = "http://" + HTTP_HOST + ":" + HTTP_PORT + HOME;
                              //String urlstr = "http://" + HTTP_HOST + ":" + HTTP_PORT + HOME;
                              //String urlstr = "http://" + HTTP_HOST + ":" + HTTP_PORT + HOME;
                              if (myDirectory.isDirectory()) {
                                   File[] xml_files = myDirectory.listFiles();
                                   if ((xml_files.length > 0) && (isNetworkAvailable())) {
                                        hasNet = true;
                                        Arrays.sort(xml_files, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
                                        sent_filename = xml_files[0].getAbsolutePath();
                                        send_result = Send_File(urlstr, xml_files[0]);
                                   }
                              }
                         }
                    }
               }
          };

          new Thread(null, send, "SendThread").start();

     }

     public String uploadFile(File file, String filename, boolean isZipFile) {
          String msg;
          try {
               if (isZipFile) file.renameTo(new File(myDirectory.getAbsolutePath() + "/" + disease + "-" + file.getName()));
               else file.renameTo(new File(myDirectory.getAbsolutePath() + "/" + disease + "-" + filename));
               String filetype;
               if (isZipFile) filetype = "zip";
               else filetype = "xml";
               msg = "Sent image diagnosis!";
          } catch (Exception e) {
               msg = "Exception occurred: " + e.getMessage();
          }

          return msg;
     }

     public boolean isNetworkAvailable() {
          ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
          NetworkInfo ani = cm.getActiveNetworkInfo();
          return (ani != null && ani.isConnected());
     }

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

     public void deleteAllFiles(View view) {
          File[] xml_files = myDirectory.listFiles();
          for(int x=0; x<xml_files.length; x++) {
               xml_files[x].delete();
          }
     }

     public void setIsPaused(boolean isPaused) {
          this.isThreadPause = isPaused;
     }

}
