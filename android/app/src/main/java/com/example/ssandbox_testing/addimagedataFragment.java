package com.example.ssandbox_testing;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.ContentView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import static android.app.Activity.RESULT_OK;
import static android.os.Environment.getExternalStoragePublicDirectory;

public class addimagedataFragment extends Fragment {

    Button captureButton;
    ImageButton saveButton;
    ImageButton discardButton;
    String pathToFile;
    View view;
    String filename;

    private Camera mCamera;
    private CameraPreview cameraPreview;

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private byte[] previewData;

    String TAG = "cameraErorr";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //view allow us to locate the app elements
        view = inflater.inflate(R.layout.fragment_addimagedata, container, false);
        //we need to get the context (from extends Activity we can use this)

        //request camera and storage permission
        if(Build.VERSION.SDK_INT >=23){
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
        }

        //------------ACTIVITY COMPONENT REFERENCES
        //We use camera API instead of the camera app (intent)
        //create an instance of the camera
        mCamera = getCameraInstance();
        //rotate the camera to portrait
        mCamera.setDisplayOrientation(90);
        // Create our Preview view and set it as the content of our activity.
        cameraPreview = new CameraPreview(getActivity(), mCamera);
        FrameLayout preview = (FrameLayout) view.findViewById(R.id.cameraPreview);
        preview.addView(cameraPreview);

        //capture button reference
        captureButton = view.findViewById(R.id.captureimage);
        captureButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                dispatchPictureTakerAction();
            }
        });
        //save image button reference
        saveButton = view.findViewById(R.id.saveButton);
        saveButton.setVisibility(view.INVISIBLE);
        saveButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                //Set the folder and image name using alert dialog (future expansion)
//                //alerdialog filename and folder name
//                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                //set up the input
//                final EditText folderName = new EditText(getActivity());



                File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null){
                    Log.d(TAG, "Error creating media file, check storage permissions");
                    return;
                }

                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(previewData);
                    fos.close();
                } catch (FileNotFoundException e) {
                    Log.d(TAG, "File not found: " + e.getMessage());
                } catch (IOException e) {
                    Log.d(TAG, "Error accessing file: " + e.getMessage());
                }


                //toggle the visibility of the save, discard and capture button
                saveButton.setVisibility(view.INVISIBLE);
                discardButton.setVisibility(view.INVISIBLE);
                captureButton.setVisibility(view.VISIBLE);

                mCamera.startPreview();
            }
        });
        //discard image button reference
        discardButton = view.findViewById(R.id.discardButton);
        discardButton.setVisibility(view.INVISIBLE);
        discardButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //toggle the visibility of the save, discard and capture button
                saveButton.setVisibility(view.INVISIBLE);
                discardButton.setVisibility(view.INVISIBLE);
                captureButton.setVisibility(view.VISIBLE);

                mCamera.startPreview();
            }
        });

        return view;
    }

    //method that enforces the intent
    //1) create the photo file (storage place)
    //2) initiate the intent (takePic)
    private void dispatchPictureTakerAction() {

        String imageName;
        String imageFolder;




        mCamera.autoFocus(mAutoFocusCallBack);
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    Camera.AutoFocusCallback mAutoFocusCallBack = new Camera.AutoFocusCallback(){
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            camera.takePicture(null,null,mPicture);
        }
    };

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            //toggle the visibility of the save, discard and capture button
            saveButton.setVisibility(view.VISIBLE);
            discardButton.setVisibility(view.VISIBLE);
            captureButton.setVisibility(view.INVISIBLE);

            previewData = data;
    }
    };


    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().toString() +"/Pictures/AS_testing", "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        mediaStorageDir.mkdirs();

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }



        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath());
        File[] list = mediaFile.listFiles();
        int count = 0;
        for (File f : list){
            String name = f.getName();
            if (name.endsWith(".jpg")){
                count ++;
            }
        }


        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    (count) + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }


}
