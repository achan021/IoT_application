package com.example.ssandbox_testing;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import tech.gusavila92.websocketclient.WebSocketClient;
import org.pytorch.Module;

import static android.app.Activity.RESULT_OK;

public class cloudInferenceFragment extends Fragment {

    private WebSocketClient webSocketClient;
    View view;
    private static final int PICK_IMAGE = 1;
    Uri imageUri;
    ImageView imgview;
    byte[] selectedImage;
    private ProgressBar simpleProgressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_cloudinference,container,false);

        createWebSocketClient();


        simpleProgressBar = (ProgressBar) view.findViewById(R.id.simpleProgressBar);

        Button cloud_button = view.findViewById(R.id.cloud_button);
        cloud_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                sendMessage(0);
            }
        });

        imgview = view.findViewById(R.id.imageView);
        Button select_button = view.findViewById(R.id.select_image);
        select_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        Button test_button = view.findViewById(R.id.test_connection);
        test_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                sendMessage(1);
            }
        });

        Button retrieve_button = view.findViewById(R.id.retrieve_model);
        retrieve_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                sendMessage(2);
                TextView textView = view.findViewById(R.id.Result);
                textView.setText("Retrieving Model...!");
                simpleProgressBar.setVisibility(view.VISIBLE);

            }
        });

        return view;


    }

    private void createWebSocketClient() {
        URI uri;
        try {
            // Connect to local host
            uri = new URI("ws://192.168.1.149:8765");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen() {
                Log.i("WebSocket", "Session is starting");
            }

            @Override
            public void onTextReceived(String s) {
                Log.i("WebSocket", "Message received");
                TextView textView = view.findViewById(R.id.Result);
                textView.setText(s);
            }

            @Override
            public void onBinaryReceived(byte[] data) {

                try {
                    File file = new File(Environment.getExternalStorageDirectory().toString()+"/testing","model.pt");
                    FileOutputStream os = new FileOutputStream(file);
                    os.write(data);
                    os.close();



                } catch (IOException e) {
                    e.printStackTrace();
                }

                TextView textView = view.findViewById(R.id.Result);
                textView.setText("Model Updated!");
                simpleProgressBar.setVisibility(view.INVISIBLE);

//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            TextView textView = findViewById(R.id.textView);
//                            textView.setText("received!");
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
            }

            @Override
            public void onPingReceived(byte[] data) {
            }

            @Override
            public void onPongReceived(byte[] data) {
            }

            @Override
            public void onException(Exception e) {
                System.out.println(e.getMessage());
            }

            @Override
            public void onCloseReceived() {
                Log.i("WebSocket", "Closed ");
                System.out.println("onCloseReceived");
            }
        };
        webSocketClient.setConnectTimeout(10000);
        webSocketClient.setReadTimeout(60000);
        webSocketClient.enableAutomaticReconnection(5000);
        webSocketClient.connect();
    }

    //we tag the button to this function
    public void sendMessage(int message_type) {
        Log.i("WebSocket", "Button was clicked");
        // Send button id string to WebSocket Server

        switch(message_type){
            case 0:
                //inference
//                if (selectedImage.length > 20){
//                    int segments = (int) Math.ceil(selectedImage.length/20);
//                    for (int i =0;i<segments;i++){
//                        webSocketClient.send(Arrays.copyOfRange(selectedImage,i*20,i+20));
//                    }
//                }else{
//                    webSocketClient.send(selectedImage);
//                }
                webSocketClient.send(selectedImage);
                webSocketClient.send("fin");
                break;
            case 1:
                //test connection
                webSocketClient.send("Hello");
                break;
            case 2:
                //test connection
                webSocketClient.send("Save");
                break;
            default:
                break;
        }


    }
    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE){
            imageUri = data.getData();
            imgview.setImageURI(imageUri);

            try {
                InputStream iStream = this.getContext().getContentResolver().openInputStream(imageUri);
                selectedImage = getBytes(iStream);
                Log.i("iStream","iStream : Success?");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}
