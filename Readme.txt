Run instructions:
1) Load the android testing project and compile on android studios
2)run the websocket server in 4171_CourseProject\4171_server\websocket_sandbox using :
python host.py

files and descriptions:
1) 4171_server/Model_creation_preprocessing.ipynb : model preprocessing and creation script 
2) 4171_server/raw_split : categorical images for model training Without preprocessing
3) 4171_server/train_split : categorical images for model training with preprocessing
4) 4171_server/valid_split : categorical images for model validation 
5) 4171_server/websocket_sandbox/host.py : Model host script
6) 4171_server/websocket_sandbox/websocket_server.py : websocket producer and consumer handler script
7) 4171_server/websocket_sandbox/synset.txt : classification labels txt file
8) 4171_server/websocket_sandbox/recv.jpg : image received from mobile (chunking not implemented, therefore, image transferred from phone to server cannot be too large else connection time out before response received.
9) 4171_server/websocket_sandbox/model.pt : model file for server side inference. Model created from creation script can be saved here.

youtube video link : https://www.youtube.com/watch?v=buzCJ6MK6NU