package com.remidi.cvmig1516.remidi_x;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
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

     public static final String FILE_NAME = "testfile.xml";
     public String HTTP_HOST = "192.168.1.26";
     public String HOME = "/data/";
     public int HTTP_PORT = 5000;
     public int ctr = 0;
     public String exception_message = "";

     XMLFileHandler xmlfh = null;

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
          xmlfh.write("laman");
          field.setText("");
          new UploadTask().execute("http://" + HTTP_HOST + ":" + HTTP_PORT + HOME);

     }

     public String Upload_Data(String urlstr, String filepath) {
          String result = null;
          try {
               HttpClient hc = new DefaultHttpClient();
               HttpPost httpPost = new HttpPost(urlstr);
               httpPost.setHeader("ENCTYPE", "multipart/form-data");
               String boundary = "-------------" + System.currentTimeMillis();
               httpPost.setHeader("Content-type", "multipart/form-data; boundary=" + boundary);

               HttpParams params = new BasicHttpParams();
               params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

               File file = new File(filepath);
               FileBody fb = new FileBody(file, ContentType.APPLICATION_XML, FILE_NAME);
               MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create();
               multipartEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
               multipartEntity.setBoundary(boundary);
               multipartEntity.addPart("uploaded_file", fb);
               HttpEntity he = multipartEntity.build();
               httpPost.setEntity(he);

               HttpResponse hr = hc.execute(httpPost);
               if (hr != null) {
                    BasicResponseHandler responseHandler = new BasicResponseHandler();
                    result = responseHandler.handleResponse(hr);
               } else {
                    result = "Didn't work!";
               }
          } catch (Exception e) {
               e.printStackTrace();
               Log.d("OutputStream", e.getLocalizedMessage());
          }
          return result;
     }
     private class UploadTask extends AsyncTask<String, Void, String> {
          @Override
          protected String doInBackground(String... params) {
               return Upload_Data(params[0], xmlfh.filepath);
          }
          @Override
          protected void onPostExecute(String result) {
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

               HttpURLConnection conn = null;
               DataOutputStream os = null;

               String urlServer = "http://" + HTTP_HOST + ":" + HTTP_PORT + HOME;

               String lineEnd = "\r\n";
               String twoHyphens = "--";
               String boundary =  "*****";
               int bytesRead, bytesAvailable, bufferSize, bytesUploaded = 0;
               byte[] buffer;
               int maxBufferSize = 1024*1024;

               String uploadname = filename.substring(0,filename.length()-5);

               try {
                    File file = new File(filepath);
                    FileInputStream fis = new FileInputStream(file);
                    URL url = new URL(urlServer);
                    conn = (HttpURLConnection) url.openConnection();

                    // POST settings.
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary="+boundary);
                    conn.setRequestProperty("uploaded_file", filename);
                    conn.setRequestProperty("Content-Length",String.valueOf(file.length()));

                    os = new DataOutputStream(conn.getOutputStream());
                    os.writeBytes(twoHyphens + boundary + lineEnd);
                    os.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + uploadname +"\"" + lineEnd);
                    os.writeBytes("Content-Type: " + HttpURLConnection.guessContentTypeFromName(filepath) + lineEnd);
                    os.writeBytes(lineEnd);

                    bytesAvailable = fis.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    bytesRead = fis.read(buffer, 0, bufferSize);
                    bytesUploaded += bytesRead;

                    while (bytesRead > 0)
                    {
                         os.write(buffer, 0, bufferSize);
                         bytesAvailable = fis.available();
                         bufferSize = Math.min(bytesAvailable, maxBufferSize);
                         buffer = new byte[bufferSize];
                         bytesRead = fis.read(buffer, 0, bufferSize);
                         bytesUploaded += bytesRead;
                    }

                    os.writeBytes(lineEnd);
                    os.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    // Responses from the server (code and message)

                    conn.setConnectTimeout(10000); // allow 10 seconds timeout.
                    int rcode = conn.getResponseCode();
                    if (rcode == 200) exception_message = rcode + ": Success!!!";
                    else  exception_message = rcode + ": Failed!!!";

                    fis.close();
                    os.flush();
                    os.close();

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
          }

          @Override
          protected void onPreExecute() {
          }
     }



}
