import numpy as np 
import cv2
import tensorflow as tf
from PIL import Image
import os
from sklearn.model_selection import train_test_split
from keras.utils import to_categorical
from keras.models import Sequential, load_model
from keras.layers import Conv2D, MaxPool2D, Dense, Flatten, Dropout

def load_and_preprocess_image(image_path):
    try:
        # Read image using PIL
        image = Image.open(image_path)
        # Convert to RGB if image is in different format
        if image.mode != 'RGB':
            image = image.convert('RGB')
        # Resize image
        image = image.resize((30, 30))
        # Convert to numpy array
        image = np.array(image)
        return image
    except Exception as e:
        print(f"Error loading image {image_path}: {str(e)}")
        return None

# Initialize lists for data and labels
data = []
labels = []
classes = 4
cur_path = os.getcwd()

print(f"Current working directory: {cur_path}")

# Retrieving the images and their labels 
for i in range(classes):
    path = os.path.join(cur_path, 'train', str(i))
    print(f"Loading images from: {path}")
    
    if not os.path.exists(path):
        print(f"Warning: Directory {path} does not exist!")
        continue
        
    images = os.listdir(path)
    print(f"Found {len(images)} images in class {i}")

    for img_name in images:
        img_path = os.path.join(path, img_name)
        image = load_and_preprocess_image(img_path)
        
        if image is not None:
            data.append(image)
            labels.append(i)

if not data:
    raise Exception("No images were loaded! Please check the data directories.")

# Converting lists into numpy arrays
data = np.array(data)
labels = np.array(labels)

print(f"Data shape: {data.shape}, Labels shape: {labels.shape}")

# Splitting training and testing dataset
X_train, X_test, y_train, y_test = train_test_split(data, labels, test_size=0.2, random_state=1)

print(f"Training data shape: {X_train.shape}")
print(f"Testing data shape: {X_test.shape}")

# Converting the labels into one hot encoding
y_train = to_categorical(y_train, 4)
y_test = to_categorical(y_test, 4)

# Building the model
model = Sequential()
model.add(Conv2D(filters=32, kernel_size=(5,5), activation='relu', input_shape=X_train.shape[1:]))
model.add(Conv2D(filters=32, kernel_size=(5,5), activation='relu'))
model.add(MaxPool2D(pool_size=(2, 2)))
model.add(Dropout(rate=0.25))
model.add(Conv2D(filters=64, kernel_size=(3, 3), activation='relu'))
model.add(Conv2D(filters=64, kernel_size=(3, 3), activation='relu'))
model.add(MaxPool2D(pool_size=(2, 2)))
model.add(Dropout(rate=0.25))
model.add(Flatten())
model.add(Dense(256, activation='relu'))
model.add(Dropout(rate=0.5))
model.add(Dense(4, activation='softmax'))

# Compilation of the model
model.compile(loss='categorical_crossentropy', optimizer='adam', metrics=['accuracy'])

# Print model summary
model.summary()

# Training the model
print("\nStarting training...")
epochs = 15
history = model.fit(
    X_train, y_train,
    batch_size=32,
    epochs=epochs,
    validation_data=(X_test, y_test),
    verbose=1
)

# Save the model
model.save("dogbreed.h5")
print("\nModel saved as 'dogbreed.h5'")

