/* Copyright 2019 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package org.tensorflow.lite.examples.detection.tflite;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Trace;
import android.util.Log;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.tensorflow.lite.Interpreter;
//import org.tensorflow.lite.support.metadata.MetadataExtractor;

/**
 * Wrapper for TensorFlow Lite classification models.
 * This class is specifically designed for classification tasks like dog breed identification.
 */
public class TFLiteClassificationModel implements Detector {
  private static final String TAG = "TFLiteClassificationModel";

  // Number of threads in the java app
  private static final int NUM_THREADS = 4;
  
  private boolean isModelQuantized;
  private int inputSize;
  
  // Pre-allocated buffers.
  private final List<String> labels = new ArrayList<>();
  private int[] intValues;
  
  // Output buffer for classification results
  private float[][] outputArray;
  
  private ByteBuffer imgData;
  private MappedByteBuffer tfLiteModel;
  private Interpreter.Options tfLiteOptions;
  private Interpreter tfLite;

  private TFLiteClassificationModel() {}

  /** Memory-map the model file in Assets. */
  private static MappedByteBuffer loadModelFile(AssetManager assets, String modelFilename)
      throws IOException {
    AssetFileDescriptor fileDescriptor = assets.openFd(modelFilename);
    FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
    FileChannel fileChannel = inputStream.getChannel();
    long startOffset = fileDescriptor.getStartOffset();
    long declaredLength = fileDescriptor.getDeclaredLength();
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
  }

  /**
   * Initializes a TensorFlow Lite session for classifying images.
   *
   * @param modelFilename The model file path relative to the assets folder
   * @param labelFilename The label file path relative to the assets folder
   * @param inputSize The size of image input
   * @param isQuantized Boolean representing model is quantized or not
   */
  public static Detector create(
      final Context context,
      final String modelFilename,
      final String labelFilename,
      final int inputSize,
      final boolean isQuantized)
      throws IOException {
    final TFLiteClassificationModel d = new TFLiteClassificationModel();

    MappedByteBuffer modelFile = loadModelFile(context.getAssets(), modelFilename);
    try (BufferedReader br =
                 new BufferedReader(
                         new InputStreamReader(
                                 context.getAssets().open(labelFilename), Charset.defaultCharset()))) {
      String line;
      while ((line = br.readLine()) != null) {
        Log.d(TAG, "Label: " + line);
        d.labels.add(line);
      }
    }


    d.inputSize = inputSize;

    try {
      Interpreter.Options options = new Interpreter.Options();
      options.setNumThreads(NUM_THREADS);
      options.setUseXNNPACK(true);
      d.tfLite = new Interpreter(modelFile, options);
      d.tfLiteModel = modelFile;
      d.tfLiteOptions = options;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    d.isModelQuantized = isQuantized;
    
    // Pre-allocate buffers.
    d.imgData = ByteBuffer.allocateDirect(1 * d.inputSize * d.inputSize * 3 * 4);
    d.imgData.order(ByteOrder.nativeOrder());
    d.intValues = new int[d.inputSize * d.inputSize];

    // Initialize output array for classification (1 batch, num_classes)
    d.outputArray = new float[1][d.labels.size()];
    
    return d;
  }

  @Override
  public List<Recognition> recognizeImage(final Bitmap bitmap) {
    // Log this method so that it can be analyzed with systrace.
    Trace.beginSection("recognizeImage");

    Trace.beginSection("preprocessBitmap");
    // Preprocess the image data from 0-255 int to normalized float based
    // on the provided parameters.
    bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

    imgData.rewind();
    for (int i = 0; i < inputSize; ++i) {
      for (int j = 0; j < inputSize; ++j) {
        int pixelValue = intValues[i * inputSize + j];
        if (isModelQuantized) {
          // Quantized model
          imgData.put((byte) ((pixelValue >> 16) & 0xFF));
          imgData.put((byte) ((pixelValue >> 8) & 0xFF));
          imgData.put((byte) (pixelValue & 0xFF));
        } else { 
          // Float model - normalize to [0,1] range
          imgData.putFloat(((pixelValue >> 16) & 0xFF) / 255.0f);
          imgData.putFloat(((pixelValue >> 8) & 0xFF) / 255.0f);
          imgData.putFloat((pixelValue & 0xFF) / 255.0f);
        }
      }
    }
    Trace.endSection(); // preprocessBitmap

    // Copy the input data into TensorFlow.
    Trace.beginSection("feed");
    Trace.endSection();

    // Run the inference call.
    Trace.beginSection("run");
    tfLite.run(imgData, outputArray);
    Trace.endSection();

    // Process the classification results
    Trace.beginSection("processResults");
    final ArrayList<Recognition> recognitions = new ArrayList<>();
    
    // Find the class with highest probability
    int maxIndex = 0;
    float maxConfidence = outputArray[0][0];
    
    for (int i = 1; i < outputArray[0].length; i++) {
      if (outputArray[0][i] > maxConfidence) {
        maxConfidence = outputArray[0][i];
        maxIndex = i;
      }
    }
    
    // Create a recognition result with the predicted class
    if (maxIndex < labels.size()) {
      String label = labels.get(maxIndex);
      // For classification, we don't have bounding boxes, so create a dummy one
      RectF dummyLocation = new RectF(0, 0, inputSize, inputSize);
      
      recognitions.add(
          new Recognition(
              String.valueOf(maxIndex), label, maxConfidence, dummyLocation));
      
      Log.d(TAG, "Predicted: " + label + " with confidence: " + maxConfidence);
    }
    
    Trace.endSection(); // processResults
    Trace.endSection(); // recognizeImage
    
    return recognitions;
  }

  @Override
  public void enableStatLogging(final boolean logStats) {}

  @Override
  public String getStatString() {
    return "";
  }

  @Override
  public void close() {
    if (tfLite != null) {
      tfLite.close();
      tfLite = null;
    }
  }

  @Override
  public void setNumThreads(int numThreads) {
    if (tfLite != null) {
      tfLiteOptions.setNumThreads(numThreads);
      recreateInterpreter();
    }
  }

  @Override
  public void setUseNNAPI(boolean isChecked) {
    if (tfLite != null) {
      tfLiteOptions.setUseNNAPI(isChecked);
      recreateInterpreter();
    }
  }

  private void recreateInterpreter() {
    tfLite.close();
    tfLite = new Interpreter(tfLiteModel, tfLiteOptions);
  }
} 