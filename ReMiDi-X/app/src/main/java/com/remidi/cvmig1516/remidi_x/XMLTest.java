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
          ((TextView)findViewById(R.id.xmltester)).setText(progress_file.toString());

          loadProgressFile();
          //updateProgress();
          Patch patch = patches.get(0);
          StringBuilder patch1 = new StringBuilder();
          patch1.append("Image no: " + patch.imgno);
          patch1.append("\nPatch no: " + patch.patchno);
          patch1.append("\nDisease: " + patch.disease);
          patch1.append("\nAnalysis:");
          for (int i = 0; i<patch.analysis.size(); i++) {
               patch1.append("\n\t>" + patch.analysis.get(i));
          }
          patch1.append("\nRemarks: " + patch.remarks);

          ((TextView) findViewById(R.id.xmltester2)).setText(patch1.toString());

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

     public void loadProgressFile() {

          String contents = progress_file.readContents();
          StringTokenizer tokens = new StringTokenizer(contents, "$");

          current_image = Integer.parseInt(tokens.nextToken());

          while (tokens.hasMoreTokens()) {
               String token = tokens.nextToken();
               StringTokenizer patchData = new StringTokenizer(token, "|");
               float x1 = Float.parseFloat(patchData.nextToken());
               float y1 = Float.parseFloat(patchData.nextToken());
               float x2 = Float.parseFloat(patchData.nextToken());
               float y2 = Float.parseFloat(patchData.nextToken());

               Patch patch = new Patch(context, current_image, patches.size(),x1,y1,x2,y2,disease);
               String analysisToken = patchData.nextToken();
               if (!analysisToken.equals("###*No data*###")) {
                    StringTokenizer analysisData = new StringTokenizer(analysisToken, ",");
                    patch.state = PATCH_COMPLETE;
                    while (analysisData.hasMoreTokens()) {
                         String analysis = analysisData.nextToken();
                         patch.analysis.add(analysis);
                    }
               }
               else patch.state = PATCH_INCOMPLETE;

               String remarks = patchData.nextToken();
               if (!remarks.equals("###*No data*###")) patch.remarks = remarks;

               patches.add(patch);

          }

     }

     public void uploadZipfile() {

          Toast.makeText(context, "Sent image diagnosis!", Toast.LENGTH_SHORT).show();
          for (int i = 0; i<patches.size(); i++) {
               (patches.get(i)).deleteFolder();
          }

     }

     public void createZipfile(String zipPath) {

          String imageFolder = progress_file.filefolder;
          progress_file.delete();

          try {
               FileOutputStream fos = new FileOutputStream(zipPath);
               ZipOutputStream zos = new ZipOutputStream(fos);
               File srcFile = new File(imageFolder);
               File[] files = srcFile.listFiles();
               Log.d("", "Zip directory: " + srcFile.getName());
               for (int i = 0; i < files.length; i++) {
                    Log.d("", "Adding file: " + files[i].getName());
                    byte[] buffer = new byte[1024];
                    FileInputStream fis = new FileInputStream(files[i]);
                    zos.putNextEntry(new ZipEntry(files[i].getName()));
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                         zos.write(buffer, 0, length);
                    }
                    zos.closeEntry();
                    fis.close();
               }
               zos.close();
               Toast.makeText(context, "Zip created!", Toast.LENGTH_SHORT).show(); //test
          } catch (Exception ex) {
               Log.e("", ex.getMessage());
          }

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
