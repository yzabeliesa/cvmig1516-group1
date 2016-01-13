package com.remidi.cvmig1516.remidi_x;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Created by Abbey on 18/12/2015.
 */

public class XMLFileHandler {

     String filename;
     String filepath;
     File file;

     public XMLFileHandler(Context context, String fn) {
          filename = fn;
          filepath = context.getFilesDir() + "/" + filename;
          file = new File(context.getFilesDir(), filename);
          try {
               Log.d("FILE PROCESS", "CHECKING NEW FILE");
               if (!file.exists()) {
                    Log.d("FILE PROCESS", "CREATED NEW FILE");
                    file.createNewFile();
               }
          } catch (IOException e) {
               Log.d("FILE ERROR", "Could not create Test file!");
          }

     }

     public XMLFileHandler(Context context, String fn, String initMessage) {

          new XMLFileHandler(context,fn);
          write(initMessage);

     }

     public String readContents() {

          StringBuilder sb = new StringBuilder();
          Scanner fileScanner = null;

          try {
               fileScanner = new Scanner(file);
          } catch (FileNotFoundException e) {
               Log.d("FILE ERROR", "Could not find Test file for scanner.");
          }

          while (fileScanner.hasNextLine()) sb.append(fileScanner.nextLine());

          return sb.toString();

     }

     public void write(String toWrite) {

          FileOutputStream fos = null;
          try {
               fos = new FileOutputStream(file, false);
          } catch(FileNotFoundException e) {
               Log.d("FILE ERROR", "Cannot find output file for writing!");
          }

          PrintWriter out = new PrintWriter(fos);
          out.print(toWrite);
          out.flush();
          out.close();

     }

     public void append(String toWrite) {

          toWrite = readContents() + toWrite;
          write(toWrite);

     }

}
