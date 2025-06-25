#!C:\Users\APARNA\AppData\Local\Programs\Python\Python38\python.exe
##print("Content-Type: text/html")
##print()
import numpy
from PIL import ImageTk, Image
from keras.models import Sequential, load_model
from keras.models import load_model
model = load_model('dogbreed.h5')

classes = {1:'MALTESE',2:'SCOTTISH DEERHOUND',3:'AFGHAN HOUND',4:'BARNESE MOUNTAIN DOG'}

file_path='D:/Anjali academics/Final Year Project/data/image.jpg'
image = Image.open(file_path)
image = image.resize((30,30))
image = numpy.expand_dims(image, axis=0)
image = numpy.array(image)
pred = model.predict_classes([image])[0]
sign = classes[pred+1]
print(sign)

##print("<html><head>")
##print("<title>Result</title>")
##print("<style>body{background-color:lightblue;text-align:center;}")
##print("</style>")
##print("<body>")
##print("<h3>Fruit Type:</h3><br>")
##print(sign)
##print("<br>")
##print("</h3></body>")
##print("</html>")
