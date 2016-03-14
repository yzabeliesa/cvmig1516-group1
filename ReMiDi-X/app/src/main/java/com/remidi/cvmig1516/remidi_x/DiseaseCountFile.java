package com.remidi.cvmig1516.remidi_x;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * Created by Abbey on 27/02/2016.
 */
public class DiseaseCountFile {

     final int DISEASE_COUNT = 19;
     String filename;
     String filepath;
     String filefolder;
     File file;
     ArrayList<Integer> disease_counts;

     public DiseaseCountFile(Context context) {

          this.disease_counts = new ArrayList<>();

          filename = "disease-count.txt";
          filefolder = context.getFilesDir().toString();
          filepath =  filefolder + "/" + filename;
          file = new File(filefolder, filename);
          try {
               Log.d("FILE PROCESS", "CHECKING NEW FILE");
               if (!file.exists()) {
                    Log.d("FILE PROCESS", "CREATED NEW FILE");
                    file.createNewFile();
                    for (int i = 0; i<DISEASE_COUNT; i++) {
                         if (i == 0) this.append("0");
                         else this.append("|0");
                    }
               }
               for (int i = 0; i<DISEASE_COUNT; i++) {
                    this.disease_counts.add(0);
               }
               this.loadFileToArrayList();
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

     public void loadFileToArrayList() {
          String contents = readContents();
          StringTokenizer tokens = new StringTokenizer(contents, "|");

          for (int i = 0; i<DISEASE_COUNT; i++) {
               disease_counts.set(i, Integer.parseInt(tokens.nextToken()));
          }
     }

     public boolean incrementCount(int disease_num) {

          boolean successful = false;
          write("");

          for (int i = 0; i<DISEASE_COUNT; i++) {
               int count = disease_counts.get(i);
               if (i+1 == disease_num) {
                    count++;
                    disease_counts.set(i,count);
                    successful = true;
               }
               if (i == 0) append(count + "");
               else append("|" + count);
          }

          loadFileToArrayList();
          return successful;

     }

}
