package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.github.tcking.giraffecompressor.GiraffeCompressor;
import com.iceteck.silicompressorr.SiliCompressor;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

import static com.github.tcking.giraffecompressor.GiraffeCompressor.TYPE_FFMPEG;
import static com.github.tcking.giraffecompressor.GiraffeCompressor.TYPE_MEDIACODEC;

public class MainActivity extends AppCompatActivity {

    private static String mCurrentVideoPath;
    Button button, upload, upload_2, video_2;
    ImageView video_preview;
    TextView text_video;
    VideoView video_play;
    final private int SELECT_VIDEO_REQUEST_CODE = 129;
    final private int SELECT_VIDEO_REQUEST_CODE_2 = 130;
    final private int REQUEST_CAMERA_VIDEO_PERMISSION = 128;
    String[] PERMISSIONS_VID = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public static final String FILE_PROVIDER_AUTHORITY = ".provider";
    private static final int TYPE_IMAGE = 1;
    private static final int TYPE_VIDEO = 2;
    //String mCurrentVideoPath;
    Uri capturedUri = null;
    boolean video2 = false;

    public static String output;
    private Uri fileUri;
    public static final int MEDIA_TYPE_VIDEO = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button);
        upload = (Button) findViewById(R.id.upload);
        upload_2 = (Button) findViewById(R.id.upload_2);
        video_preview = (ImageView) findViewById(R.id.video_preview);
        text_video = (TextView) findViewById(R.id.text_video);
        text_video.setVisibility(View.GONE);
        video_play = (VideoView) findViewById(R.id.video_play);
        video_play.setVisibility(View.GONE);
        video_2 = (Button) findViewById(R.id.video_2);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                video2 = false;
                if(!hasPermissions(MainActivity.this, PERMISSIONS_VID)){
                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_VID, REQUEST_CAMERA_VIDEO_PERMISSION);
                } else {
                    startVideo();
                }
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Progressbar_Timer();
                customeDialogBar();
            }
        });

        upload_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

                progressDialog.setMessage("Uploading Video…");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setCanceledOnTouchOutside(false);

                final int totalProgressTime = 100;
                final Thread t = new Thread() {
                    @Override
                    public void run() {
                        int jumpTime = 0;

                        while(jumpTime < totalProgressTime) {
                            try {
                                sleep(200);
                                jumpTime += 5;
                                progressDialog.setProgress(jumpTime);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                };
                t.start();
                progressDialog.show();
            }
        });

        video_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                video2 = true;
                if(!hasPermissions(MainActivity.this, PERMISSIONS_VID)){
                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_VID, REQUEST_CAMERA_VIDEO_PERMISSION);
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
                    //create destination directory
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/PetBacker/videos");

                    /*Log.e("checkSize", getReadableFileSize(f.length()));
                    File imageFile = new File(capturedUri.toString());
                    float length = imageFile.length() / 1024f; // Size in KB
                    String value;
                    if (length >= 1024){
                        value = length / 1024f + " MB";
                    } else {
                        value = length + " KB";
                    }
                    String text = String.format(Locale.US, "%s\nName: %s\nSize: %s", "Video Compress Complete ", f.getName(), value);
                    Log.i("Silicompressor", " size previous = " + text);

                    File imageFile1 = new File(data.getData().getPath());
                    float length1 = imageFile1.length() / 1024f; // Size in KB
                    String value1;
                    if (length1 >= 1024){
                        value1 = length1 / 1024f + " MB";
                    } else {
                        value1 = length1 + " KB";
                    }
                    String text1 = String.format(Locale.US, "%s\nName: %s\nSize: %s", "Video Compress Complete ", imageFile1.getName(), value1);
                    Log.i("Silicompressor", " size previous = " + text1);*/

                    if (file.mkdirs() || file.isDirectory()){
                        //compress and output new video specs
                        //new VideoCompressAsyncTask(this).execute(videoUri.getPath(), NameVideo);
                        Log.e("checkSize", getReadableFileSize(file.length()));
                        Log.e("checkSize", getReadableFileSize(mCurrentVideoPath.length()));
                        new VideoCompressAsyncTask(this).execute(mCurrentVideoPath, file.getPath());
                    }

                    break;
                case SELECT_VIDEO_REQUEST_CODE_2:
                    final Uri videoUri = data.getData();
                    Log.e("checkDataUri", videoUri.toString() + " yeah video " + videoUri.getPath() + " && " + data.getData().getPath());

                    File files = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/PetBacker/videos");
                    final Long tsLong = System.currentTimeMillis();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
                    Date date = new Date();
                    String timeStamp = dateFormat.format(date);
                    String NameVideo = files + "_" + timeStamp + "_" + tsLong.toString() + ".mp4";

                    final File imageFile = new File(videoUri.toString());
                    Log.e("checkDataUri", imageFile.toString() + " yeah video " + imageFile.getPath());
                    GiraffeCompressor.create(TYPE_FFMPEG)// ? TYPE_MEDIACODEC : TYPE_FFMPEG) //two implementations: mediacodec and ffmpeg,default is mediacodec
                            .input(videoUri.toString()) //set video to be compressed
                            .output(NameVideo) //set compressed video output
                            .bitRate(2073600)//set bitrate 码率
                            .resizeFactor(1)//set video resize factor 分辨率缩放,默认保持原分辨率
                            //.watermark("/sdcard/videoCompressor/watermarker.png")//add watermark(take a long time) 水印图片(需要长时间处理)
                            .ready()
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Subscriber<GiraffeCompressor.Result>() {
                                @Override
                                public void onCompleted() {
                                    //$.id(R.id.btn_start).enabled(true).text("start compress");
                                    Log.e("checkVideoCompleted", "yeah completed");
                                }

                                @Override
                                public void onError(Throwable e) {
                                    e.printStackTrace();
                                    //$.id(R.id.btn_start).enabled(true).text("start compress");
                                    //$.id(R.id.tv_console).text("error:"+e.getMessage());
                                    Log.e("checkVideoError", "error : " + e.getMessage());

                                }

                                @Override
                                public void onNext(GiraffeCompressor.Result s) {
                                    String msg = String.format("compress completed \ntake time:%s \nout put file:%s", s.getCostTime(), s.getOutput());
                                    msg = msg + "\ninput file size:"+ Formatter.formatFileSize(getApplication(),new File(videoUri.getPath()).length());
                                    msg = msg + "\nout file size:"+ Formatter.formatFileSize(getApplication(),new File(s.getOutput()).length());
                                    System.out.println(msg);
                                    text_video.setVisibility(View.VISIBLE);
                                    text_video.setText(msg);

                                    video_play.setVisibility(View.VISIBLE);

                                    video_play.setVideoPath(new File(s.getOutput()).getName());
                                    final MediaController mediaController = new MediaController(MainActivity.this);
                                    video_play.setMediaController(mediaController);

                                    video_play.requestFocus();
                                    video_play.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                        @Override
                                        public void onPrepared(MediaPlayer mp) {
                                            video_play.seekTo(1);
                                        }
                                    });
                                }
                            });

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

    public void startVideo(){
        try {
            /*Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            // set video quality, 0 = low quality and then 1 = high quality
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
            // set video max record 15 sec
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,15);
            if (intent.resolveActivity(getPackageManager()) != null) {
                // start the video capture Intent
                startActivityForResult(intent, SELECT_VIDEO_REQUEST_CODE);
            }*/

            if (video2){
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                // set video quality, 0 = low quality and then 1 = high quality
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                // set video max record 15 sec
                intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,15);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    // create a file to save the video
                    fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO, MainActivity.this);
                    Log.d("VideoUri:"," " + fileUri.toString());

                    // set the image file name
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                    if (Build.VERSION.SDK_INT > 24){
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                    // start the video capture Intent
                    startActivityForResult(intent, SELECT_VIDEO_REQUEST_CODE_2);
                }
            } else {
                Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
                    try {

                        takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 15);
                        takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                        capturedUri = FileProvider.getUriForFile(MainActivity.this,
                                getPackageName() + FILE_PROVIDER_AUTHORITY,
                                createMediaFile());

                        takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedUri);
                        Log.d("VideoUri:"," " + capturedUri.toString());

                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                            Log.e("checkFlags 1", "yeah below 21");
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                Log.e("checkFlags 2", "yeah below 21");
                                takeVideoIntent.setClipData(ClipData.newRawUri("", capturedUri));
                            }
                            takeVideoIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION|Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } else {
                            Log.e("checkFlags", "yeah above 21");
                            takeVideoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }
                        startActivityForResult(takeVideoIntent, SELECT_VIDEO_REQUEST_CODE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static File createMediaFile() throws IOException {
        // Create an video file name
        Long tsLong = System.currentTimeMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Date date = new Date();
        String timeStamp = dateFormat.format(date);
        //String fileName = "VID_" + timeStamp + "_" + tsLong.toString() + ".mp4";
        String fileName = "VID_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        //File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "Movies");
        File file = File.createTempFile(
                fileName,  /* prefix */
                ".mp4",         /* suffix */
                storageDir      /* directory */
        );

        // Get the path of the file created
        mCurrentVideoPath = file.getAbsolutePath();
        Log.d("mCurrentPhotoPath:"," " + mCurrentVideoPath);
        return file;
    }

    private String path(){
        return mCurrentVideoPath;
    }


    class VideoCompressAsyncTask extends AsyncTask<String, String, String> {

        Context mContext;

        public VideoCompressAsyncTask(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            video_preview.setImageDrawable(ContextCompat.getDrawable(mContext, R.mipmap.ic_launcher));
            text_video.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(String... paths) {
            String filePath = null;
            try {
                filePath = SiliCompressor.with(mContext).compressVideo(paths[0], paths[1]);
                Log.i("Silicompressor", "Path 2: " + filePath);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (Exception ex){
                ex.printStackTrace();
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
            String text = String.format(Locale.US, "%s\nName: %s\nSize: %s", "Video Compress Complete ", imageFile.getName(), value);
            text_video.setVisibility(View.VISIBLE);
            text_video.setText(text);
            Log.i("Silicompressor", "Path: " + compressedFilePath + " size = " + text);
            Log.e("checkSize", getReadableFileSize(imageFile.length()));
            video_play.setVisibility(View.VISIBLE);

            video_play.setVideoPath(imageFile.getName());
            final MediaController mediaController = new MediaController(MainActivity.this);
            video_play.setMediaController(mediaController);

            video_play.requestFocus();
            video_play.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    video_play.seekTo(1);
                }
            });
        }
    }

    public static String getReadableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public void Progressbar_Timer() {
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

        // Set horizontal progress bar style.
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        // Set progress dialog title.
        progressDialog.setTitle("Upload Video...");
        // The maxima progress value.
        //progressDialog.setMax(100);
        // Whether progress dialog can be canceled or not.
        progressDialog.setCancelable(true);
        // When user touch area outside progress dialog whether the progress dialog will be canceled or not.
        progressDialog.setCanceledOnTouchOutside(false);


        // Set progress dialog message.
        //progressDialog.setMessage("This is a horizontal progress dialog.");

        // Popup the progress dialog.
        progressDialog.show();

        // Create a new thread object.
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                int i = 0;
                // Update progress bar every 0.3 second.
                while (i < 100) {
                    try {
                        Thread.sleep(300);
                        // Update the progress value.
                        progressDialog.incrementProgressBy(1);
                        // Update the secondary progress value.
                        //progressDialog.incrementSecondaryProgressBy(5);
                        i++;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                // Close and delete the dialog when the progress bar is finished
                progressDialog.dismiss();
            }
        });

        // Start the thread.
        thread.start();
    }

    Dialog bar;
    public void customeDialogBar(){
        bar = new Dialog(MainActivity.this);
        bar.requestWindowFeature(Window.FEATURE_NO_TITLE);
        bar.setContentView(R.layout.custom_dialog);
        bar.setCanceledOnTouchOutside(false);
        bar.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float pxWidth = (float)displayMetrics.widthPixels;
        bar.getWindow().setLayout((int)((double)pxWidth * 0.75D), -2);
        bar.getWindow().getAttributes().windowAnimations = libs.mjn.prettydialog.R.style.pdlg_default_animation;

        LinearLayout linearLayout = bar.findViewById(R.id.ll_content);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-1, -2);
        lp.setMargins(0, getResources().getDimensionPixelSize(libs.mjn.prettydialog.R.dimen.pdlg_icon_size) / 2, 0, 0);
        linearLayout.setLayoutParams(lp);
        linearLayout.setPadding(0, (int)(1.25D * (double)getResources().getDimensionPixelSize(libs.mjn.prettydialog.R.dimen.pdlg_icon_size) / 2.0D), 0, getResources().getDimensionPixelSize(libs.mjn.prettydialog.R.dimen.pdlg_space_1_0x));

        ImageView imageView = bar.findViewById(R.id.iv_icon);
        imageView.setImageResource(R.drawable.oops_small);
        TextView textView = bar.findViewById(R.id.tv_message);
        textView.setText("Uploading Video…");
        TextView textView1 = bar.findViewById(R.id.tv_title);
        textView1.setVisibility(View.GONE);

        final TextView percent = bar.findViewById(R.id.percent);

        final ProgressBar progressBar = bar.findViewById(R.id.progressBar);
        //process here
        @SuppressWarnings("VisibleForTests")
        long fileSize = 1000000;
        @SuppressWarnings("VisibleForTests")
        long uploadBytes = 500;
        final long progress = (100 * 100) / 1000;
        progressBar.setProgress((int) progress);
        percent.setText((int) progress + "%");
        // Create a new thread object.
        /*Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                int i = 0;
                // Update progress bar every 0.3 second.
                while (i < (int) progress) {
                    try {
                        Thread.sleep(5);
                        // Update the progress value.
                        progressBar.incrementProgressBy(1);
                        percent.setText(i + "%");
                        // Update the secondary progress value.
                        //progressDialog.incrementSecondaryProgressBy(5);
                        i++;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                // Close and delete the dialog when the progress bar is finished
                bar.dismiss();
            }
        });

        // Start the thread.
        thread.start();*/

        bar.show();
    }

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type, Activity activity){

        if (Build.VERSION.SDK_INT > 24){
            //try {
                return FileProvider.getUriForFile(activity,
                        activity.getApplicationContext().getPackageName() + FILE_PROVIDER_AUTHORITY, getOutputMediaFile(type, activity));
            /*} catch (IOException e) {
                e.printStackTrace();
                return null;
            }*/
        } else {
            return Uri.fromFile(getOutputMediaFile(type, activity));
        }
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type, Activity activity){

        // Check that the SDCard is mounted
        File mediaStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);


        // Create the storage directory(MyCameraVideo) if it does not exist
        if (! mediaStorageDir.exists()){

            if (! mediaStorageDir.mkdirs()){

                output = "Failed to create directory Video.";
                Log.e("output", output);

                Toast.makeText(activity, "Failed to create directory MyCameraVideo.",
                        Toast.LENGTH_LONG).show();

                Log.d("Video", "Failed to create directory MyCameraVideo.");
                return null;
            }
        }


        // Create a media file name

        // For unique file name appending current timeStamp with file name
        java.util.Date date= new java.util.Date();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(date.getTime());

        File mediaFile;

        if(type == MEDIA_TYPE_VIDEO) {

            // For unique video file name appending current timeStamp with file name
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");

        } else {
            return null;
        }

        return mediaFile;
    }


}
