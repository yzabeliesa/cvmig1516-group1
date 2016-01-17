package com.remidi.cvmig1516.remidi_x;

import android.content.Context;
import android.provider.MediaStore;
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
     String filefolder;
     File file;

     public XMLFileHandler(Context context, String name, String disease, boolean isTextData) {
          if (isTextData) {
               filename = "textData.xml";
               filefolder = context.getFilesDir() + "/" + disease + "/" + name; // name = imageno_patch
               File folder = new File(filefolder);
               if (!folder.exists()) folder.mkdirs();
          }
          else {
               filename = name; // name = progress_file
               filefolder = context.getFilesDir() + "/" + disease;
               File folder = new File(filefolder);
               if (!folder.exists()) folder.mkdir();
          }

          filepath =  filefolder + "/" + filename;
          file = new File(filefolder, filename);
          try {
               Log.d("FILE PROCESS", "CHECKING NEW FILE");
               if (!file.exists()) {
                    Log.d("FILE PROCESS", "CREATED NEW FILE");
                    file.createNewFile();
               }
          } catch (IOException e) {
               Log.d("FILE ERROR", "Could not create text file!");
          }

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

     public void delete() {
          file.delete();
     }

     public void deleteFolder() {
          File folder = new File(filefolder);
          folder.delete();
     }

}
