import torch
import torchvision
import torchvision.transforms as transforms

import torch.optim as optim

import torch.nn as nn
import torch.nn.functional as F

import CNN

from tqdm import tqdm

def dataloader_preprocess():
    #normalize the data
    transform = transforms.Compose(
        [transforms.ToTensor(),
         transforms.Normalize((0.5, 0.5, 0.5), (0.5, 0.5, 0.5))])

    #download the train set
    trainset = torchvision.datasets.CIFAR10(root='./data', train=True,
                                            download=True, transform=transform)
    trainloader = torch.utils.data.DataLoader(trainset, batch_size=4,
                                              shuffle=True, num_workers=0)
    #download the test set
    testset = torchvision.datasets.CIFAR10(root='./data', train=False,
                                           download=True, transform=transform)
    testloader = torch.utils.data.DataLoader(testset, batch_size=4,
                                             shuffle=False, num_workers=0)
    #types of classes
    classes = ('plane', 'car', 'bird', 'cat',
               'deer', 'dog', 'frog', 'horse', 'ship', 'truck')


    return classes,trainloader,testloader

def lossFunc_optim(net):
    criterion = nn.CrossEntropyLoss()
    optimizer = optim.SGD(net.parameters(), lr=0.001, momentum=0.9)
    return criterion,optimizer

def train_network(trainloader,optimizer,criterion,net):
    for epoch in range(2):  # loop over the dataset multiple times

        running_loss = 0.0
        for i, data in tqdm(enumerate(trainloader, 0)):
            # get the inputs; data is a list of [inputs, labels]
            inputs, labels = data

            # zero the parameter gradients
            optimizer.zero_grad()

            # forward + backward + optimize
            outputs = net(inputs)
            loss = criterion(outputs, labels)
            loss.backward()
            optimizer.step()

            # print statistics
            running_loss += loss.item()
            if i % 2000 == 1999:  # print every 2000 mini-batches
                print('[%d, %5d] loss: %.3f' %
                      (epoch + 1, i + 1, running_loss / 2000))
                running_loss = 0.0

    print('Finished Training')
    return net


def save_model(net):
    example_input = torch.rand(6,3,32,32)
    traced_script_module = torch.jit.trace(net,example_input)
    traced_script_module.save("./model.pt")

    # PATH = './cifar_net.pth.tar'
    # torch.save({
    #     'State_dict' : net.state_dict()
    # },PATH)

def load_model(PATH,net):
    return torch.jit.load(PATH)

    # checkpoint = torch.load(PATH)
    # net.load_state_dict(checkpoint['State_dict'])
    # return net


def test_model(testloader,net):

    correct = 0
    total = 0
    with torch.no_grad():
        for data in testloader:
            images, labels = data
            outputs = net(images)
            _, predicted = torch.max(outputs.data, 1)
            total += labels.size(0)
            correct += (predicted == labels).sum().item()

    print('Accuracy of the network on the 10000 test images: %d %%' % (
            100 * correct / total))