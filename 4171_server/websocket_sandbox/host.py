import ML_functions
import CNN
import logging

logging.basicConfig(level=logging.DEBUG)
from websocket_server import WebsocketServerWorker

def main():

    # # logging.info("loading the datasets")
    # # #load the datasets
    # # classes,trainloader,testloader = ML_functions.dataloader_preprocess()
    # logging.info("loading the model")
    # #load the net CNN
    # net = CNN.get_net()
    # logging.info("loading the loss funciton and optimizers")
    # #load the optimers and loss function
    # criterion,optimizer = ML_functions.lossFunc_optim(net)
    # logging.info("training the network")
    # #train the network

    # net = ML_functions.train_network(trainloader,optimizer,criterion,net)
    # logging.info("saving the model")
    # #save the model
    # ML_functions.save_model(net)
    #
    # net = ML_functions.load_model("./cifar_net.pth.tar")
    # ML_functions.test_model(testloader,net)

    server_worker = WebsocketServerWorker(host = "192.168.1.149",port=8765)
    server_worker.start()
main()