package com.example.daehwankim.image_upload;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Path;
import android.media.ExifInterface;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class MainActivity extends FragmentActivity {

    Button camera;
    Button album;
    ImageView getImage;
    TextView text;
    ExifInterface exifInterface;
    File image;
    String response;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camera = (Button) findViewById(R.id.button);
        album = (Button) findViewById(R.id.button2);
        getImage = (ImageView) findViewById(R.id.imageView);
        text = (TextView) findViewById(R.id.textView);
        
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent move_camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(move_camera, 1);
            }
        });

        album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent move_album = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(move_album, 2);
            }
        });
    }



    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case 1:
                    String fileName = null;
                    File[] listFiles = (new File(Environment.getExternalStorageDirectory()+"/DCIM/Camera/").listFiles());

                    if(listFiles[listFiles.length-1].getName().endsWith(".jpg") || listFiles[listFiles.length-1].getName().endsWith(".bmp")){
                        fileName = listFiles[listFiles.length-1].getName();
                        Log.e("listFiles Log : ", String.valueOf(listFiles.length));
                        Log.e("listFiles Log : ", listFiles[listFiles.length-1].toString());
                        Log.e("listFiles Log : ", fileName);
                    }



                    try {
                        exifInterface = new ExifInterface(Environment.getExternalStorageDirectory()+"/DCIM/Camera/"+fileName);
                        showExif(exifInterface);
                        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);

                        Bitmap image = (Bitmap)data.getExtras().get("data");

                        switch (orientation){
                            case 6:
                                getImage.setImageBitmap(rotateBitmap(image, 90));
                                break;
                            default:
                                getImage.setImageBitmap((Bitmap)data.getExtras().get("data"));
                                break;
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;
                case 2:
                    try {
                        Log.e("album Log : ", String.valueOf(data.getData()));

                        image = new File(getPath(data.getData()));

                        exifInterface = new ExifInterface(getPath(data.getData()));

                        int case2_orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);

                        Bitmap case2_image = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());

                        switch (case2_orientation){
                            case 6:
                                getImage.setImageBitmap(rotateBitmap(case2_image, 90));
                                break;
                            default:
                                getImage.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData()));
                                break;
                        }


                        showExif(exifInterface);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    send_image();
                    break;
            }
        }
    }


    private void showExif(ExifInterface exif) {

        String myAttribute = "[Exif information] \n\n";

        myAttribute += "DATETIME : " + exif.getAttribute(ExifInterface.TAG_DATETIME);
        myAttribute += "\nFLASH : " + exif.getAttribute(ExifInterface.TAG_FLASH);
        myAttribute += "\nLATITUDE : " + exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
        myAttribute += "\nLATITUDE_REF : " + exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
        myAttribute += "\nLONGITUDE : " + exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
        myAttribute += "\nLONGITUDE_REF : " + exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
        myAttribute += "\nIMAGE_LENGTH : " + exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
        myAttribute += "\nIMAGE_WIDTH : " + exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
        myAttribute += "\nMAKE : " + exif.getAttribute(ExifInterface.TAG_MAKE);
        myAttribute += "\nMODEL : " + exif.getAttribute(ExifInterface.TAG_MODEL);
        myAttribute += "\nORIENTATION : " + exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
        myAttribute += "\nWHITE_BALANCE : " + exif.getAttribute(ExifInterface.TAG_WHITE_BALANCE);

        text.setText(myAttribute);
    }

    private String getPath(Uri uri){
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        startManagingCursor(cursor);
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(columnIndex);
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int degree){
        if (bitmap != null && degree != 0){
            Matrix matrix = new Matrix();
            matrix.setRotate(degree, (float)bitmap.getWidth()/2, (float)bitmap.getHeight()/2);
            Bitmap tempbitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);{
                if (bitmap != tempbitmap){
                    bitmap.recycle();
                    bitmap = tempbitmap;
                }
            }
        }
        return bitmap;
    }

    public void send_image(){

        class GetImageData extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {

                MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create()
                        .setCharset(Charset.forName("UTF-8"))
                        .setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

                multipartEntityBuilder.setBoundary("----");



//                multipartEntityBuilder.addPart("image", new FileBody(image));
                multipartEntityBuilder.addPart("image2", new FileBody(new File("/storage/emulated/0/DCIM/Book/P1015027.jpg")));
                multipartEntityBuilder.addTextBody("aa","aa");

                HttpEntity entity = multipartEntityBuilder.build();

                HttpClient client = AndroidHttpClient.newInstance("Android");

                HttpPost post = new HttpPost("http://miraclehwan.vps.phps.kr/webtest/upload.php");

                post.setEntity(entity);
                Log.e("Response Log : ", "1");
                try {

                    HttpResponse httpRes;
                    httpRes = client.execute(post);
                    HttpEntity httpEntity = httpRes.getEntity();
                    Log.e("Response Log : ", "2");
                    if (httpEntity != null) {
                        response = EntityUtils.toString(httpEntity);
                        Log.e("Response Log : ", response);
                    }
                    Log.e("Response Log : ", "3");
                } catch (IOException e) {
                    e.printStackTrace();
                }


                return null;
            }
        }
        GetImageData getImageData = new GetImageData();
        getImageData.execute();
    }

}