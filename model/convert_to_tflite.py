import tensorflow as tf
from keras.models import load_model

# Load the trained model
model = load_model('dogbreed.h5')

# Convert the model to TensorFlow Lite format
converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()

# Save the TensorFlow Lite model
with open('dogbreed.tflite', 'wb') as f:
    f.write(tflite_model)

print("Model converted to TensorFlow Lite format successfully!") 