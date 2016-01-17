package com.remidi.cvmig1516.remidi_x;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class XMLTest extends ActionBarActivity {

     XMLFileHandler progress_file;
     int current_image = 0;

     final int PATCH_NEUTRAL = 0;
     final int PATCH_COMPLETE = 1;
     final int PATCH_INCOMPLETE = 2;

     ArrayList<Patch> patches = new ArrayList<>();
     String disease = "Disease";
     Context context;

     @Override
     protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_xmltest);
          context = getApplicationContext();
          /*
          Bundle extras = getIntent().getExtras();

          if (extras != null) {
               ((TextView)findViewById(R.id.xmltester)).setText(extras.getString("XML Data"));
          }
          */
          progress_file = new XMLFileHandler(context,"progress.txt",disease,false);
          patches.add(new Patch(context, current_image, 0, 1, 2, 3, 4, disease));
          patches.add(new Patch(context, current_image, 1, 1, 2, 3, 4, disease));
          patches.add(new Patch(context,current_image,2,1,2,3,4,disease));
          patches.add(new Patch(context,current_image,3,1,2,3,4,disease));
          patches.add(new Patch(context,current_image,4,1,2,3,4,disease));
          patches.get(2).remarks="hehehe";
          patches.get(3).analysis.add("jajejejeje");
          patches.get(3).analysis.add("hohohohoho");
          updateProgress();

          String imageFolder = progress_file.filefolder;
          String zipPath = progress_file.filefolder + "/img" + patches.get(0).formatImgno() + ".zip";
          createZipfile(imageFolder, zipPath);
          uploadZipfile(imageFolder);

     }

     public void updateProgress() {
          // updates current image file as well as patches created in image
          /* PROTOCOL:
          current_image$x1|y1|x2|y2|Species (comma-separated)|Remarks$x1|y1|x2|y2|Species (comma-separated)|Remarks
          */
          progress_file.write(current_image + "");
          for (int i = 0; i<patches.size(); i++) {
               Patch patch = patches.get(i);
               progress_file.append("$");
               progress_file.append(patch.x1 + "|" + patch.y1 + "|" + patch.x2 + "|" + patch.y2 + "|");
               for (int j = 0; j<patch.analysis.size(); j++) {
                    if (j>0) progress_file.append(",");
                    progress_file.append(patch.analysis.get(j));
               }
               if (patch.analysis.size() == 0) progress_file.append("###*No data*###");
               progress_file.append("|" + patch.remarks);
               if (patch.remarks.equals("")) progress_file.append("###*No data*###");
          }

     }

     public void uploadZipfile(String imageFolder) {

          Toast.makeText(context, "Sent image diagnosis!", Toast.LENGTH_SHORT).show();
          for (int i = 0; i<patches.size(); i++) {
               Patch patch = patches.get(i);
               //patch.delete();
               //patch.deleteFolder();
          }

          // FOR TESTING ONLY
          File srcFile = new File(imageFolder);
          File[] files = srcFile.listFiles();
          StringBuilder sb = new StringBuilder();
          for (int i = 0; i < files.length; i++) {
               if (i>0) sb.append("\n");
               sb.append(files[i].getName());
          }

          ((TextView) findViewById(R.id.xmltester)).setText(sb.toString());

     }

     public void createZipfile(String imageFolder, String zipPath) {

          progress_file.delete();
          //StringBuilder sb = new StringBuilder();

          try {
               FileOutputStream fos = new FileOutputStream(zipPath);
               ZipOutputStream zos = new ZipOutputStream(fos);
               File srcFile = new File(imageFolder);
               File[] files = srcFile.listFiles();
               for (int i = 0; i < files.length; i++) {
                    String filename = files[i].getPath();
                    String extension = filename.substring(filename.length()-4,filename.length());
                    if (!extension.equals(".zip")) {
                         //if (i > 0) sb.append("\n");
                         //sb.append("[" + i + "] Adding file: " + files[i].getName());
                         byte[] buffer = new byte[1024];
                         FileInputStream fis = new FileInputStream(files[i] + "/textData.xml");
                         zos.putNextEntry(new ZipEntry(files[i].getName() + "/textData.xml"));
                         int length;
                         while ((length = fis.read(buffer)) > 0) {
                              zos.write(buffer, 0, length);
                         }
                         zos.closeEntry();
                         fis.close();
                    }
               }
               zos.close();
               Toast.makeText(context, "Zip created!", Toast.LENGTH_SHORT).show(); //test
          } catch (Exception ex) {
               //sb.append("\nEXCEPTION!!! " + ex.getMessage());
          }

          //((TextView) findViewById(R.id.xmltester2)).setText(sb.toString());

     }

     private class Patch extends XMLFileHandler {

          int imgno;
          int patchno;
          float x1, y1, x2, y2;
          String disease;
          ArrayList<String> analysis = new ArrayList<>();
          String remarks;
          int state;

          Patch(Context context, int imgno, int patchno, float x1, float y1, float x2, float y2, String disease) {

               super(context, "img" + String.format("%07d", imgno) + "_" + String.format("%03d", patchno), disease, true);
               this.imgno = imgno;
               this.patchno = patchno;
               this.x1 = x1;
               this.y1 = y1;
               this.x2 = x2;
               this.y2 = y2;
               this.disease = disease;
               this.remarks = "";
               this.state = PATCH_NEUTRAL;

          }

          public String formatImgno() {
               return String.format("%07d", imgno);
          }
          public String formatPatchno() {
               return String.format("%03d", patchno);
          }

     }

}
