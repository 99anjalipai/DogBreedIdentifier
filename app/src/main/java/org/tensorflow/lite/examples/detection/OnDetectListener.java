package org.tensorflow.lite.examples.detection;

public interface OnDetectListener
{
    void onClick(boolean b);
    void onDetected(boolean b);
    void onUploaded(boolean b);
    void onResponse(int code, float percent, String result);
}
