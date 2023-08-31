package com.fatiheminkarahan.imagegenerator;

import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.backup.FullBackupDataOutput;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ImageGenerator extends AppCompatActivity {

    EditText inputText;
    MaterialButton generateBtn;
    ProgressBar progressBar;
    ImageView imageView;

    MaterialButton saveİmageBtn;

    Bitmap bitmap;
    BitmapDrawable bitmapDrawable;

    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();

    @SuppressLint("MissingInflatedId")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_generator);

        ActivityCompat.requestPermissions(this,
                new String[]{READ_MEDIA_IMAGES, WRITE_EXTERNAL_STORAGE},
                PackageManager.PERMISSION_GRANTED);

        inputText = findViewById(R.id.input_text);
        generateBtn = findViewById(R.id.generate_btn);
        progressBar = findViewById(R.id.progress_bar);
        imageView = findViewById(R.id.image_view);
        saveİmageBtn = findViewById(R.id.save_image_btn);

        generateBtn.setOnClickListener((v)->{
            String text = inputText.getText().toString().trim();
            if(text.isEmpty()){
                inputText.setError("Text can't be empty");
                return;
            }
            callAPI(text);
        });

        saveİmageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if(ContextCompat.checkSelfPermission(ImageGenerator.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
                saveImage();
            }
        });
    }

    public void saveImage(){

        Uri images;
        ContentResolver contentResolver = getContentResolver();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            images = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        }else{
            images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, System.currentTimeMillis() + ".jpg");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "images/*");
        Uri uri = contentResolver.insert(images, contentValues);

        try {
            BitmapDrawable bitmapDrawable1 = (BitmapDrawable) imageView.getDrawable();
            Bitmap bitmap1 = bitmapDrawable1.getBitmap();
            OutputStream outputStream = contentResolver.openOutputStream(Objects.requireNonNull(uri));
            bitmap1.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            Objects.requireNonNull(outputStream);

            Toast.makeText(ImageGenerator.this, "Images Saved Successfully", Toast.LENGTH_LONG).show();



        } catch (Exception e){;
            Toast.makeText(ImageGenerator.this, "Images Saved Successfully", Toast.LENGTH_LONG).show();
        }

    }





    void callAPI(String text){
        //API CALL
        setInProgress(true);
        JSONObject jsonBody = new JSONObject();
        try{
            jsonBody.put("prompt",text);
            jsonBody.put("size","256x256");
        }catch (Exception e){
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(jsonBody.toString(),JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/images/generations")
                .header("Authorization","Bearer API API API API API API API API")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Toast.makeText(getApplicationContext(),"Failed to generate image",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {


                try{
                    String deneme = response.body().string();
                    Log.i("Response : " , deneme);
                    JSONObject jsonObject = new JSONObject(deneme);
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    String imgUrl;
                    JSONObject jsonObject1 = jsonArray.getJSONObject(0);
                    imgUrl = jsonObject1.getString("url");
                    Log.i("IMG_URL: ", imgUrl);
                    loadImage(imgUrl);
                    /*for (int i = 0; i < jsonArray.length(); i++){
                        String imgUrl;
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        imgUrl = jsonObject1.getString("url");
                        Log.i("IMG_URL: ", imgUrl);
                        loadImage(imgUrl);
                    }*/
                    String imageUrl = jsonObject.getJSONArray("data").getJSONObject(0).getString("url");
                    //Log.i("Response : " , imageUrl);
                    //loadImage(imageUrl);
                    setInProgress(false);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

    }

    void setInProgress(boolean inProgress){
        runOnUiThread(()->{
            if(inProgress){
                progressBar.setVisibility(View.VISIBLE);
                generateBtn.setVisibility(View.GONE);
            }else{
                progressBar.setVisibility(View.GONE);
                generateBtn.setVisibility(View.VISIBLE);
            }
        });

    }

    void loadImage(String url){
        //load image

        runOnUiThread(()->{
            Picasso.get().load(url).into(imageView);
        });

    }

}