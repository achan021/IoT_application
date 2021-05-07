import argparse
import ML_functions
import CNN
import logging

from websocket_server import WebsocketServerWorker

def main():
    #argparser settings
    parser = argparse.ArgumentParser(description="Server instruction")
    parser.add_argument("-N","--New",help="0 : New Model, 1 : Load Model", default = 0)
    parser.add_argument("-T","--Train",help="0 : Train Model, 1 : Test Model", default = 0)
    parser.add_argument("-M","--Model",help="0: CNN",default=0)

    args = parser.parse_args()

    print("{},{},{}".format(args.New,args.Train,args.Model))

    logging.info("loading the datasets")
    # load the datasets
    classes, trainloader, testloader = ML_functions.dataloader_preprocess()

    net = None

    #new model
    logging.info("loading the model")
    if args.New == "0":
        #MODEL selection

        #CNN selected
        if args.Model == "0":
            # load the net CNN
            net = CNN.get_net()

    #load model
    elif args.New == "1":
        path = input("Input file path (relative to the root file)")
        path = "./" + path
        # MODEL selection
        # CNN selected
        if args.Model == "0":
            # load the net CNN
            net = CNN.get_net()
            net = ML_functions.load_model(path,net)
    else:
        print("Error")

    #load the optimers and loss function
    criterion, optimizer = ML_functions.lossFunc_optim(net)

    if args.Train == "0":
        #train the network
        net = ML_functions.train_network(trainloader, optimizer, criterion, net)
        # save the model
        ML_functions.save_model(net)

    elif args.Train == "1":
        #test the network
        ML_functions.test_model(testloader, net)
        ML_functions.save_model(net)

main()