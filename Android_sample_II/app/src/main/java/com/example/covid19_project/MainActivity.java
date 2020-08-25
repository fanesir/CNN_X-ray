package com.example.covid19_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.provider.MediaStore;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;

import com.google.firebase.ml.common.*;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.custom.*;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalDate;


public class MainActivity<onActivityResult> extends AppCompatActivity {
    private static String TAG = "DEBUG_LOG";
    private FirebaseCustomLocalModel localModel;
    private FirebaseModelInputOutputOptions inputOutputOptions;
    private ImageView mImageView;
    TextView resultDisplayTV;
    public Bitmap bitmap;
    Drawable drawable;
    Uri uri;
    Button choosImage, scanImage;
    FirebaseCustomRemoteModel remoteModel;
    FirebaseModelInterpreter interpreter;
    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;
    private String imagepath = null;
    String Patg_Msg;
//    Task<AuthResult> task = FirebaseAuth.getInstance().signInAnonymously();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Log.d("test_timer", "one second");
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);
        mImageView = findViewById(R.id.such_image);
        resultDisplayTV = findViewById(R.id.results_tv);

        choosImage = findViewById(R.id.add_img_btn);
        scanImage = findViewById(R.id.scan_image_btn);
        mImageView.setOnClickListener(this::saveimage);


        choosImage.setOnClickListener(view -> {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)//取得權限
            {//確認使否有授權
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {//跳出來給選擇
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                } else {

                    FindImagePicker();//找圖片
                }
            } else {

                FindImagePicker();
            }
        });
        scanImage.setOnClickListener(new View.OnClickListener() {//辨識圖片
            @Override
            public void onClick(View view) {
                resultDisplayTV.setText("");//Textview 先給空值

                // Getting bitmap from Imageview
                BitmapDrawable drawable = (BitmapDrawable) mImageView.getDrawable();//把圖片拿取
                bitmap = drawable.getBitmap();//get 像素點
                //Toast.makeText(MainActivity.this, bitmap+"", Toast.LENGTH_LONG).show();
                suchimage();

            }
        });


        remoteModel = new FirebaseCustomRemoteModel.Builder("mobilenet_for_covid").build();
        //將模型下載到設備
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().requireWifi().build();//
        //檢查網路模型是否進來
        FirebaseModelManager.getInstance().download(remoteModel, conditions)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {//Task 是一個呼叫是否成功的函數
                        // Success.
                        if (task.isSuccessful()) {//這裡來判別
                            Log.d(TAG + "_DWNLD TSK: ", "Task Successfull");
                        } else {
                            Log.d(TAG + "_DWNLD TSK: ", "Task UnSuccessfull");
                        }
                    }
                });
        localModel = new FirebaseCustomLocalModel.Builder()//配置本地模型
                .setAssetFilePath("mobilenet_for_covid-19.tflite")
                .build();


        try {//根據模型創建解釋器
            FirebaseModelInterpreterOptions options =
                    new FirebaseModelInterpreterOptions.Builder(localModel).build();
            interpreter = FirebaseModelInterpreter.getInstance(options);
            Log.d(TAG + "_LCL MDL DWNLD STS: ", "Local Model Loaded");
        } catch (FirebaseMLException e) {
            Log.d(TAG + "_LCL MDL DWNLD STS: ", "Error Loading local model :" + e.getMessage());
        }

        //開啟遠端的模型
        FirebaseModelManager.getInstance().download(remoteModel, conditions)
                .addOnSuccessListener((Void v) -> {
                    // Download complete. Depending on your app, you could enable
                    // the ML feature, or switch from the local model to the remote
                    // model, etc.
                    Log.d(TAG + "_RMT MDL DWNLD STS", "Remote Model Downloaded Successfully");
                });
        //遠程託管
        FirebaseModelManager.getInstance()
                .isModelDownloaded(remoteModel)
                .addOnSuccessListener((Boolean isDownloaded) -> {
                    FirebaseModelInterpreterOptions options;
                    //儘管您只需要已下載，否則從本地模型下載
                    if (isDownloaded) {
                        options = new FirebaseModelInterpreterOptions.Builder(remoteModel).build();//網路模型
                        Log.d(TAG + "_INTRPTR LOADED: ", "Remote model is being used");
                    } else {
                        options = new FirebaseModelInterpreterOptions.Builder(localModel).build();//本地模型
                        Log.d(TAG + "_INTRPTR LOADED: ", "Local model is being used");
                    }
                    try {
                        FirebaseModelInterpreter interpreter = FirebaseModelInterpreter.getInstance(options);
                    } catch (FirebaseMLException e) {
                        e.printStackTrace();
                        Log.d(TAG + "_ERROR LODNG INTRPTR: ", "Error loading interpeter: " + e.getMessage());
                    }
                });
        try {//input model size
            inputOutputOptions =
                    new FirebaseModelInputOutputOptions.Builder()
                            .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 224, 224, 3})
                            .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 3})
                            .build();
        } catch (FirebaseMLException e) {
            Log.d(TAG + "_ERROR: ", "Error setting up FirebaseModelInputOutputOptions: " + e.getMessage());
        }
        bitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
    }

    public void suchimage() {//開始辨識照片


        int batchNum = 0;
        ByteBuffer input = ByteBuffer.allocateDirect(224 * 224 * 3 * 4).order(ByteOrder.nativeOrder());
        //float[][][][] input = new float[1][224][224][3];//先宣告裡面有幾個位置

        bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
        //Log.d(TAG + "_size", bitmap.getWidth() + "," + bitmap.getHeight());

        // Toast.makeText(this, "" + bitmap.getWidth() + "," + bitmap.getHeight(), Toast.LENGTH_LONG).show();
        try {
            for (int y = 0; y < 224; y++) {
                for (int x = 0; x < 224; x++) {
                    // Get channel values from the pixel value.
                    int pixel = bitmap.getPixel(x, y);
                    float ff = 255;
                    input.putFloat(Color.red(pixel) / ff);
                    input.putFloat(Color.green(pixel) / ff);
                    input.putFloat(Color.blue(pixel) / ff);

                }
            }
        } catch (Exception e) {
            Log.d(TAG + "_ERROR", "Error while converting bitmap to pixels : " + e.getMessage());
            //resultDisplayTV.setText("Couldn't process Img. Please try again with different aspect ratio.");
        }
        FirebaseModelInputs inputs = null;
        try {
            inputs = new FirebaseModelInputs.Builder().add(input).build();
            Log.d(TAG + "INPT STS: ", "Success in generating FirebaseModelInputs");
        } catch (FirebaseMLException e) {
            Log.d(TAG + "_ERROR:", "Error in building FirebaseModelInputs : " + e.getMessage());
        }

        //將其以及模型的輸入和輸出規範傳遞給模型解釋器的run *inputs 是要辨識的圖片

        interpreter.run(inputs, inputOutputOptions).addOnSuccessListener((FirebaseModelOutputs result) -> {

            Log.d(TAG + "_INTRPTR STS:", "Success in running interpreter on input image");

            float[][] output = result.getOutput(0);
            float[] probabilities = output[0];
            BufferedReader reader = null;

            try {
                reader = new BufferedReader(//read file.txt
                        new InputStreamReader(getAssets().open("covid-19_c3_label.txt")));

                String res = "";
                for (int i = 0; i < probabilities.length; i++) {
                    String label = null;

                    try {
                        label = reader.readLine();//這個lab讀取檔案裡面的資料
                    } catch (IOException e1) {
                        Log.d(TAG + "_ERROR: ", "Error reading line in label file: " + e1.getMessage());
                    }

                    res += String.format("%s: %1.4f", label, probabilities[i]) + "\n";//probabilities讀取每個機率

                    Log.i(TAG + "_MLKit", String.format("%s: %1.4f", label, probabilities[i]));

                }
                resultDisplayTV.setText(res);


            } catch (IOException e1) {
                Log.d(TAG + "_ERROR: ", "Error reading label file: " + e1.getMessage());
            }
        }).addOnFailureListener((@NonNull Exception e) -> {
            Log.d(TAG + "_ERROR: ", "Error running interpreter on input image : " + e.getMessage());
        });

    }

    //裁切圖片方式
    private void bringImagePicker() {//設置選取的圖片
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.OFF).setAspectRatio(1, 1).setMinCropResultSize(224, 224)
                .setRequestedSize(224, 224)
                .setCropShape(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? CropImageView.CropShape.RECTANGLE : CropImageView.CropShape.OVAL)
                .start(MainActivity.this);//?
    }

    private void FindImagePicker() {


        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);


    }


    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            super.onActivityResult(requestCode, resultCode, data);
            Uri selectedImageUri = data.getData();

            imagepath = getPath(selectedImageUri);
            Log.d(TAG + "_name", imagepath);
            String patg_msg = new String(imagepath + "");
            Patg_Msg = patg_msg.substring(36);
            Toast.makeText(MainActivity.this, Patg_Msg + "", Toast.LENGTH_LONG).show();

            bitmap = BitmapFactory.decodeFile(imagepath);
            mImageView.setImageBitmap(bitmap);

            //mImageView.setImageURI(data.getData());
        }
    }

    public void saveimage(View v) {

        bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
        if ("" + bitmap.getWidth() + "," + bitmap.getHeight() != "224,224") {
            bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
            Toast.makeText(this, "轉檔", Toast.LENGTH_LONG).show();
        }

        FileOutputStream fos = null;
        File sdCard = Environment.getExternalStorageDirectory();//拿到SD卡資料
        File directory = new File(sdCard.getAbsolutePath() + "/test_img");//在SD卡放置這個路徑
        directory.mkdir();//創建資料夾
        String fileName = String.format("%d.png", System.currentTimeMillis());//創建名稱
        File outFile = new File(directory, fileName);//給予路徑跟名稱

        Toast.makeText(this, "Image save 成功", Toast.LENGTH_LONG).show();
        try {
            fos = new FileOutputStream(outFile);//此張照片放進來
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);//壓縮
            fos.flush();
            fos.close();

            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);//掃描指定文件
            intent.setData(Uri.fromFile(outFile));//給予資料
            sendBroadcast(intent);//刷新

        } catch (FileNotFoundException e) {
            Log.d(TAG + "savefirst", e.getMessage());

            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG + "savefirst", e.getMessage());
            e.printStackTrace();
        }
    }


}