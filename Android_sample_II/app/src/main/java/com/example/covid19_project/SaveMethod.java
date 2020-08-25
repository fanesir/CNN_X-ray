//package com.example.covid19_project;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.content.ContentValues.TAG;
//import static com.example.covid19_project.MainActivity.bitmap;

//public class SaveMethod {


// public void sss() {

//     bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
//     if ("" + bitmap.getWidth() + "," + bitmap.getHeight() != "224,224") {
//         bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
//       //  Toast.makeText(SaveMethod.this, "轉檔", Toast.LENGTH_LONG).show();
//     }

//     FileOutputStream fos = null;
//     File sdCard = Environment.getExternalStorageDirectory();//拿到SD卡資料
//     File directory = new File(sdCard.getAbsolutePath() + "/test_img");//在SD卡放置這個路徑
//     directory.mkdir();//創建資料夾
//     String fileName = String.format("%d.png", System.currentTimeMillis());//創建名稱
//     File outFile = new File(directory, fileName);//給予路徑跟名稱

//    // Toast.makeText(this, "Image save 成功", Toast.LENGTH_LONG).show();
//     try {
//         fos = new FileOutputStream(outFile);//此張照片放進來
//         bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);//壓縮
//         fos.flush();
//         fos.close();

//         Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);//掃描指定文件
//         intent.setData(Uri.fromFile(outFile));//給予資料
//         sendBroadcast(intent);//刷新

//     } catch (FileNotFoundException e) {
//         Log.d(TAG + "savefirst", e.getMessage());

//         e.printStackTrace();
//     } catch (IOException e) {
//         Log.d(TAG + "savefirst", e.getMessage());
//         e.printStackTrace();
//     }

// }
// public void sendBroadcast(Intent intent) {
//     throw new RuntimeException("Stub!");
// }
//
