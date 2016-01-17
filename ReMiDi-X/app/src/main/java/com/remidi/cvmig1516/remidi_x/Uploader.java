package com.remidi.cvmig1516.remidi_x;

import android.content.Context;
import java.io.File;

/**
 * Created by Abbey on 18/01/2016.
 */

public class Uploader {

     File myDirectory;

     Uploader(File directory) {
          this.myDirectory = directory;
     }

     public String uploadFile(File file) {
          String msg;
          try {
               file.renameTo(new File(myDirectory + "/" + file.getName()));
               msg = "Sent image diagnosis!";
          } catch (Exception e) {
               msg = "Exception occurred: " + e.getMessage();
          }

          return msg;
     }

}
