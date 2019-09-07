package com.example.myapplication.SiliCompressor;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.github.tcking.giraffecompressor.GiraffeCompressor;
import com.iceteck.silicompressorr.SiliCompressor;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

import static com.github.tcking.giraffecompressor.GiraffeCompressor.TYPE_FFMPEG;

public class MainActivity2 extends AppCompatActivity {

    private static String mCurrentVideoPath;
    Button button;
    TextView text_video, after_text_video;
    VideoView video_play, video_already_compress;
    final private int SELECT_VIDEO_REQUEST_CODE = 129;
    final private int SELECT_VIDEO_REQUEST_CODE_2 = 130;
    final private int REQUEST_CAMERA_VIDEO_PERMISSION = 128;
    String[] PERMISSIONS_VID = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public static final String FILE_PROVIDER_AUTHORITY = ".provider";
    private static final int TYPE_IMAGE = 1;
    private static final int TYPE_VIDEO = 2;

    String mCurrentPhotoPath;
    Uri capturedUri = null;
    Uri compressUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        button = (Button) findViewById(R.id.button);
        text_video = (TextView) findViewById(R.id.text_video);
        text_video.setVisibility(View.GONE);
        after_text_video = (TextView) findViewById(R.id.after_text_video);
        after_text_video.setVisibility(View.GONE);
        video_play = (VideoView) findViewById(R.id.video_play);
        video_already_compress = (VideoView) findViewById(R.id.video_already_Compress);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!hasPermissions(MainActivity2.this, PERMISSIONS_VID)){
                    ActivityCompat.requestPermissions(MainActivity2.this, PERMISSIONS_VID, REQUEST_CAMERA_VIDEO_PERMISSION);
                } else {
                    startVideo();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_VIDEO_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    startVideo();
                } else {

                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode){
                case SELECT_VIDEO_REQUEST_CODE:
                    Uri videoUri = data.getData();
                    video_play.setVideoPath(videoUri.toString());
                    //add control play or stop or pause
                    final MediaController mediaController = new MediaController(MainActivity2.this);
                    video_play.setMediaController(mediaController);

                    video_play.requestFocus();
                    video_play.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            video_play.seekTo(1);
                        }
                    });

                    if (data.getData() != null) {
                        //create destination directory
                        File f = new File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/Silicompressor/videos");

                        File imageFile = new File(mCurrentPhotoPath);
                        float length = imageFile.length() / 1024f; // Size in KB
                        String value;
                        if (length >= 1024)
                            value = length / 1024f + " MB";
                        else
                            value = length + " KB";
                        String text = String.format(Locale.US, "%s\nName: %s\nSize: %s", "video original ", imageFile.getName(), value);
                        text_video.setVisibility(View.VISIBLE);
                        text_video.setText(text);
                        Log.i("Silicompressor", "Path: " + mCurrentPhotoPath);

                        if (f.mkdirs() || f.isDirectory()) {
                            //compress and output new video specs
                            new VideoCompressAsyncTask(this).execute(mCurrentPhotoPath, f.getPath());
                        }

                    }
                    String path = MediaStore.Video.Media.INTERNAL_CONTENT_URI.getPath();
                    break;
                case SELECT_VIDEO_REQUEST_CODE_2:

                    break;
            }
        }
    }

    public boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void startVideo(){
        /*try {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            // set video quality if 1 = high quality video and 0 = low quality
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            // set video max record 15 sec
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,15);
            // set size record video
            intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 2097152);// 2*1024*1024 = 2MB
            if (intent.resolveActivity(getPackageManager()) != null) {
                // start the video capture Intent
                startActivityForResult(intent, SELECT_VIDEO_REQUEST_CODE);
            }
        }catch (Exception e){
            e.printStackTrace();
        }*/

        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        takeVideoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            try {
                takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,60);
                capturedUri = FileProvider.getUriForFile(this,
                        getPackageName() + FILE_PROVIDER_AUTHORITY,
                        createMediaFile(TYPE_VIDEO));

                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedUri);
                startActivityForResult(takeVideoIntent, SELECT_VIDEO_REQUEST_CODE);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private File createMediaFile(int type) throws IOException {

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = (type == TYPE_IMAGE) ? "JPEG_" + timeStamp + "_" : "VID_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                type == TYPE_IMAGE ? Environment.DIRECTORY_PICTURES : Environment.DIRECTORY_MOVIES);
        File file = File.createTempFile(
                fileName,  /* prefix */
                type == TYPE_IMAGE ? ".jpg" : ".mp4",         /* suffix */
                storageDir      /* directory */
        );

        // Get the path of the file created
        mCurrentPhotoPath = file.getAbsolutePath();
        Log.d( "mCurrentPhotoPath: ", mCurrentPhotoPath);
        return file;
    }

    class VideoCompressAsyncTask extends AsyncTask<String, String, String> {

        Context mContext;

        public VideoCompressAsyncTask(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... paths) {
            String filePath = null;
            try {

                filePath = SiliCompressor.with(mContext).compressVideo(paths[0], paths[1]);

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return filePath;

        }


        @Override
        protected void onPostExecute(String compressedFilePath) {
            super.onPostExecute(compressedFilePath);
            File imageFile = new File(compressedFilePath);
            float length = imageFile.length() / 1024f; // Size in KB
            String value;
            if (length >= 1024)
                value = length / 1024f + " MB";
            else
                value = length + " KB";
            String text = String.format(Locale.US, "%s\nName: %s\nSize: %s", "complete compress", imageFile.getName(), value);
            after_text_video.setVisibility(View.VISIBLE);
            after_text_video.setText(text);
            Log.i("Silicompressor", "Path: " + compressedFilePath);

            video_already_compress.setVideoPath(compressedFilePath);
            //add control play or stop or pause
            final MediaController mediaController2 = new MediaController(MainActivity2.this);
            video_already_compress.setMediaController(mediaController2);

            video_already_compress.requestFocus();
            video_already_compress.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    video_already_compress.seekTo(1);
                }
            });
        }
    }



}