package com.remidi.cvmig1516.remidi_x;

/*
FUCKING DEPENDENCIES:
compile 'org.apache.httpcomponents:httpclient:4.5.1'
compile 'org.apache.httpcomponents:httpclient-osgi:4.5.1'

*/

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class TestActivity extends ActionBarActivity {

     public static final String FILE_NAME = "testfile.txt";
     public String HTTP_HOST = "192.168.1.26";
     public String HOME = "/data/";
     public int HTTP_PORT = 5000;
     public int ctr = 0;
     public String exception_message = "";

     XMLFileHandler xmlfh = null;
     /**
      * ATTENTION: This was auto-generated to implement the App Indexing API.
      * See https://g.co/AppIndexing/AndroidStudio for more information.
      */

     @Override
     protected void onCreate(Bundle savedInstanceState) {

          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_test);
          xmlfh = new XMLFileHandler(getApplicationContext(), FILE_NAME);
          xmlfh.readContents();

     }

     public void readFile(View view) {

          TextView text = (TextView) findViewById(R.id.file_contents);
          text.setText(xmlfh.readContents());

     }

     public void writeToFile(View view) {

          EditText field = (EditText) findViewById(R.id.test_message);
          String toWrite = field.getText().toString();
          field.setText("");
          xmlfh.append(toWrite);
          //readFile(view);
          new UploadTask().execute("http://" + HTTP_HOST + ":" + HTTP_PORT + HOME);

     }

     public String Upload_Data(String urlstr, String filepath) {
          String result = null;
          try {
               // /*
               HttpClient hc = new DefaultHttpClient();
               HttpPost httpPost = new HttpPost(urlstr);
               httpPost.addHeader("Accept", "text/xml");
               httpPost.addHeader("Content-Type", "application/xml");

               HttpParams params = new BasicHttpParams();
               params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
               //DefaultHttpClient mHttpClient = new DefaultHttpClient(params);

               String boundary = "-------------" + System.currentTimeMillis();
               MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create();
               multipartEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
               multipartEntity.setBoundary(boundary);
               //multipartEntity.addPart("chunk_id", new StringBody(String.valueOf(0), ContentType.TEXT_PLAIN)); //Chunk Id used for identification.
               multipartEntity.addPart("uploaded_file", new FileBody(new File(filepath),ContentType.APPLICATION_XML));
               httpPost.setEntity(multipartEntity.build());

               HttpHost targetHost = new HttpHost(HTTP_HOST, HTTP_PORT, "http");
               HttpResponse hr = hc.execute(targetHost, httpPost);

               if (hr != null) {
                    BasicResponseHandler responseHandler = new BasicResponseHandler();
                    result = responseHandler.handleResponse(hr);
               } else {
                    result = "Didn't work!";
               }
               // */
               /* <---- this is commented out
               final int cSize = 1024 * 1024; // size of chunk
               File file = new File(filepath);
               final long pieces = file.length()/cSize; // used to return file length.

               HttpPost httpPost = new HttpPost(urlstr);
               httpPost.addHeader("Accept", "text/xml");
               httpPost.addHeader("Content-Type", "application/xml");

               BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file));

               for (int i= 0; i< pieces; i++) {
                    byte[] buffer = new byte[cSize];

                    if(stream.read(buffer) ==-1)
                         break;

                    MultipartEntityBuilder entity = MultipartEntityBuilder.create();
                    entity.addPart("chunk_id", new StringBody(String.valueOf(i), ContentType.TEXT_PLAIN)); //Chunk Id used for identification.
                    httpPost.setEntity(entity.build());
                    ByteArrayInputStream arrayStream = new ByteArrayInputStream(buffer);

                    entity.addPart("file_data", new InputStreamBody(arrayStream, xmlfh.filename));


                    HttpHost targetHost = new HttpHost(HTTP_HOST, HTTP_PORT, "http");
                    HttpClient hc = new DefaultHttpClient();
                    HttpResponse hr = hc.execute(targetHost, httpPost);

                    if (hr != null) {
                         BasicResponseHandler responseHandler = new BasicResponseHandler();
                         result = responseHandler.handleResponse(hr);
                    } else {
                         result = "Didn't work!";
                    }
                    ((TextView)findViewById(R.id.exception_text)).setText(result + ": " + i);

               } */
               ((TextView)findViewById(R.id.exception_text)).setText(result);

          } catch (Exception e) {
               e.printStackTrace();
               Log.d("OutputStream", e.getLocalizedMessage());
          }
          Toast.makeText(getApplicationContext(), "Sent:\n" + result, Toast.LENGTH_LONG).show();
          return result;
     }

     public String make_xml() {

          StringBuilder xmlfile = new StringBuilder();
          xmlfile.append("<?xml version='1.0' encoding='us-ascii'?> \n");
          xmlfile.append("<!--  A SAMPLE --> \n");
          xmlfile.append("<displayName> My Message </displayName> \n");
          xmlfile.append("<msg> " + xmlfh.readContents() + " </msg> \n");
          return xmlfile.toString();

     }

     private class UploadTask extends AsyncTask<String, Void, String> {
          @Override
          protected String doInBackground(String... params) {
               return Upload_Data(params[0], xmlfh.filepath);
          }

          @Override
          protected void onPostExecute(String result) {
               //Toast.makeText(getApplicationContext(), "Sent:\n" + result, Toast.LENGTH_LONG).show();
               //textView.setText(result);
          }
     }

     // ----------------------------------*******************---------------------------------------
     // try this
     public void doUpload(View view) {

          new Uploader().execute("");

     }

     public void getStatus(View view) {
          ((TextView)findViewById(R.id.some_textview)).setText("Ended here: " + ctr);
          ((TextView)findViewById(R.id.exception_text)).setText(exception_message);
          exception_message = "No exception";
          ctr = 0;
     }

     private class Uploader extends AsyncTask <String, Void, Boolean> {

          @Override
          protected Boolean doInBackground(String... params) {
               String filename = xmlfh.filename;
               String filepath = xmlfh.filepath;
               ctr++; //1
               HttpURLConnection conn = null;
               DataOutputStream os = null;
               //DataInputStream inputStream = null;

               String urlServer = "http://" + HTTP_HOST + ":" + HTTP_PORT + HOME;

               String lineEnd = "\r\n";
               String twoHyphens = "--";
               String boundary =  "*****";
               int bytesRead, bytesAvailable, bufferSize, bytesUploaded = 0;
               byte[] buffer;
               int maxBufferSize = 1024*1024;

               String uploadname = filename.substring(0,filename.length()-5);

               try {
                    ctr++; //2
                    File file = new File(filepath);
                    FileInputStream fis = new FileInputStream(file);
                    ctr++; //3
                    ctr++; //4
                    URL url = new URL(urlServer);
                    ctr++; //5
                    conn = (HttpURLConnection) url.openConnection();
                    ctr++; //6
                    conn.setChunkedStreamingMode(maxBufferSize);
                    ctr++; //7

                    // POST settings.
                    conn.setDoInput(true);
                    ctr++; //8
                    conn.setDoOutput(true);
                    ctr++; //9
                    conn.setUseCaches(false);
                    ctr++; //10
                    conn.setRequestMethod("POST");
                    ctr++; //11
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    ctr++; //12
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    ctr++; //13
                    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary="+boundary);
                    ctr++; //14
                    conn.setRequestProperty("uploaded_file", filename);
                    conn.setRequestProperty("Content-Length",String.valueOf(file.length()));
                    ctr++; //15
                    //conn.addRequestProperty("username", Username);
                    //conn.addRequestProperty("password", Password);
                    //conn.connect();
                    ctr++; //16

                    os = new DataOutputStream(conn.getOutputStream());
                    ctr++; //17
                    os.writeBytes(twoHyphens + boundary + lineEnd);
                    ctr++; //18
                    os.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + uploadname +"\"" + lineEnd);
                    ctr++; //19
                    os.writeBytes(lineEnd);
                    ctr++; //20

                    bytesAvailable = fis.available();
                    ctr++; //21
                    //System.out.println("available: " + String.valueOf(bytesAvailable));
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    ctr++; //22
                    buffer = new byte[bufferSize];
                    ctr++; //23

                    bytesRead = fis.read(buffer, 0, bufferSize);
                    ctr++;//24
                    bytesUploaded += bytesRead;
                    ctr++;//25
                    while (bytesRead > 0)
                    {
                         os.write(buffer, 0, bufferSize);
                         bytesAvailable = fis.available();
                         bufferSize = Math.min(bytesAvailable, maxBufferSize);
                         buffer = new byte[bufferSize];
                         bytesRead = fis.read(buffer, 0, bufferSize);
                         bytesUploaded += bytesRead;
                    }
                    ctr++; //26
                    //System.out.println("uploaded: "+String.valueOf(bytesUploaded));
                    os.writeBytes(lineEnd);
                    ctr++; //27
                    os.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                    ctr++; //28

                    // Responses from the server (code and message)
                    conn.setConnectTimeout(10000); // allow 10 seconds timeout.
                    ctr++; //29
                    int rcode = conn.getResponseCode();
                    ctr++; //30
                    if (rcode == 200) exception_message = "Success!!!";
                    else  exception_message = "Failed!!!";

                    ctr++; //31
                    fis.close();
                    ctr++; //32
                    os.flush();
                    ctr++; //33
                    os.close();
                    ctr++; //34
                    //Toast.makeText(getApplicationContext(), "Record Uploaded!", Toast.LENGTH_LONG).show();
                    //ctr++; //35
               }
               catch (Exception ex) {
                    ctr+=1000;
                    exception_message = ex.toString();
                    return false;
               }
               return true;
          }

          @Override
          protected void onPostExecute(Boolean result) {
               //Toast.makeText(getApplicationContext(), "Sent:\n" + result, Toast.LENGTH_LONG).show();
               //textView.setText(result);
               ctr+=100;
          }

          @Override
          protected void onPreExecute() {
               //ctr+=100;
          }
     }



}
