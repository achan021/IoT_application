package com.example.ssandbox_testing;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import ai.djl.Model;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.transform.Normalize;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.translate.Pipeline;
import ai.djl.translate.Translator;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import tech.gusavila92.websocketclient.WebSocketClient;
import org.pytorch.Module;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.repository.zoo.Criteria;
import androidx.fragment.app.Fragment;


public class localInferenceFragment extends Fragment {

    View view;
    private WebSocketClient webSocketClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_localinference,container,false);
//        createWebSocketClient();

        Button send_button = view.findViewById(R.id.sendButton);
        send_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                sendMessage(v);
            }
        });

        Button load_button = view.findViewById(R.id.loadImage);
        load_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                loadImage(v);
            }
        });
        Button next_button = view.findViewById(R.id.next_button);
        next_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                loadImage(v);
            }
        });

        Button prev_button = view.findViewById(R.id.prev_button);
        prev_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                loadImage(v);
            }
        });

        Button predict = view.findViewById(R.id.Predict);
        predict.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                predict();
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

            }

            @Override
            public void onBinaryReceived(byte[] data) {

                try {
                    File file = new File(Environment.getExternalStorageDirectory().toString()+"/testing","model.pt");
                    FileOutputStream os = new FileOutputStream(file);
                    os.write(data);
                    os.close();
                    Module module = Module.load(Environment.getExternalStorageDirectory().toString()+"/testing/model.pt");


                } catch (IOException e) {
                    e.printStackTrace();
                }

                TextView textView = view.findViewById(R.id.textView);
                textView.setText("received!");


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
    public void sendMessage(View view) {
        Log.i("WebSocket", "Button was clicked");
        // Send button id string to WebSocket Server
        switch(view.getId()){
            case(R.id.sendButton):
                webSocketClient.send("Save");
                break;
        }
    }

    private int page = 0;
    private int pageItem = 6;
    private int fileNumber = 0;

    public void imageload_function(int page,int pageItem){
        File image_folder = new File(Environment.getExternalStorageDirectory().toString()+"/Pictures/AS_testing/MyCameraApp");
        fileNumber = image_folder.listFiles().length;
        Log.i("image","number of files is : "+fileNumber);
        for (int i = page*pageItem ; i <(page*pageItem)+pageItem; i ++){
            if (i < fileNumber) {
                Log.i("image","file number : "+i);
                File image_file = new File(Environment.getExternalStorageDirectory().toString() + "/Pictures/AS_testing/MyCameraApp"+File.separator + i + ".jpg");
                String filePath = image_file.getAbsolutePath();
                Bitmap bmImg = BitmapFactory.decodeFile(filePath);
                int id = getResources().getIdentifier("imageView" + i%pageItem, "id", this.getContext().getPackageName());

                Matrix matrix = new Matrix();
                matrix.postRotate(90);


                Bitmap rotatedBitmap = Bitmap.createBitmap(bmImg, 0, 0, bmImg.getWidth(), bmImg.getHeight(), matrix, true);

                ImageView imview = view.findViewById(id);
                imview.setImageBitmap(rotatedBitmap);

                //Reset the predictions to 0
                int Pid = getResources().getIdentifier("Prediction" + i%pageItem, "id", this.getContext().getPackageName());
                EditText EditT = view.findViewById(Pid);
                EditT.setText("None");
            }
            else{
                //Reset the images to null
                int id = getResources().getIdentifier("imageView" + i%pageItem, "id", this.getContext().getPackageName());
                ImageView imview = view.findViewById(id);
                imview.setImageBitmap(null);

                //Reset the predictions to 0
                int Pid = getResources().getIdentifier("Prediction" + i%pageItem, "id", this.getContext().getPackageName());
                EditText EditT = view.findViewById(Pid);
                EditT.setText("None");
            }
        }
    }

    //load image button
    public void loadImage(View view){
        Log.i("image","loading images");
        //load images from the saved folder
        switch(view.getId()){
            //load image
            case(R.id.loadImage):
                imageload_function(page,pageItem);
                break;
            //load next page of images
            case(R.id.next_button):
                page++;
                imageload_function(page,pageItem);
                break;
            //load prev page of images
            case(R.id.prev_button):
                page--;
                imageload_function(page,pageItem);
                break;
        }
    }

    //predict image button
    public void predict(){
        //set directory to search for models
        System.setProperty("ai.djl.repository.zoo.location",Environment.getExternalStorageDirectory().toString() + "/testing");
        Log.i("model","predict images");
        Criteria<Image, Classifications> criteria = Criteria.builder()
                .setTypes(Image.class,Classifications.class)
                .optArtifactId("ai.djl.localmodelzoo:model")
                .build();
        try {
            //load model directly without the use of model zoo
            Path modelDir = Paths.get(Environment.getExternalStorageDirectory().toString() + "/testing");
            Model model = Model.newInstance("model");
            model.load(modelDir,"model");

            //create a translator to translate to output labels
            Pipeline pipeline = new Pipeline();
            pipeline.add(new Resize(224,224)).add(new ToTensor()).add(new Normalize(
                    new float[] {0.485f, 0.456f, 0.406f},
                    new float[] {0.229f, 0.224f, 0.225f}));
            Translator<Image,Classifications> translator = ImageClassificationTranslator.builder()
                    .setPipeline(pipeline)
                    .optApplySoftmax(true)
                    .build();

            Predictor<Image,Classifications> predictor = model.newPredictor(translator);

//            if (fileNumber == 0){
//                //display popup to load image first
//                Log.i("Prediction","Pop up to load images first" + classification);
//            }
            for (int i = page*pageItem ; i <(page*pageItem)+pageItem; i ++){
                if (i < fileNumber) {
                    Log.i("image","file number : "+i);
                    File image_file = new File(Environment.getExternalStorageDirectory().toString() + "/Pictures/AS_testing/MyCameraApp/" + i + ".jpg");
                    Log.i("image",Environment.getExternalStorageDirectory().toString() + "/Pictures/AS_testing/MyCameraApp/" + i + ".jpg");
                    String filePath = image_file.getAbsolutePath();
                    Path img_path = Paths.get(filePath);
                    Image img = ImageFactory.getInstance().fromFile(img_path);
                    img.getWrappedImage();
                    Classifications classification = predictor.predict(img);
                    int id = getResources().getIdentifier("Prediction" + i%pageItem, "id", this.getContext().getPackageName());
                    String result = classification.best().getClassName();
                    Log.i("image",result);
                    EditText EditT = view.findViewById(id);
                    EditT.setText("Class : %s".format(result));
                }
                else{
                    int id = getResources().getIdentifier("Prediction" + i%pageItem, "id", this.getContext().getPackageName());
                    EditText EditT = view.findViewById(id);
                    EditT.setText("None");
                }
            }
        }catch(Exception e){
            Log.i("model","loading model failed");
            e.printStackTrace();
        }
    }



}

