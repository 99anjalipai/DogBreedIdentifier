package org.tensorflow.lite.examples.detection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;


import com.bumptech.glide.Glide;

import org.tensorflow.lite.examples.detection.tflite.Detector;
import org.tensorflow.lite.examples.detection.tflite.TFLiteClassificationModel;

import java.io.IOException;
import java.util.List;

public class ResultActivity extends AppCompatActivity implements OnDetectListener {

    TextView TXbreedName;
    TextView TXconfidence;
    ImageView IVupload, IVbreed;

    OnDetectListener onDetectListener;
    private Detector detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        onDetectListener = this;

        Intent data = getIntent();

        Uri imageUri = Uri.parse(data.getStringExtra("imageUri"));
        Log.d("ResultActivity", "Received image URI: " + imageUri.toString());

        IVupload = findViewById(R.id.image_result);
        IVbreed = findViewById(R.id.image_breed);
        TXbreedName = findViewById(R.id.text_breed_name);
        TXconfidence = findViewById(R.id.text_confidence);

        IVupload.setImageURI(imageUri);

        try {
            Toast.makeText(this, "Decoding image...", Toast.LENGTH_SHORT).show();
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            Toast.makeText(this, "Decoded successfully", Toast.LENGTH_SHORT).show();


            // Initialize the local classification model
            initializeModel();
            
            // Process the image locally
            processImageLocally(bitmap);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeModel() {
        try {
            detector = TFLiteClassificationModel.create(
                this,
                "dogbreed.tflite",
                "labelmap.txt",
                30,  // input size
                false // is quantized
            );
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error initializing model", Toast.LENGTH_SHORT).show();
        }
    }

    private void processImageLocally(Bitmap bitmap) {
        if (detector == null) {
            Toast.makeText(this, "Model not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        // Resize bitmap to 30x30 for the model
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 30, 30, true);
        
        // Run inference
        List<Detector.Recognition> results = detector.recognizeImage(resizedBitmap);
        
        if (!results.isEmpty()) {
            Detector.Recognition result = results.get(0);
            
            // Display the result
            TXbreedName.setText(result.getTitle());
            TXconfidence.setText(String.format("Confidence: %.2f%%", result.getConfidence() * 100));
            
            // You can also load a sample image of the breed if available
            // For now, we'll just show the uploaded image
            IVbreed.setImageBitmap(bitmap);
            
        } else {
            TXbreedName.setText("No breed detected");
            TXconfidence.setText("Confidence: 0%");
        }
    }

    @Override
    public void onClick(boolean b) {
        // Not used in this activity
    }

    @Override
    public void onDetected(boolean b) {
        // Not used in this activity
    }

    @Override
    public void onUploaded(boolean b) {
        // Not used in this activity
    }

    @Override
    public void onResponse(int code, float percent, String result) {
        // Not used in this activity - we process locally now
    }
}