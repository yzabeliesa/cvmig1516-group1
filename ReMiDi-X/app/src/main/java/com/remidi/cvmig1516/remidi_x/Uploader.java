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
     int diseasenum;

     int getCurrentCode = -1;

     Uploader(Context context, File directory, int disease, String host, int port, String home) {

          this.context = context;
          this.diseasenum = disease;
          this.myDirectory = directory;
          this.HTTP_HOST = host;
          this.HTTP_PORT = port;
          this.HOME = home;

     }

     public String uploadFile(File file, String filename, boolean isZipFile) {
          String msg;
          try {
               if (isZipFile) file.renameTo(new File(myDirectory.getAbsolutePath() + "/" + diseasenum + "-" + file.getName()));
               else file.renameTo(new File(myDirectory.getAbsolutePath() + "/" + diseasenum + "-" + filename));
               String filetype;
               if (isZipFile) filetype = "zip";
               else filetype = "xml";
               msg = "Sent image diagnosis!";
          } catch (Exception e) {
               msg = "Exception occurred: " + e.getMessage();
          }

          return msg;
     }

}
