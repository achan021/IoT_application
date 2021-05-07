#!/usr/bin/env python

# WS server example

import asyncio
import websockets
import ML_functions
import pickle
import msgpack
import torch
import cv2
import torchvision.transforms as transforms

class WebsocketServerWorker():
    def __init__(self,
                 host : str,
                 port : int,
                 loop= None
                 ):

        self.host = host
        self.port = port

        if loop is None:
            loop = asyncio.new_event_loop()
        self.loop = loop

        self.broadcast_queue = asyncio.Queue()
        self.img_byte = []

    #consumer handler
    async def _consumer_handler(self,websocket : websockets.WebSocketCommonProtocol):
        """This handler listens for messages from WebsocketClientWorker
               objects.
           Args:
               websocket: the connection object to receive messages from and
                   add them into the queue.
        """
        try:
            while True:
                msg = await websocket.recv()
                await self.broadcast_queue.put(msg)
        except websockets.exceptions.ConnectionClosed:
            self._consumer_handler(websocket)

    #producer handler
    async def _producer_handler(self, websocket : websockets.WebSocketCommonProtocol):
        """This handler listens to the queue and processes messages as they
                arrive.
           Args:
                websocket: the connection object we use to send responses
                           back to the client.
        """

        while True:
            #get the message from the queue
            message = await self.broadcast_queue.get()
            #process the message

            if isinstance(message, bytes):
                while True:
                    if message == 'fin':
                        break
                    self.img_byte.append(message)
                    message = await self.broadcast_queue.get()

                print('performing inference')
                with open('./recv.jpg', 'wb') as out_s:
                    for m_bytes in self.img_byte:
                        out_s.write(m_bytes)

                self.img_byte = []
                load_model = torch.jit.load("./model.pt")

                received_image = cv2.imread('./recv.jpg')
                received_image = cv2.resize(received_image, (224, 224))

                transform = transforms.Compose([transforms.ToTensor(),
                                                transforms.Normalize((0.485, 0.456, 0.406), (0.229, 0.224, 0.225))])

                received_image = transform(received_image)
                received_image = received_image.reshape(-1, 3, 224, 224)
                output = load_model(received_image)
                output = torch.argmax(output)

                class_list = []
                with open('synset.txt', 'r') as in_s:
                    while(1):
                        line = in_s.readline()
                        if not line:
                            break
                        class_list.append(line)
                print(class_list)


                await websocket.send(class_list[output])

            response = self.process_message(message)
            #send the response
            await websocket.send(response)


    def process_message(self,message):
        '''
        Websocket only send str, so if the data is a dict obj,
        it will send only the string key, thats why we need
        to serialize the data. using pickle works since json
        cannot serialise tensor.
        '''


        message = message.strip()
        print("This is the message : {}".format(message))

        if message == "Hello":
            print('received message from phone')
            return "Greetings, Amigo!"
        elif message == "Save":
            # load the model saved file
            PATH = "./model.pt"
            # load_model = torch.jit.load("./model.pt")
            # net_state = ML_functions.load_model_checkpoint(PATH)
            # net_state_ser = pickle.dumps(net_state,-1)

            with open(PATH,"rb") as model_data:
                byte_data = model_data.read()

            #serialize using msgpack
            # packed = msgpack.packb(byte_data)

            return byte_data



        else:
            print(message)
            print(type(message))
            print("ERROR")


    async def _handler(self,websocket, path):
        """Setup the consumer and producer response handlers with asyncio.
           Args:
                websocket: the websocket connection to the client
        """

        asyncio.set_event_loop(self.loop)
        consumer_task = asyncio.ensure_future(self._consumer_handler(websocket))
        producer_task = asyncio.ensure_future(self._producer_handler(websocket))

        done,pending = await asyncio.wait(
            [consumer_task,producer_task],return_when=asyncio.FIRST_COMPLETED
        )

        for task in pending:
            task.cancel()

    def start(self):
        print("Starting the server...")
        start_server = websockets.serve(self._handler, self.host, self.port)


        asyncio.get_event_loop().run_until_complete(start_server)
        print("Server started...")
        try:
            asyncio.get_event_loop().run_forever()
        except KeyboardInterrupt:
            print("Websocket server stopped...")